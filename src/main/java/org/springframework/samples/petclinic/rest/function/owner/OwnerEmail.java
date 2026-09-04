package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional owner {@code email}. Email is absent when null or blank; when present it must
 * be a syntactically valid address whose domain is not on the disposable-domain blocklist, and is
 * stored/returned lower-cased.
 */
public final class OwnerEmail {

    /**
     * A pragmatic syntactic check: a non-empty local part, a single {@code @}, and a domain carrying at
     * least one dot, none of the three parts containing whitespace or an {@code @}.
     */
    private static final Pattern SYNTAX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Disposable email domains that are rejected: an address in one of these is a throwaway inbox, so
     * an owner supplying one is a 400.
     */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmail() {
    }

    /**
     * @return the trimmed, lower-cased email, or {@code null} when it is absent (null or blank).
     * @throws InvalidEmailException when present but not syntactically valid.
     * @throws DisposableEmailException when present and its domain is on the disposable-domain blocklist.
     */
    static String normalize(String email) throws InvalidEmailException, DisposableEmailException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!SYNTAX.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        String normalized = trimmed.toLowerCase();
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailException(email);
        }
        return normalized;
    }

    /**
     * Whether the address's domain is <em>disposable-adjacent</em>: it shares its registrable base
     * name (the second-level label, e.g. {@code mailinator} in {@code mailinator.com}) with a known
     * disposable-email provider, yet is not itself on the {@link #BLOCKED_DOMAINS blocklist} — for
     * instance {@code mailinator.net} (same brand, different TLD) or {@code sub.tempmail.com} (a
     * subdomain of a blocked domain). An address whose exact domain is blocked never reaches storage
     * (it is a 400 at create), so this recognises the near-misses that slip past that gate.
     *
     * @param email a stored/normalized email, or {@code null}/blank when absent.
     * @return true when present and disposable-adjacent; false when absent or unrelated.
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
        if (domain.isEmpty() || BLOCKED_DOMAINS.contains(domain)) {
            return false;
        }
        Set<String> labels = new java.util.HashSet<>(java.util.Arrays.asList(domain.split("\\.")));
        for (String blocked : BLOCKED_DOMAINS) {
            String base = blocked.substring(0, blocked.indexOf('.'));
            if (labels.contains(base)) {
                return true;
            }
        }
        return false;
    }
}
