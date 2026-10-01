package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;

/**
 * The identity of an owner's household, keyed on its {@code lastName} and {@code postcode}. The last
 * name is normalized (surrounding whitespace stripped, internal runs collapsed to a single space,
 * lower-cased) and the postcode is stripped, so incidental casing or spacing does not split a
 * household. Two owners belong to the same household when their {@link #householdId()} values are equal.
 *
 * <p>The {@code householdId} is <em>deterministic</em>: the first 12 hex characters of
 * {@code SHA-256(V2 + '|' + normalizedLastName + '|' + postcode)}, where {@code V2} is the fixed
 * {@linkplain org.springframework.samples.petclinic.model.IdentityVersion#TAG identity-version tag} mixed
 * in so every version-2 id differs from the version-1 id for the same household. Owners with the same
 * last name and postcode therefore share it automatically, with no bookkeeping or minting — there is no
 * longer any donor-owner hand-off. An owner with no postcode has no household to key on, so its
 * {@code householdId} is {@code null}.
 *
 * <p>This is the single definition of the {@code householdId}, shared by {@link AssignHousehold}
 * (which stamps the computed id onto the new owner) and the household-size steps that key off it —
 * {@link CountHouseholdMembers} and {@link CapMembershipLevel}. Changing how the id is keyed — e.g.
 * including city or normalizing the last name differently — happens here, once. (The soft-duplicate
 * match in {@link CheckPossibleDuplicate} is a <em>separate</em> similarity rule on
 * {@code soundex(lastName) + postcode} and does not go through this id, though it reuses this class's
 * {@link #normalizePostcode(String)} so the postcode rule stays shared.)
 */
public record OwnerHousehold(String lastName, String postcode) {

    public OwnerHousehold {
        lastName = normalizeLastName(lastName);
        postcode = normalizePostcode(postcode);
    }

    public static OwnerHousehold of(Owner owner) {
        return new OwnerHousehold(owner.getLastName(), owner.getPostcode());
    }

    /**
     * The deterministic household identifier: the first 12 hex characters of
     * {@code SHA-256(V2 + '|' + normalizedLastName + '|' + postcode)}, or {@code null} when there is no
     * postcode to key the household on.
     */
    public String householdId() {
        if (postcode.isEmpty()) {
            return null;
        }
        return Sha256.hex(IdentityVersion.TAG + "|" + lastName + "|" + postcode).substring(0, 12);
    }

    /** Lower-cases and collapses whitespace so the household key ignores case and incidental spacing. */
    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Strips surrounding whitespace so a postcode-keyed match ignores incidental spacing; the single
     *  postcode-normalization rule, reused by {@link CheckPossibleDuplicate}. A {@code null} becomes "". */
    static String normalizePostcode(String value) {
        return value == null ? "" : value.strip();
    }
}
