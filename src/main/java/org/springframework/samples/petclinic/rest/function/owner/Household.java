package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single source of truth for household identity. A household groups owners that share the same last
 * name and the same postcode, with the last name compared case-insensitively and with surrounding and
 * internal whitespace collapsed (so "Smith" / " smith " belong together).
 *
 * <p>The {@link #id(String, String)} derived from that normalized (lastName, postcode) pair is
 * deterministic: the same last name and postcode always map to the same value, so every owner at the
 * same household receives an identical {@code householdId} automatically — it is no longer something an
 * owner "joins" via {@code sharesHousehold}. The household groups members for membership counting and
 * level capping; duplicate detection itself is no longer keyed on the household but on the owner's
 * identity key (see {@link CheckIdentityUnique}).
 */
final class Household {

    private Household() {
    }

    /** Lower-case (locale-independent) with leading/trailing and repeated internal whitespace collapsed. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * The canonical household key for a (lastName, postcode) pair — the single definition of household
     * membership: {@code normalizedLastName + "|" + postcode}. Two owners belong to the same household
     * exactly when their keys are equal; {@link #id} hashes this same key, so the duplicate check and the
     * assigned id can never diverge. The last name is normalized (whitespace collapsed, lower-cased); the
     * postcode is trimmed and used as given (a {@code null} or blank postcode contributes an empty
     * component).
     */
    static String key(String lastName, String postcode) {
        return normalize(lastName) + "|" + normalizePostcode(postcode);
    }

    /**
     * The canonical form of a postcode for household/soft-match comparison: trimmed, or {@code ""} when
     * {@code null}. The single definition shared by {@link #key} and the soft-match
     * ({@link FlagPossibleDuplicate}) so the two cannot diverge if the rule changes.
     */
    static String normalizePostcode(String postcode) {
        return (postcode == null) ? "" : postcode.trim();
    }

    /**
     * All existing owners that belong to the household of the given last name and postcode — those whose
     * own (recomputed) {@link #id household id} equals this household's. The single definition of
     * "collect the members of a household", shared by the callers that count members and that cap a new
     * member's level; soft-deleted owners are included (callers that must exclude them filter the result).
     */
    static List<Owner> membersOf(String lastName, String postcode, OwnerRepository repository) {
        String householdId = id(lastName, postcode);
        List<Owner> members = new ArrayList<>();
        for (Owner existing : repository.findAll()) {
            if (householdId.equals(id(existing.getLastName(), existing.getPostcode()))) {
                members.add(existing);
            }
        }
        return members;
    }

    /**
     * Deterministic household identifier for the given last name and postcode: {@code "H-"} followed by
     * the first 12 hex characters of the SHA-256 of the fixed {@link Owner#IDENTITY_VERSION_TAG version tag}
     * mixed with the {@link #key household key}. The leading tag distinguishes this v2 id from its v1 form
     * (which hashed the key alone), so no v1 household id is produced again; because every owner's id is
     * recomputed through here, the household grouping stays self-consistent.
     */
    static String id(String lastName, String postcode) {
        return "H-" + Sha256.hex(Owner.IDENTITY_VERSION_TAG + "|" + key(lastName, postcode)).substring(0, 12);
    }
}
