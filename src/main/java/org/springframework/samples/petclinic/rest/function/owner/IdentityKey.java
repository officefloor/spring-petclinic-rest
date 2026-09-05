package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Derives an owner's {@code identityKey}: the single value all duplicate detection is now expressed
 * through. It is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the
 * telephone is normalized to E.164 the same way as {@link NormalizeTelephone}/{@link TelephoneE164},
 * the email is trimmed and lower-cased (empty when absent or blank, the same form as
 * {@link NormalizeEmail}), and the household component is {@link HouseholdId} over the last name and
 * address. Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones derive different identityKeys.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email, String lastName, String address) {
        String normalizedTelephone = TelephoneE164.normalizeOrNull(telephone);
        if (normalizedTelephone == null) {
            normalizedTelephone = telephone == null ? "" : telephone.trim();
        }
        String normalizedEmail = (email == null || email.isBlank())
                ? "" : email.trim().toLowerCase(Locale.ROOT);
        String householdId = HouseholdId.of(lastName, address);
        return normalizedTelephone + "|" + normalizedEmail + "|" + householdId;
    }
}
