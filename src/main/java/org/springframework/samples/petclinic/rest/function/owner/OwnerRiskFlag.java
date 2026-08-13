package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes the response-time {@code riskFlag} surfaced on
 * {@link org.springframework.samples.petclinic.rest.dto.OwnerDto}. It is true when any of these
 * hold, otherwise false:
 * <ul>
 *   <li>the owner is a possible duplicate ({@link Owner#getPossibleDuplicate()} true, as assigned
 *       by {@link AssignOwnerPossibleDuplicate});</li>
 *   <li>the owner's email domain is <em>disposable-adjacent</em> — its second-level label matches a
 *       blocklisted disposable provider (see {@link RejectDisposableEmailDomain}), e.g.
 *       {@code mailinator.net} or {@code sub.tempmail.com}, without necessarily being the exact
 *       blocklisted domain;</li>
 *   <li>the owner's city is over its soft capacity ({@link OwnerCityCounts#overSoftCapacity}).</li>
 * </ul>
 * Like {@code capacityWarning} it is derived at response time, not persisted.
 */
final class OwnerRiskFlag {

    /**
     * Second-level labels of the disposable-domain blocklist in {@link RejectDisposableEmailDomain}
     * (mailinator.com, tempmail.com, guerrillamail.com). A domain is disposable-adjacent when one of
     * its dot-separated labels is one of these.
     */
    private static final Set<String> DISPOSABLE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    private OwnerRiskFlag() {
    }

    static boolean of(OwnerRepository ownerRepository, Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || disposableAdjacentEmail(owner.getEmail())
                || OwnerCityCounts.overSoftCapacity(ownerRepository, owner);
    }

    /** True when the email's domain shares a label with a blocklisted disposable provider. */
    private static boolean disposableAdjacentEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
