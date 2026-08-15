package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Shared derivation of an owner's {@code identityKey} — the single value that all duplicate
 * detection is now expressed through. The key is
 * {@code normalizedTelephone + "|" + (email or empty) + "|" + householdId}, where:
 *
 * <ul>
 * <li>{@code normalizedTelephone} is the E.164 form (see {@link TelephoneE164});</li>
 * <li>the email component is the lower-cased email, or empty when none is supplied; and</li>
 * <li>{@code householdId} is the owner's deterministic household identifier, derived from its last
 *     name and postcode (see {@link Household}).</li>
 * </ul>
 *
 * <p>Two owners are duplicates only when their <em>whole</em> keys are equal. Because the telephone
 * is part of the key, two members of the same household (same {@code householdId}) with different
 * telephones have different keys and are both allowed.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /**
     * Identity key for an incoming create-owner request (after {@link ValidateNewOwner}). The
     * household component is the deterministic id derived from the request's last name and postcode,
     * so the key matches the one the owner will carry once saved.
     */
    public static String forRequest(OwnerFieldsDto request) {
        return build(request.getTelephone(), request.getEmail(), Household.idFor(request));
    }

    /** Identity key for an existing, stored owner. */
    public static String forOwner(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private static String build(String telephone, String email, String householdId) {
        return normalizeTelephone(telephone) + "|" + normalizeEmail(email) + "|"
                + (householdId == null ? "" : householdId);
    }

    /** E.164 form when the value can form one, otherwise the raw value so it only matches itself. */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return "";
        }
        try {
            return TelephoneE164.normalize(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone;
        }
    }

    /** Lower-cased email, or empty when none is supplied. */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
