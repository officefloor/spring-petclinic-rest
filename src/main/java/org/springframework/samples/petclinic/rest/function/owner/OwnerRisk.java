package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag} at read time. The flag is true when any of these hold:
 * <ul>
 *   <li>the owner is a possible duplicate ({@code possibleDuplicate} true — see
 *       {@link AssignPossibleDuplicate});</li>
 *   <li>the owner's email domain is <em>disposable-adjacent</em> — a variant of a known
 *       disposable-email domain that shares one of its domain labels (e.g. {@code mailinator},
 *       {@code tempmail}, {@code guerrillamail}) without being the exact blocked domain. The exact
 *       blocked domains are rejected at create time by {@link ValidateOwnerEmailDomain}, so a stored
 *       owner is flagged only for adjacent variants such as {@code mailinator.net} or
 *       {@code sub.mailinator.com};</li>
 *   <li>the owner's city is over its soft capacity ({@code capacityWarning} true — see
 *       {@link AssignCapacityWarning}).</li>
 * </ul>
 * False otherwise. Purely derived from stored fields, so it is computed by the mapper rather than
 * persisted.
 */
public final class OwnerRisk {

    /** Domain labels of the disposable-email blocklist ({@code mailinator.com}, etc.). */
    private static final Set<String> DISPOSABLE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    private OwnerRisk() {
    }

    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || isDisposableAdjacent(owner.getEmail());
    }

    /**
     * True when the email's domain shares a label with a known disposable-email domain — an exact
     * blocked domain, a variant TLD, or a subdomain of one.
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        if (domain.isEmpty()) {
            return false;
        }
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
