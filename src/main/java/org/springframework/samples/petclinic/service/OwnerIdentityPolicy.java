package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: every owner is identified by a single derived {@code identityKey} of
 * {@code normalizedTelephone|email|householdId}. This consolidates the former separate
 * telephone, email and household duplicate checks into one key: on creation an owner is
 * rejected only when its WHOLE identityKey exactly matches an existing owner's. Because the
 * telephone is part of the key, two members of the same household (same householdId) with
 * different telephones have different identityKeys and are both allowed; only an exact
 * full-key match is a duplicate. Kept as a small, self-contained unit so the rule can be
 * enforced from the create flow without adding complexity to the controller or service.
 */
public final class OwnerIdentityPolicy {

    private OwnerIdentityPolicy() {
    }

    /**
     * Assign the owner's householdId (when it knowingly joins an existing household) and reject
     * the owner when its whole identityKey matches any other existing owner's.
     *
     * @param clinicService   source of the existing owners
     * @param owner           the owner being created
     * @param sharesHousehold whether the request opted in to a shared household
     * @throws DuplicateIdentityException if the identityKey is already in use
     */
    public static void rejectDuplicateIdentity(ClinicService clinicService, Owner owner, Boolean sharesHousehold) {
        assignHouseholdId(clinicService, owner, sharesHousehold);
        String key = identityKey(owner);
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId()) && key.equals(identityKey(existing))) {
                throw new DuplicateIdentityException();
            }
        }
    }

    /** The owner's derived identity key: normalized telephone, email and household id, '|'-joined. */
    public static String identityKey(Owner owner) {
        return normalizeTelephone(owner.getTelephone()) + "|" + normalizeEmail(owner.getEmail()) + "|"
            + orEmpty(owner.getHouseholdId());
    }

    /** Adopt the existing household's id when sharing was opted in and a matching owner already exists. */
    private static void assignHouseholdId(ClinicService clinicService, Owner owner, Boolean sharesHousehold) {
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String lastName = normalizeHousehold(owner.getLastName());
        String address = normalizeHousehold(owner.getAddress());
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId())
                && lastName.equals(normalizeHousehold(existing.getLastName()))
                && address.equals(normalizeHousehold(existing.getAddress()))) {
                owner.setHouseholdId(householdId(lastName, address));
                return;
            }
        }
    }

    private static String normalizeTelephone(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.toLowerCase();
    }

    private static String normalizeHousehold(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
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

    /** Thrown when another owner already has the same identityKey. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateIdentityException extends RuntimeException {
    }
}
