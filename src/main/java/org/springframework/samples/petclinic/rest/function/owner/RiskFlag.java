package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives an owner's {@code riskFlag}: {@code true} when any risk signal holds — the owner
 * is a possible duplicate ({@link DetectPossibleDuplicate}), its email domain is
 * disposable-adjacent, or its city is over soft capacity ({@link CityCapacity}) — otherwise
 * {@code false}. Read-only; nothing is mutated.
 *
 * <p>A domain is <em>disposable-adjacent</em> when its second-level label matches that of a
 * known disposable provider (mailinator, tempmail, guerrillamail) under any suffix — the exact
 * blocked domains are already refused at creation by {@link NormaliseEmail}, so this catches the
 * look-alikes (e.g. {@code mailinator.net}) that slip through.
 */
public final class RiskFlag {

    private static final Set<String> DISPOSABLE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    private RiskFlag() {
    }

    public static boolean of(Owner owner, OwnerRepository ownerRepository) {
        return owner.getPossibleDuplicateOf() != null
                || disposableAdjacent(owner.getEmail())
                || CityCapacity.approaching(owner.getCity(), ownerRepository);
    }

    private static boolean disposableAdjacent(String email) {
        if (email == null) {
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
