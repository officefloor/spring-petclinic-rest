package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The identity of an owner's household, keyed on its {@code lastName} and {@code postcode}. The last
 * name is normalized (surrounding whitespace stripped, internal runs collapsed to a single space,
 * lower-cased) and the postcode is stripped, so incidental casing or spacing does not split a
 * household. Two owners belong to the same household when their {@link #householdId()} values are equal.
 *
 * <p>The {@code householdId} is <em>deterministic</em>: the first 12 hex characters of
 * {@code SHA-256(normalizedLastName + '|' + postcode)}. Owners with the same last name and postcode
 * therefore share it automatically, with no bookkeeping or minting — there is no longer any
 * donor-owner hand-off. An owner with no postcode has no household to key on, so its
 * {@code householdId} is {@code null}.
 *
 * <p>This is the single definition of "same household" shared by {@link AssignHousehold} (which stamps
 * the computed id onto the new owner) and {@link CheckOwnerHouseholdUnique} (which rejects a second,
 * undeclared member of the same household). Changing the keying rule — e.g. including city or
 * normalizing the postcode differently — happens here, once.
 */
public record OwnerHousehold(String lastName, String postcode) {

    public OwnerHousehold {
        lastName = normalizeLastName(lastName);
        postcode = postcode == null ? "" : postcode.strip();
    }

    public static OwnerHousehold of(Owner owner) {
        return new OwnerHousehold(owner.getLastName(), owner.getPostcode());
    }

    /**
     * The deterministic household identifier: the first 12 hex characters of
     * {@code SHA-256(normalizedLastName + '|' + postcode)}, or {@code null} when there is no postcode
     * to key the household on.
     */
    public String householdId() {
        if (postcode.isEmpty()) {
            return null;
        }
        return sha256hex(lastName + "|" + postcode).substring(0, 12);
    }

    /** Lower-cases and collapses whitespace so the household key ignores case and incidental spacing. */
    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
