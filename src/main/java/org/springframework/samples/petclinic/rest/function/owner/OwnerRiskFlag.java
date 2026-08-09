package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The owner {@code riskFlag}, derived at read time and surfaced on the owner response. It is
 * {@code true} when <em>any</em> of these hold, otherwise {@code false}:
 *
 * <ul>
 * <li>the owner is a possible duplicate — {@code possibleDuplicate} is {@code true} (assigned at
 *     creation by {@link AssignPossibleDuplicate});</li>
 * <li>the owner's email domain is <em>disposable-adjacent</em> — its second-level label matches a
 *     known disposable-email provider (see {@link #DISPOSABLE_LABELS}). An <em>exactly</em>
 *     disposable domain is rejected at creation by {@link NormalizeOwnerEmail}, so what this catches
 *     is the neighbours it lets through: the same provider on a different TLD (e.g.
 *     {@code mailinator.org}) or a subdomain of one (e.g. {@code inbox.mailinator.com});</li>
 * <li>the owner's city is over its soft capacity — it already holds at least
 *     {@link OwnerCityCapacity#CITY_SOFT_CAPACITY} owners (see
 *     {@link OwnerCityCapacity#isOverSoftCapacity}).</li>
 * </ul>
 */
public final class OwnerRiskFlag {

    /**
     * Second-level labels of the known disposable-email providers (the {@code DISPOSABLE_DOMAINS} in
     * {@link NormalizeOwnerEmail}, minus their TLD). A stored email is disposable-adjacent when its
     * domain's second-level label is one of these, regardless of the TLD or any subdomain.
     */
    private static final Set<String> DISPOSABLE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    private OwnerRiskFlag() {
    }

    /** The owner's read-time risk flag; see the class documentation. */
    public static boolean isRisk(Owner owner, OwnerRepository ownerRepository) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || isDisposableAdjacent(owner.getEmail())
                || OwnerCityCapacity.isOverSoftCapacity(ownerRepository, owner.getCity());
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: its second-level label (the label before
     * the final TLD label) matches a known disposable-email provider. A null/blank email, or one with
     * no domain, is not disposable-adjacent. The comparison is case-insensitive.
     */
    static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (domain.isBlank()) {
            return false;
        }
        String[] labels = domain.split("\\.");
        String secondLevel = labels.length >= 2 ? labels[labels.length - 2] : labels[0];
        return DISPOSABLE_LABELS.contains(secondLevel);
    }
}
