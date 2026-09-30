package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner whose email domain is on the disposable-domain blocklist. Email is optional, so
 * an absent (null/blank) email is skipped; a present but syntactically invalid address is already
 * rejected 400 by the {@code @Email} constraint at {@link ValidateOwner}. Runs after
 * {@link NormalizeOwnerEmail} has lower-cased the address, so the domain comparison is
 * case-insensitive. A blocklisted domain is rejected 400 via {@link DisposableEmailDomainException}.
 */
public class ValidateOwnerEmailDomain {

    /** Disposable email domains that are not accepted for owner registration. */
    private static final Set<String> BLOCKED_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The second-level (registrable) labels of the {@link #BLOCKED_DOMAINS}, e.g. {@code mailinator}
     * from {@code mailinator.com}. A domain sharing one of these labels under a different TLD or with
     * extra sub-labels is "disposable-adjacent" — it passes the exact-match blocklist above but still
     * belongs to a known disposable provider.
     */
    private static final Set<String> DISPOSABLE_BASE_LABELS = BLOCKED_DOMAINS.stream()
        .map(ValidateOwnerEmailDomain::secondLevelLabel)
        .filter(label -> label != null)
        .collect(Collectors.toUnmodifiableSet());

    /**
     * Whether {@code email}'s domain is disposable-adjacent: its second-level label (the label
     * immediately left of the TLD, e.g. {@code mailinator} in {@code mailinator.net} or
     * {@code sub.tempmail.io}) matches a known disposable provider, but the full domain is not an
     * exact {@link #BLOCKED_DOMAINS} entry (those never reach persistence — they are rejected 400
     * here). A null/blank email or one with no domain part is not adjacent. Case-insensitive.
     */
    static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            return false; // the disposable set itself, not merely adjacent
        }
        String label = secondLevelLabel(domain);
        return label != null && DISPOSABLE_BASE_LABELS.contains(label);
    }

    /** The label immediately left of the TLD in {@code domain}, or null when it has no TLD. */
    private static String secondLevelLabel(String domain) {
        if (domain == null) {
            return null;
        }
        String[] labels = domain.split("\\.");
        return labels.length < 2 ? null : labels[labels.length - 2];
    }

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null) {
            return; // optional when absent
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return; // no domain part; syntactic validity is enforced by @Email at ValidateOwner
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(
                "Email domain " + domain + " is not accepted (disposable-domain blocklist)");
        }
    }
}
