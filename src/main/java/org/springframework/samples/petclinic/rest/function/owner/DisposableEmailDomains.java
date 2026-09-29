package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * The single definition of the disposable-email-domain blocklist. An owner email whose domain is one of
 * these throwaway providers is rejected. Matching is case-insensitive and applies only to a present,
 * syntactically valid address (a blank email has no domain to check).
 *
 * <p>Also defines the softer <em>disposable-adjacent</em> notion (see
 * {@link #isDisposableAdjacent(String)}) used by the read-model risk flag: an address is
 * disposable-adjacent when its domain is not itself blocked (those never reach persistence, being
 * rejected at create) yet resembles a throwaway provider — it shares a blocked provider's
 * second-level name under a different TLD, or its second-level name carries a throwaway-mail token.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method; this is a plain helper, not a wired function.
 */
public final class DisposableEmailDomains {

    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com",
            "tempmail.com",
            "guerrillamail.com");

    /**
     * Tokens that, when contained in a domain's second-level name, mark it as throwaway-adjacent even
     * when the exact domain is not on the blocklist.
     */
    private static final Set<String> ADJACENT_TOKENS = Set.of(
            "mailinator",
            "tempmail",
            "temp-mail",
            "guerrilla",
            "throwaway",
            "throw-away",
            "disposable",
            "trashmail",
            "fakemail",
            "10minutemail",
            "yopmail",
            "getnada",
            "maildrop",
            "dispostable");

    /**
     * Whether the given (non-blank) email address sits on a blocked domain. The domain is the part after
     * the last {@code @}, compared case-insensitively.
     */
    static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED.contains(domain);
    }

    /**
     * Whether the given email address is <em>disposable-adjacent</em>: its domain resembles a throwaway
     * provider without being an exactly-blocked one. True when the domain's second-level name equals a
     * blocked provider's second-level name (e.g. {@code mailinator.net} beside blocked
     * {@code mailinator.com}) or contains a throwaway-mail token (see {@link #ADJACENT_TOKENS}). A blank
     * or domain-less address is never adjacent.
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || domain.isEmpty()) {
            return false;
        }
        String sld = secondLevelName(domain);
        for (String blocked : BLOCKED) {
            if (sld.equals(secondLevelName(blocked))) {
                return true;
            }
        }
        return ADJACENT_TOKENS.stream().anyMatch(sld::contains);
    }

    /** The lower-cased domain (part after the last {@code @}), or null when absent. */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).trim().toLowerCase();
    }

    /** The second-level label of a domain — the label immediately left of the TLD (e.g.
     *  {@code mailinator} of {@code mailinator.com}), or the whole domain when it has no dot. */
    private static String secondLevelName(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot < 0) {
            return domain;
        }
        String withoutTld = domain.substring(0, lastDot);
        int prevDot = withoutTld.lastIndexOf('.');
        return prevDot < 0 ? withoutTld : withoutTld.substring(prevDot + 1);
    }

    private DisposableEmailDomains() {
    }
}
