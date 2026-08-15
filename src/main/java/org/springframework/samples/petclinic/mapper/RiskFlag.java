package org.springframework.samples.petclinic.mapper;

import java.util.Locale;
import java.util.Set;

/**
 * Derives an owner's {@code riskFlag}: a single boolean that is {@code true} when any one of three
 * risk signals holds, otherwise {@code false}.
 *
 * <ol>
 * <li>the owner is a possible duplicate ({@code possibleDuplicate} is {@code true});</li>
 * <li>the owner's email domain is <em>disposable-adjacent</em> - equal to, a subdomain of, or
 * sharing the second-level label of a known disposable domain (the exact disposable domains are
 * rejected at create, so this catches the near-misses that slip past that block, e.g. a subdomain
 * {@code mail.mailinator.com} or the same brand on another TLD {@code mailinator.net});</li>
 * <li>the owner's city is over its soft capacity ({@code capacityWarning} is {@code true}).</li>
 * </ol>
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * method declared on a MapStruct mapper would be picked up as an implicit conversion and applied to
 * every matching property mapping.
 */
public final class RiskFlag {

    /**
     * Known disposable email domains. Kept in sync with the create-time block list; an email whose
     * domain equals, is a subdomain of, or shares the second-level label of one of these is
     * considered disposable-adjacent.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private RiskFlag() {
    }

    /**
     * Returns {@code true} when the owner is a possible duplicate, has a disposable-adjacent email
     * domain, or is over its city's soft capacity; otherwise {@code false}.
     */
    public static boolean of(Boolean possibleDuplicate, String email, Boolean capacityWarning) {
        return Boolean.TRUE.equals(possibleDuplicate)
            || disposableAdjacent(email)
            || Boolean.TRUE.equals(capacityWarning);
    }

    /**
     * Whether {@code email}'s domain is adjacent to a known disposable domain: an exact match, a
     * subdomain of one, or a domain sharing the same second-level label (the brand label before the
     * TLD). Case-insensitive; {@code null}/blank or address-less values are not adjacent.
     */
    static boolean disposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        if (domain.isEmpty()) {
            return false;
        }
        String secondLevel = secondLevelLabel(domain);
        for (String disposable : DISPOSABLE_EMAIL_DOMAINS) {
            if (domain.equals(disposable) || domain.endsWith("." + disposable)) {
                return true;
            }
            if (secondLevel.equals(secondLevelLabel(disposable))) {
                return true;
            }
        }
        return false;
    }

    /** The second-level label of a domain (the label immediately before the TLD). */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return domain;
        }
        return labels[labels.length - 2];
    }
}
