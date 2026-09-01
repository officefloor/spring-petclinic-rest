package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: true when any single risk signal holds — the owner is a
 * {@link FlagPossibleDuplicate possible duplicate}, its city is over soft capacity
 * ({@link EnsureCityCapacity capacityWarning}), or its email domain is disposable-adjacent. A domain is
 * disposable-adjacent when its registrable base label matches a known disposable provider
 * ({@code mailinator}, {@code tempmail}, {@code guerrillamail}) under any TLD or as a subdomain (e.g.
 * {@code mailinator.net} or {@code inbox.mailinator.com}) — the exact disposable domains are already
 * rejected at create by {@link RejectDisposableEmail}, so this catches the look-alikes that slip through.
 */
public final class OwnerRiskFlag {

    private static final Set<String> DISPOSABLE_BASES = Set.of("mailinator", "tempmail", "guerrillamail");

    private OwnerRiskFlag() {
    }

    /** True when any risk signal holds, otherwise false. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || disposableAdjacent(owner.getEmail());
    }

    private static boolean disposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String[] labels = email.substring(at + 1).split("\\.");
        return labels.length >= 2 && DISPOSABLE_BASES.contains(labels[labels.length - 2]);
    }
}
