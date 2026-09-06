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
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return BLOCKED_DOMAINS.contains(domain);
    }
}
