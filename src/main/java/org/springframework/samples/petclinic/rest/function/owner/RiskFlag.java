package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: {@code true} when at least one risk signal holds at the
 * moment the owner is read, otherwise {@code false}. The signals are:
 *
 * <ul>
 * <li><b>Possible duplicate</b> — the owner's {@code possibleDuplicate} is set (it shares an
 * existing owner's last-name {@link Soundex} code and {@code postcode}; see
 * {@link AssignPossibleDuplicate}).</li>
 * <li><b>Disposable-adjacent email</b> — the owner's email domain is related to a known
 * disposable-mail provider (see {@link #isDisposableAdjacent}).</li>
 * <li><b>Over soft city capacity</b> — the owner's {@code capacityWarning} is set (its city held
 * 40-49 owners when created, i.e. it is over the soft capacity of
 * {@value AssignCapacityWarning#CAPACITY_WARNING_THRESHOLD} but under the hard limit; see
 * {@link AssignCapacityWarning}).</li>
 * </ul>
 *
 * <p>The flag is a pure function of already-derived owner state, so it is computed at response
 * time rather than stored.
 */
public final class RiskFlag {

    /**
     * Second-level labels of the disposable-mail providers on the hard blocklist enforced by
     * {@link RequireNonDisposableOwnerEmail} (mailinator.com, tempmail.com, guerrillamail.com). An
     * email domain that carries one of these labels — whether the exact blocked domain, a subdomain
     * of it, or the same name under a different TLD (e.g. {@code mailinator.net}, {@code
     * mail.tempmail.com}) — is treated as disposable-adjacent.
     */
    private static final Set<String> DISPOSABLE_LABELS =
            Set.of("mailinator", "tempmail", "guerrillamail");

    private RiskFlag() {
    }

    /** Whether any risk signal holds for the given owner. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || isDisposableAdjacent(owner.getEmail());
    }

    /**
     * Whether the email's domain is disposable-adjacent: it carries the second-level label of a
     * known disposable-mail provider. A null, blank or address without a domain is not adjacent.
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
