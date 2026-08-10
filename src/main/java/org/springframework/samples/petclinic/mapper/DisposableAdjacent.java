package org.springframework.samples.petclinic.mapper;

import java.util.Locale;
import java.util.Set;

/**
 * Decides whether an email address is <em>disposable-adjacent</em>: its domain belongs to the same
 * family as a known disposable email provider without being one of the hard-blocked disposable
 * domains (those are rejected outright at create time, so a stored email never carries one).
 *
 * <p>A domain is disposable-adjacent when its second-level label (the label immediately before the
 * final TLD) matches a known disposable provider's label. That catches a different TLD
 * ({@code mailinator.net}, {@code guerrillamail.biz}) and a subdomain ({@code smtp.mailinator.com}),
 * while an unrelated domain that merely starts the same ({@code mailinatorish.com},
 * {@code tempmailer.com}) is not matched — the label must be exact.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s {@code riskFlag} expression
 * via {@link RiskFlag}) rather than a mapper {@code default} method to avoid MapStruct picking it up
 * as an automatic conversion.
 */
final class DisposableAdjacent {

    /** Second-level labels of the known disposable providers. */
    private static final Set<String> DISPOSABLE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    private DisposableAdjacent() {
    }

    /** True when {@code email} is present and its domain is disposable-adjacent. */
    static boolean matches(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false; // no TLD -> no second-level label to compare
        }
        String secondLevel = labels[labels.length - 2];
        return DISPOSABLE_LABELS.contains(secondLevel);
    }
}
