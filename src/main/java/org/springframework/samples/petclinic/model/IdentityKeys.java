package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Derives an owner's {@code identityKey}: the lower-case hex SHA-256 of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Two owners
 * are the same identity exactly when this key is equal, so a shared surname/postcode with
 * a different telephone yields distinct keys (a soft match, not a hard duplicate).
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The identityKey for {@code owner} (64 lower-case hex characters). */
    public static String of(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone().strip();
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        return sha256Hex("V2|" + telephone + "|" + email + "|" + soundex(owner.getLastName()));
    }

    /** The American Soundex code of {@code name}, or {@code ""} when it has no letters. */
    public static String soundex(String name) {
        String letters = name == null ? "" : name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = digitOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            char digit = digitOf(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            if (letter != 'H' && letter != 'W') {
                previous = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char digitOf(char letter) {
        return switch (letter) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
