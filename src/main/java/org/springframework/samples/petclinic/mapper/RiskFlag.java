package org.springframework.samples.petclinic.mapper;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: a single boolean that is {@code true} when the owner trips any
 * of the "review this signup" heuristics, otherwise {@code false}. The flag is {@code true} when any
 * of these hold:
 *
 * <ul>
 * <li><b>possible duplicate</b> &mdash; the owner's {@code possibleDuplicate} is set (it shares an
 * existing owner's phonetic last name and postcode with a different identity; see
 * {@code FlagPossibleDuplicate});</li>
 * <li><b>disposable-adjacent email</b> &mdash; the owner has an email whose domain
 * {@link #disposableAdjacentEmail(String) looks like} a disposable / throw-away mail provider. The
 * hard blocklist (mailinator.com, tempmail.com, guerrillamail.com) is rejected outright at create,
 * so this catches the <em>adjacent</em> cases: a known disposable brand under a different TLD
 * (e.g. {@code mailinator.net}) or a domain built from a disposable keyword;</li>
 * <li><b>city over soft capacity</b> &mdash; the owner's {@code capacityWarning} is set, i.e. the
 * owner's city had reached its soft capacity (40 or more owners, approaching the hard cap of 50)
 * when the owner was created.</li>
 * </ul>
 *
 * <p>Every input is read from the persisted owner, so the flag recomputes identically on read-back.
 * Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class RiskFlag {

    /**
     * Second-level-domain keywords that mark an email domain as disposable-adjacent. Matched as a
     * case-insensitive substring of the domain's second-level label, so both the known disposable
     * brands under any TLD (e.g. {@code mailinator.net}, {@code tempmail.io}) and domains built from
     * a disposable keyword (e.g. {@code throwaway-mail.co}) are caught.
     */
    private static final Set<String> DISPOSABLE_KEYWORDS = Set.of(
            "mailinator", "tempmail", "temp-mail", "guerrilla", "throwaway", "throw-away",
            "trashmail", "trash-mail", "disposable", "yopmail", "getnada", "maildrop",
            "sharklasers", "10minutemail", "mintemail", "dispostable");

    private RiskFlag() {
    }

    /** {@code true} when the owner trips any of the risk heuristics; otherwise {@code false}. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || disposableAdjacentEmail(owner.getEmail());
    }

    /**
     * {@code true} when {@code email} has a domain whose second-level label contains a known
     * disposable-mail keyword; {@code false} when the email is absent, malformed, or its domain
     * looks ordinary.
     */
    public static boolean disposableAdjacentEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        String secondLevel = secondLevelLabel(domain);
        if (secondLevel == null) {
            return false;
        }
        for (String keyword : DISPOSABLE_KEYWORDS) {
            if (secondLevel.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The domain's second-level label &mdash; the label immediately before the top-level domain
     * (e.g. {@code mailinator} for {@code mailinator.com}, {@code tempmail} for {@code sub.tempmail.io}).
     * Returns {@code null} when the domain has no dot.
     */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return null;
        }
        return labels[labels.length - 2];
    }
}
