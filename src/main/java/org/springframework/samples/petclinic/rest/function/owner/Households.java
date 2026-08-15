package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Canonical household logic shared by the create-owner pipeline: what counts as the same household
 * (same last name and address, compared case-insensitively with collapsed whitespace) and the stable
 * {@code householdId} derived from it. {@link RejectDuplicateIdentity} uses this to work out the
 * household component of a request's identity key, and {@link AssignHousehold} uses it to assign and
 * back-fill the shared id, so both compute the same value.
 */
final class Households {

    private Households() {
    }

    /**
     * The {@code householdId} the given request would receive, or {@code null} when it gets none. An
     * owner joins a household — and so shares its id — only when it opts in with
     * {@code sharesHousehold: true} and an existing owner already occupies the same household. A lone
     * owner, or one that does not opt in, has no household id.
     */
    static String resolve(String lastName, String address, boolean sharesHousehold,
            OwnerRepository ownerRepository) {
        if (!sharesHousehold) {
            return null;
        }
        String normalizedLastName = normalizeName(lastName);
        String normalizedAddress = AddressNormalizer.normalize(address);
        for (Owner existing : ownerRepository.findAll()) {
            if (sameHousehold(normalizedLastName, normalizedAddress, existing)) {
                return householdId(normalizedLastName, normalizedAddress);
            }
        }
        return null;
    }

    /** Whether {@code existing} is in the household identified by the given normalized name/address. */
    static boolean sameHousehold(String normalizedLastName, String normalizedAddress, Owner existing) {
        return normalizedLastName.equals(normalizeName(existing.getLastName()))
                && normalizedAddress.equals(AddressNormalizer.normalize(existing.getAddress()));
    }

    /** Lower-case, trim and collapse internal whitespace runs to a single space. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Derive the stable household identifier {@code 'H-' + first 12 upper-case hex characters of
     * SHA-256(normalizedLastName + '\n' + normalizedAddress)}. Deterministic, so every owner in the
     * same household resolves to the same value.
     */
    static String householdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedLastName + "\n" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return "H-" + sb;
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
