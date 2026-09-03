package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Computes an owner's advisory risk flag. An owner is risky when it is a possible duplicate, when
 * its email uses a disposable-adjacent domain (a known throwaway-mail domain or a subdomain of one),
 * or when its city is over its soft capacity (the per-city capacity-warning threshold).
 */
public final class RiskFlag {

    private static final List<String> DISPOSABLE_DOMAINS =
        List.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private RiskFlag() {
    }

    /** {@code true} when the owner trips any advisory risk condition. */
    public static boolean of(Owner owner) {
        return owner.isPossibleDuplicate()
            || disposableAdjacent(owner.getEmail())
            || Boolean.TRUE.equals(owner.getCapacityWarning());
    }

    /** Whether {@code email}'s domain is, or is a subdomain of, a known disposable-mail domain. */
    private static boolean disposableAdjacent(String email) {
        int at = email == null ? -1 : email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return DISPOSABLE_DOMAINS.stream()
            .anyMatch(d -> domain.equals(d) || domain.endsWith("." + d));
    }
}
