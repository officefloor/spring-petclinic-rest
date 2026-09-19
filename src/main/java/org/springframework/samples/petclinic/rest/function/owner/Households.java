package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.AddressNormalizer;

/**
 * Shared household logic. An owner belongs to a household identified by their last name
 * and postal address. The last name is compared case-insensitively with collapsed
 * whitespace; the address is compared in its normalized form (see {@link AddressNormalizer})
 * so the same household resolves regardless of casing, spacing or street-type abbreviation.
 * The identifier is derived deterministically from those two fields, so every owner in the
 * same household resolves to the same stable value without any coordination.
 */
final class Households {

    private Households() {
    }

    /**
     * The existing owners that share a household (same last name and address) with the
     * given fields.
     */
    static List<Owner> membersOf(OwnerRepository ownerRepository, String lastName, String address) {
        String key = normalizeName(lastName);
        String addr = AddressNormalizer.normalize(address);
        return ownerRepository.findByLastName(lastName).stream()
                .filter(owner -> normalizeName(owner.getLastName()).equals(key)
                        && AddressNormalizer.normalize(owner.getAddress()).equals(addr))
                .toList();
    }

    /**
     * The stable identifier shared by every owner at the given last name and address.
     */
    static String idFor(String lastName, String address) {
        return sha256Hex(normalizeName(lastName) + "|" + AddressNormalizer.normalize(address))
                .substring(0, 12).toUpperCase();
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
