package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared household matching: canonicalizes the free-text fields that identify a household
 * (last name and address) so values that differ only in letter case, in the amount of
 * surrounding/internal whitespace, or (for the address) in common street-type
 * abbreviations compare equal. Used to detect when a new owner shares a household with an
 * existing one, and to derive the stable identifier that owners in the same household share.
 */
final class Households {

    private Households() {
    }

    /**
     * Canonicalizes a last name for case-insensitive, whitespace-insensitive comparison:
     * trims the ends, collapses every run of whitespace to a single space and lower-cases
     * the result. Returns an empty string when {@code lastName} is {@code null}.
     */
    private static String normalizeName(String lastName) {
        if (lastName == null) {
            return "";
        }
        return lastName.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Canonicalizes an address using the same normalization applied when an owner is created
     * (see {@link Addresses#normalize(String)}), so household comparisons see the stored,
     * abbreviation-expanded form. Returns an empty string when {@code address} is
     * {@code null}.
     */
    private static String normalizeAddress(String address) {
        String normalized = Addresses.normalize(address);
        return normalized == null ? "" : normalized;
    }

    /**
     * Whether {@code owner} belongs to the household identified by {@code lastName} and
     * {@code address}, comparing the last name and the address in their canonical forms.
     */
    static boolean matches(Owner owner, String lastName, String address) {
        return normalizeName(lastName).equals(normalizeName(owner.getLastName()))
                && normalizeAddress(address).equals(normalizeAddress(owner.getAddress()));
    }

    /**
     * The stable identifier shared by every owner in the household identified by
     * {@code lastName} and {@code address}. Derived purely from the canonical household key,
     * so the same household always yields the same id without any coordination.
     */
    static String id(String lastName, String address) {
        String key = normalizeName(lastName) + "\n" + normalizeAddress(address);
        return "H-" + sha256Hex(key).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    /**
     * The household id a create request resolves to: the shared id of the household it joins
     * when it opts in ({@code sharesHousehold} true) and an existing owner already lives at
     * the same address under the same last name, otherwise an empty string. Mirrors the
     * assignment performed by {@link AssignHousehold}.
     */
    static String resolveHouseholdId(OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return "";
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (matches(existing, request.getLastName(), request.getAddress())) {
                return id(request.getLastName(), request.getAddress());
            }
        }
        return "";
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
