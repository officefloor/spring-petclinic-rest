package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Canonical household identity shared by the create pipeline. A household is keyed on
 * <em>last name plus postcode</em>: the last name is compared case-insensitively with runs of
 * whitespace collapsed to a single space and outer whitespace trimmed, and the postcode is compared
 * verbatim. Owners with equal keys are the same household and therefore share the same
 * {@link #id(String, String) household id}, which is a pure, stable function of those two fields — so
 * it is stamped on every owner at creation ({@link AssignHousehold}) rather than looked up.
 *
 * <p>The id feeds the rules that key off the household: the household-size count
 * ({@link CountHousehold}) and the membership-level ceiling ({@link CapMembershipLevel}) decide
 * membership with {@link #belongsTo(Owner, String)}. (Duplicate detection and the possible-duplicate
 * flag instead key off the {@link IdentityKey}.) Not a pipeline step, so it is free to expose plain
 * helpers.
 */
public final class HouseholdNormalizer {

    private HouseholdNormalizer() {
    }

    /**
     * The stable household identifier for an owner: the first 12 hex characters of the SHA-256 of the
     * household {@link #key(String, String) key}. Owners with equal keys share the same id, and the id
     * never changes for a given (last name, postcode), so it can be recomputed rather than stored.
     */
    public static String id(String lastName, String postcode) {
        return Sha256.prefix(key(lastName, postcode), 12);
    }

    /**
     * Builds the canonical household key: the normalized last name and the postcode joined by '|'.
     * Two owners belong to the same household when their keys are equal. Either field may be
     * {@code null}, which normalizes to an empty token.
     */
    public static String key(String lastName, String postcode) {
        return normalizeName(lastName) + "|" + orEmpty(postcode);
    }

    /**
     * Whether {@code owner} belongs to the household identified by {@code householdId}, i.e. its own
     * (last name, postcode) hashes to that id. {@code false} when {@code householdId} is {@code null}.
     */
    public static boolean belongsTo(Owner owner, String householdId) {
        return householdId != null && householdId.equals(id(owner.getLastName(), owner.getPostcode()));
    }

    /** Lower-cases and collapses whitespace to single spaces, trimming the ends; {@code null} → "". */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
