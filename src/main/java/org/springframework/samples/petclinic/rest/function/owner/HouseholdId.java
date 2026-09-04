package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the stable identifier shared by every owner in the same household. A household
 * is the set of owners with the same last name and postcode: the first 12 hex characters of
 * SHA-256 over {@code normalizedLastName + '|' + postcode} (last name normalized
 * case-insensitively with collapsed whitespace, matching {@link EnsureOwnerHouseholdUnique}),
 * so joiners created with {@code sharesHousehold} always resolve to the same value.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String seed = "V2|" + key(owner.getLastName()) + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
