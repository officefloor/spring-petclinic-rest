package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Derives an owner's {@code identityKey}: the single value all duplicate detection is now expressed
 * through. It is the full lower-case hex {@link Sha256} of {@code normalizedTelephone + '|' +
 * lowerEmail + '|' + soundex(lastName)}, where the telephone is normalized to E.164 the same way as
 * {@link NormalizeTelephone}/{@link TelephoneE164}, the email is trimmed and lower-cased (empty when
 * absent or blank, the same form as {@link NormalizeEmail}), and the surname component is
 * {@link Soundex} of the last name. Because the telephone is part of the key, two members of the same
 * household — same {@code soundex(lastName)} and postcode but different telephones — derive different
 * identityKeys and are not a hard duplicate; the postcode is no longer part of the key, so it is the
 * soft-match ({@link DetectPossibleDuplicate}) that pairs such owners.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email, String lastName) {
        String normalizedTelephone = TelephoneE164.normalizeOrNull(telephone);
        if (normalizedTelephone == null) {
            normalizedTelephone = telephone == null ? "" : telephone.trim();
        }
        String normalizedEmail = (email == null || email.isBlank())
                ? "" : email.trim().toLowerCase(Locale.ROOT);
        String soundex = Soundex.of(lastName);
        return Sha256.hex(normalizedTelephone + "|" + normalizedEmail + "|" + soundex);
    }
}
