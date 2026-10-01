package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * Single source of truth for an owner's {@code identityKey}: the one derived value all duplicate
 * detection on {@code POST /api/owners} is expressed through, and the {@code identityKey} field the
 * owner response carries. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}; two owners are the same
 * identity exactly when their whole keys are equal.
 *
 * <p>This is a model-level derivation of an {@link Owner} (like {@link MembershipLevel}): it is
 * consumed both by the response mapper and by the create pipeline's identity check, so it lives in the
 * model layer rather than beside the pipeline functions.
 *
 * <p>It deliberately folds the former separate telephone, email and household duplicate checks into
 * one comparison. Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed — only an
 * exact full-key match is a duplicate.
 *
 * <p>The components are normalized the same way for a submitted owner and a stored one, so values with
 * incidental telephone formatting or email casing still match: the telephone is run through
 * {@link OwnerTelephone#toE164(String)} and the email is lower-cased. A {@code null} component
 * contributes the empty string.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The {@code identityKey} of a persisted or in-flight owner, computed from its stored fields. */
    public static String of(Owner owner) {
        String normalizedTelephone = OwnerTelephone.toE164(owner.getTelephone());
        if (normalizedTelephone == null) {
            normalizedTelephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return normalizedTelephone + "|" + email + "|" + householdId;
    }
}
