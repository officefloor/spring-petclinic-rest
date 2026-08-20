package org.springframework.samples.petclinic.rest.function.common;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: a single read-only summary that is true when
 * <em>any</em> risk signal holds, otherwise false.
 *
 * <p>The signals are:
 * <ul>
 *   <li><b>possible duplicate</b> — the owner's {@code possibleDuplicate} flag is set (a soft match
 *       against an existing owner on lastName + postcode with a differing telephone);</li>
 *   <li><b>disposable-adjacent email domain</b> — the email's domain resembles a disposable-mail
 *       provider. The hard blocklist ({@code mailinator.com}, {@code tempmail.com},
 *       {@code guerrillamail.com}) is rejected outright at creation, so this softer check flags
 *       domains that are <em>adjacent</em> to those: any domain carrying one of the blocked
 *       providers' core labels (e.g. {@code mailinator.net}, {@code mail.tempmail.io}) or a common
 *       disposable-mail keyword;</li>
 *   <li><b>city over soft capacity</b> — the owner's city has reached the soft-capacity warning band
 *       (the {@code capacityWarning} signal), i.e. it holds 40 or more owners, approaching the hard
 *       limit of 50.</li>
 * </ul>
 *
 * <p>All inputs are read from the owner's own state, which the create and read pipelines populate
 * (persisting {@code possibleDuplicate}, and assigning the transient {@code capacityWarning}) before
 * the response is mapped, so the flag is derived consistently on both arms.
 */
public final class RiskFlags {

    /**
     * Core labels of the disposable-mail blocklist plus common disposable-mail keywords. A domain is
     * disposable-adjacent when it contains any of these tokens.
     */
    private static final Set<String> DISPOSABLE_TOKENS = Set.of(
            "mailinator", "tempmail", "guerrillamail",
            "throwaway", "disposable", "trashmail", "yopmail",
            "maildrop", "getnada", "sharklasers", "mailnesia");

    private RiskFlags() {
    }

    /** Computes the owner's risk flag. */
    public static boolean of(Owner owner) {
        return isPossibleDuplicate(owner)
                || isDisposableAdjacentEmail(owner.getEmail())
                || isCityOverSoftCapacity(owner);
    }

    private static boolean isPossibleDuplicate(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate());
    }

    private static boolean isDisposableAdjacentEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        if (domain.isEmpty()) {
            return false;
        }
        for (String token : DISPOSABLE_TOKENS) {
            if (domain.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCityOverSoftCapacity(Owner owner) {
        return Boolean.TRUE.equals(owner.getCapacityWarning());
    }
}
