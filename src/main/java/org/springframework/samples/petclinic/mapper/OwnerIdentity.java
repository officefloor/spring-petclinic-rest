package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Helpers backing the owner's {@code identityKey}: the SHA-256 hex digest and the American Soundex
 * code of the last name. Kept in a plain class (rather than as {@code OwnerMapper} methods) so
 * MapStruct does not mistake these {@code String -> String} helpers for candidate property mappings.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Returns the full lower-case (64-character) SHA-256 hex digest of {@code value}'s UTF-8 bytes.
     * Deterministic, so the same input always yields the same digest.
     */
    public static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Computes the American Soundex code of {@code name}: the retained first letter followed by three
     * digits encoding the remaining consonants ({@code b,f,p,v->1}; {@code c,g,j,k,q,s,x,z->2};
     * {@code d,t->3}; {@code l->4}; {@code m,n->5}; {@code r->6}). Adjacent letters sharing a code
     * collapse to one digit (letters separated by {@code h} or {@code w} are treated as adjacent,
     * while vowels — {@code a,e,i,o,u,y} — break the run), and the result is right-padded with zeros
     * and truncated to four characters (e.g. {@code "Robert" -> "R163"}). Returns the empty string
     * when the name is {@code null} or contains no letters.
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexCode(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a single upper-case letter to its Soundex digit, returning {@code '0'} for the vowels
     * {@code A,E,I,O,U,Y} and for {@code H,W} (which the caller handles specially).
     */
    private static char soundexCode(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }
}
