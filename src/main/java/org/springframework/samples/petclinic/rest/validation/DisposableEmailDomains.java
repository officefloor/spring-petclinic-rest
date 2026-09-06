package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Holds the blocklist of disposable email domains and decides whether a given owner email uses one.
 * A disposable domain is a throwaway mail provider ({@code mailinator.com}, {@code tempmail.com},
 * {@code guerrillamail.com}) that owners must not register with. Keeping the blocklist and the
 * matching logic in one place lets the create endpoint treat "is this a disposable address" as a
 * single opaque question.
 *
 * <p>Matching is case-insensitive (using {@link Locale#ROOT} so it is locale-independent) and looks
 * only at the domain part after the final {@code '@'}; syntactic validity of the address is enforced
 * separately by Bean Validation ({@code @Email} on the owner fields), so a malformed or missing
 * address is rejected before this runs and is treated here as not disposable.
 */
@Component
public class DisposableEmailDomains {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Reports whether the given email's domain is on the disposable-domain blocklist. A {@code null},
     * blank, or {@code '@'}-less value has no comparable domain and is treated as not disposable.
     *
     * @param email the raw email value from the request, or {@code null} when omitted
     * @return {@code true} when the email's domain is blocklisted
     */
    public boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * Reports whether the given email's domain is <em>disposable-adjacent</em>: not itself a
     * blocklisted disposable domain (an exact match is the stronger {@link #isDisposable(String)}
     * signal and is rejected outright at create time), but closely related to one. A domain is
     * adjacent to a blocklisted domain when it is a subdomain of it (for example
     * {@code inbox.mailinator.com} relative to {@code mailinator.com}) or when it shares that
     * blocklisted domain's second-level label under a different suffix (for example
     * {@code mailinator.net} relative to {@code mailinator.com}). As with the blocklist match this is
     * case-insensitive ({@link Locale#ROOT}) and looks only at the domain part after the final
     * {@code '@'}; a {@code null}, blank, or {@code '@'}-less value has no comparable domain and is
     * treated as not adjacent.
     *
     * @param email the email value to inspect, or {@code null} when omitted
     * @return {@code true} when the email's domain is disposable-adjacent
     */
    public boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || BLOCKED_DOMAINS.contains(domain)) {
            return false;
        }
        String label = secondLevelLabel(domain);
        for (String blocked : BLOCKED_DOMAINS) {
            if (domain.endsWith("." + blocked) || label.equals(secondLevelLabel(blocked))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the lower-cased domain part after the final {@code '@'} of an email, or {@code null}
     * when the value is {@code null} or carries no {@code '@'}. Shared by the disposable and
     * disposable-adjacent checks so both read the domain identically.
     */
    private String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * The registrable second-level label of a domain: the label immediately before its final dot
     * (for example {@code mailinator} for both {@code mailinator.com} and {@code inbox.mailinator.com}),
     * or the whole value when it has no dot.
     */
    private String secondLevelLabel(String domain) {
        String[] parts = domain.split("\\.");
        return parts.length >= 2 ? parts[parts.length - 2] : domain;
    }
}
