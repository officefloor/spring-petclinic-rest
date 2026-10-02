package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared household rules for the owner-create pipeline. A household is the set of owners with
 * the same last name at the same address, compared case-insensitively with collapsed
 * whitespace. The membership match and the stable identifier are defined once here so the gate
 * ({@link ValidateUniqueHousehold}), the back-fill ({@link ApplySharedHousehold}) and the new
 * owner ({@link BuildOwner}) all agree on them.
 */
final class Household {

    private Household() {
    }

    /**
     * The household identifier published between steps. A dedicated type keeps the variable
     * unambiguous (OfficeFloor matches variables by type) and distinct from other strings in
     * the pipeline. {@link #ABSENT} (an empty value) means the new owner is not joining a
     * shared household.
     */
    record Id(String value) {

        static final Id ABSENT = new Id("");

        boolean isPresent() {
            return this.value != null && !this.value.isBlank();
        }
    }

    /**
     * Existing owners that share the request's household (same normalized last name and
     * address).
     */
    static List<Owner> membersOf(OwnerFieldsDto request, OwnerRepository ownerRepository) {
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                members.add(existing);
            }
        }
        return members;
    }

    /**
     * Stable identifier for the request's household: the first 12 upper-case hex characters of
     * the SHA-256 of the normalized last name and address, so every owner in the same household
     * derives the same value regardless of creation order.
     */
    static Id idFor(OwnerFieldsDto request) {
        String seed = normalize(request.getLastName()) + "\n" + normalize(request.getAddress());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return new Id(sb.toString());
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Normalize a name or address for household comparison: trim, collapse any run of
     * whitespace to a single space, and lower-case. Yields {@code ""} for a {@code null} input.
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
