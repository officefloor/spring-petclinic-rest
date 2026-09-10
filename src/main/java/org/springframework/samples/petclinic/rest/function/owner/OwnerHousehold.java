package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared derivation of an owner's household identity — the normalized last name and address
 * (case-insensitive, collapsed whitespace) and the stable {@code householdId} computed from
 * them. Used by {@link AssignOwnerHousehold} (which assigns and back-fills the id) and by
 * {@link EnsureUniqueOwnerIdentity} (which needs the id a request would resolve to, to build
 * its {@link OwnerIdentityKey}), so both agree on exactly what a household is.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * The {@code householdId} the given last name and address would resolve to if at least one
     * existing owner already shares that household, otherwise {@code null} (a solo owner keeps a
     * null householdId).
     */
    static String resolve(String lastName, String address, OwnerRepository ownerRepository) {
        String ln = normalizeName(lastName);
        String ad = normalizeAddress(address);
        for (Owner existing : ownerRepository.findAll()) {
            if (ln.equals(normalizeName(existing.getLastName()))
                    && ad.equals(normalizeAddress(existing.getAddress()))) {
                return id(ln, ad);
            }
        }
        return null;
    }

    /** Stable 12-char upper-hex identifier derived from the normalized household key. */
    static String id(String normalizedLastName, String normalizedAddress) {
        String key = normalizedLastName + "\n" + normalizedAddress;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Address comparison uses the same normalized form the endpoint stores (see {@link OwnerAddress}). */
    static String normalizeAddress(String value) {
        String normalized = OwnerAddress.normalize(value);
        return normalized == null ? "" : normalized;
    }
}
