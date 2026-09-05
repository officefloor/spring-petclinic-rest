package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Derives an owner's {@code identityKey}: the single value all duplicate detection is now expressed
 * through. This is the version-2 derivation: the full lower-case hex {@link Sha256} of {@code regionV2
 * + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where {@code regionV2} is
 * the version-2 region code (see {@link RegionCode#v2FromPostcode(String)}, which mixes the fixed
 * {@code "V2"} tag into the postcode-derived region), the telephone is normalized to E.164 the same
 * way as {@link NormalizeTelephone}/{@link TelephoneE164}, the email is trimmed and lower-cased (empty
 * when absent or blank, the same form as {@link NormalizeEmail}), and the surname component is
 * {@link Soundex} of the last name. Mixing in the {@code "V2"} tag changes every key from its
 * version-1 value. Because the telephone is part of the key, two members of the same household — same
 * region, {@code soundex(lastName)} and postcode but different telephones — still derive different
 * identityKeys and are not a hard duplicate; it is the soft-match ({@link DetectPossibleDuplicate})
 * that pairs such owners.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email, String lastName, String postcode) {
        String region = RegionCode.v2FromPostcode(postcode);
        String normalizedTelephone = TelephoneE164.normalizeOrNull(telephone);
        if (normalizedTelephone == null) {
            normalizedTelephone = telephone == null ? "" : telephone.trim();
        }
        String normalizedEmail = (email == null || email.isBlank())
                ? "" : email.trim().toLowerCase(Locale.ROOT);
        String soundex = Soundex.of(lastName);
        return Sha256.hex(region + "|" + normalizedTelephone + "|" + normalizedEmail + "|" + soundex);
    }
}
