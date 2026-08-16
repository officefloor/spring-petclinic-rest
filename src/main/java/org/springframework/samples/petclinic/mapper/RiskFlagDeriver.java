package org.springframework.samples.petclinic.mapper;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: a single boolean that is {@code true} when the
 * owner warrants manual review and {@code false} otherwise.
 *
 * <p>The flag is {@code true} when <em>any</em> of these hold, and {@code false} when
 * none do:
 * <ul>
 *   <li>the owner is a possible (soft) duplicate of an existing owner
 *       ({@code possibleDuplicate} is true);</li>
 *   <li>the owner's email domain is <em>disposable-adjacent</em> — it is not on the
 *       hard disposable blocklist (those are rejected outright at create time and never
 *       stored) but is closely related to a known disposable provider: it is a subdomain
 *       of a known disposable domain, or shares a known disposable domain's second-level
 *       label under a different top-level domain (e.g. {@code sub.mailinator.com} or
 *       {@code mailinator.net});</li>
 *   <li>the owner's city is over its soft capacity — it had already reached the soft
 *       capacity warning threshold when the owner was created ({@code capacityWarning}
 *       is true).</li>
 * </ul>
 *
 * <p>Every input is drawn from the owner's own stored fields, so the flag is a pure,
 * deterministic function of the owner and reads back unchanged after create.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic mapping method and apply it to unrelated
 * fields; the mapper references it only through an explicit expression.
 */
public final class RiskFlagDeriver {

    /**
     * Second-level labels of the known disposable email providers. The hard blocklist
     * ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}) is
     * rejected outright on create; a domain is "disposable-adjacent" when it is built on
     * one of these labels but is not itself an exact blocklist entry.
     */
    private static final Set<String> DISPOSABLE_LABELS =
        Set.of("mailinator", "tempmail", "guerrillamail");

    private RiskFlagDeriver() {
    }

    /**
     * Returns {@code true} when the owner is a possible duplicate, has a
     * disposable-adjacent email domain, or is in a city over its soft capacity;
     * otherwise {@code false}.
     */
    public static boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || isDisposableAdjacentEmail(owner.getEmail());
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: a subdomain of a known
     * disposable domain, or a domain whose second-level label matches a known disposable
     * provider under any top-level domain. A {@code null}/blank email, or one with no
     * usable domain, is not disposable-adjacent.
     */
    private static boolean isDisposableAdjacentEmail(String email) {
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
            return false;
        }
        // The registrable second-level label is the label immediately before the
        // top-level label (the last two labels being '<secondLevel>.<tld>').
        String secondLevel = labels[labels.length - 2];
        return DISPOSABLE_LABELS.contains(secondLevel);
    }
}
