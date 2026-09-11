package org.springframework.samples.petclinic.mapper;

import org.springframework.stereotype.Component;

/**
 * Derives the shared {@code householdId} that every owner in the same household resolves to. The household is keyed on
 * the owner's last name and postcode, so the identifier is a pure, deterministic function of those two fields and every
 * owner sharing them resolves to the same value automatically, regardless of creation order. Kept here (rather than in
 * {@link OwnerMapper} or the REST controller) mirrors {@link IdentityKeyResolver}, {@link LocalityResolver} and
 * {@link MembershipLevelResolver} and gives household derivation a single home.
 */
@Component
public class HouseholdResolver {

    /**
     * Derives a household's stable identifier: the first 12 hex characters of the SHA-256 digest of the normalized
     * last name, a {@code '|'} separator and the postcode. The last name is normalized by trimming it, collapsing every
     * run of whitespace to a single space and lower-casing it; the postcode is used as given (an empty string when
     * none). Being a pure function of the last name and postcode, it is identical for every owner in the same household
     * and never changes over time.
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode, may be {@code null}
     * @return the household identifier
     */
    public String deriveHouseholdId(String lastName, String postcode) {
        String key = collapseWhitespace(lastName).toLowerCase() + "|" + (postcode == null ? "" : postcode);
        return HexDigest.upperHexPrefix(key, 12);
    }

    /**
     * Canonicalizes a value for household comparison by trimming it and collapsing every run of whitespace to a single
     * space. Case is deliberately preserved here; {@link #deriveHouseholdId} lower-cases the result itself.
     *
     * @param value the raw field value, may be {@code null}
     * @return the whitespace-collapsed value, or an empty string when {@code null}
     */
    public String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ");
    }
}
