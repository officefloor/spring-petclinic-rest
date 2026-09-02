package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: two owners must not share a household by accident. On creation an owner is
 * rejected when another owner already has the same last name and address, compared
 * case-insensitively after collapsing runs of whitespace. The caller may opt in to sharing a
 * household ({@code sharesHousehold == true}), which skips the check. Kept as a small,
 * self-contained unit so the rule can be enforced from the create flow without adding
 * complexity to the controller or service.
 */
public final class OwnerHouseholdPolicy {

    private OwnerHouseholdPolicy() {
    }

    /**
     * Reject the given owner if another existing owner has the same last name and address,
     * unless the request explicitly opted in to sharing a household.
     *
     * @param clinicService   source of the existing owners
     * @param owner           the owner being created
     * @param sharesHousehold whether the request opted in to a shared household
     * @throws DuplicateHouseholdException if the household already exists and sharing was not opted in
     */
    public static void rejectDuplicateHousehold(ClinicService clinicService, Owner owner, Boolean sharesHousehold) {
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        owner.setHouseholdId(householdId(lastName, address));
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId())
                && lastName.equals(normalize(existing.getLastName()))
                && address.equals(normalize(existing.getAddress()))) {
                if (!Boolean.TRUE.equals(sharesHousehold)) {
                    throw new DuplicateHouseholdException();
                }
                return;
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Derive a stable identifier shared by every owner in the same (last name, address) household. */
    private static String householdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Thrown when another owner already shares the same last name and address. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateHouseholdException extends RuntimeException {
    }
}
