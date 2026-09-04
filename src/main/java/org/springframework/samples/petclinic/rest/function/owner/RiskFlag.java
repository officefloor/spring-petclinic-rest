package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Response-time risk rule: an owner is flagged {@code riskFlag} when any of three signals hold — it
 * is a {@link PossibleDuplicate}, its email domain is disposable-adjacent, or its city is over soft
 * capacity (the {@link CapacityWarning} band). Reads the two flags the responder already derived, so
 * it mirrors the values reported for {@link PossibleDuplicate} and {@link CapacityWarning}.
 */
public final class RiskFlag {

    /** Second-level labels of known disposable mail services; a domain sharing one is "adjacent". */
    private static final Set<String> DISPOSABLE_LABELS =
            Set.of("mailinator", "tempmail", "guerrillamail", "trashmail", "throwawaymail", "10minutemail");

    private RiskFlag() {
    }

    public static void mark(OwnerDto dto, Owner owner) {
        boolean duplicate = Boolean.TRUE.equals(dto.getPossibleDuplicate());
        boolean overCapacity = Boolean.TRUE.equals(dto.getCapacityWarning());
        dto.setRiskFlag(duplicate || overCapacity || disposableAdjacent(owner.getEmail()));
    }

    private static boolean disposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        int dot = domain.indexOf('.');
        String label = dot < 0 ? domain : domain.substring(0, dot);
        return DISPOSABLE_LABELS.contains(label);
    }
}
