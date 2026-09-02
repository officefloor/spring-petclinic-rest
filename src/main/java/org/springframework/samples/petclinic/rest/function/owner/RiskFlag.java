package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Computes an owner's soft risk flag: true when any single soft-risk signal holds — the owner is a
 * possible duplicate, its email domain is disposable-adjacent, or its city is over soft capacity (the
 * {@link CityCapacity} warning band). The duplicate and capacity signals are computed elsewhere and
 * passed in; only the email check lives here.
 */
public final class RiskFlag {

    /** Disposable-signalling tokens; a stored email whose domain contains one is "disposable-adjacent".
     *  Exact disposable domains are rejected at create ({@link NormalizeOwnerEmail}), so only near
     *  misses ever reach a stored owner. */
    private static final List<String> DISPOSABLE_TOKENS =
            List.of("mailinator", "guerrilla", "tempmail", "throwaway", "disposable", "trashmail");

    private RiskFlag() {
    }

    public static boolean of(Owner owner, boolean possibleDuplicate, boolean overCapacity) {
        return possibleDuplicate || overCapacity || disposableAdjacentEmail(owner);
    }

    private static boolean disposableAdjacentEmail(Owner owner) {
        String email = owner.getEmail();
        if (email == null) {
            return false;
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
        for (String token : DISPOSABLE_TOKENS) {
            if (domain.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
