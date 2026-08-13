package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Household membership shared by {@link CheckUniqueOwnerIdentity} (which needs the householdId a
 * request would receive to build its identity key) and {@link AssignOwnerHousehold} (which assigns
 * that id). Members of a household are owners with the same last name and address, compared
 * case-insensitively with collapsed whitespace and canonical address form; the household id is an
 * existing member's id when present, otherwise a stable value derived from the normalized last name
 * and address so independent joiners compute the same value.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /** The existing owners that share the request's household (same last name and address). */
    static List<Owner> matchingMembers(OwnerFieldsDto request, OwnerRepository ownerRepository) {
        String lastName = normalizeName(request.getLastName());
        String address = OwnerAddressNormalizer.normalize(request.getAddress());
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalizeName(existing.getLastName()))
                    && address.equals(OwnerAddressNormalizer.normalize(existing.getAddress()))) {
                members.add(existing);
            }
        }
        return members;
    }

    /**
     * The householdId this request would be assigned, or {@code null} when it forms no household:
     * only requests that opt in with {@code sharesHousehold} true and match an existing member get
     * one. Equal to the value {@link AssignOwnerHousehold} later stores, so the identity key checked
     * before the owner is built matches the one returned afterwards.
     */
    static String resolve(OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return null;
        }
        List<Owner> members = matchingMembers(request, ownerRepository);
        if (members.isEmpty()) {
            return null;
        }
        String existing = existingId(members);
        return existing != null ? existing : deriveId(request);
    }

    /** Reuse an id already assigned to any household member, so late joiners keep the shared value. */
    static String existingId(List<Owner> members) {
        for (Owner member : members) {
            if (member.getHouseholdId() != null) {
                return member.getHouseholdId();
            }
        }
        return null;
    }

    /** A stable 8-hex-character identifier derived from the request's last name and address. */
    static String deriveId(OwnerFieldsDto request) {
        return deriveId(normalizeName(request.getLastName()),
                OwnerAddressNormalizer.normalize(request.getAddress()));
    }

    private static String deriveId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-case and collapse runs of whitespace so comparison is case- and whitespace-insensitive. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
