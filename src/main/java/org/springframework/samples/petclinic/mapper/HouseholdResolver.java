package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.stereotype.Component;

/**
 * Derives the shared {@code householdId} that every owner in the same household resolves to, and the name
 * normalization household matching is expressed through. Kept here (rather than in {@link OwnerMapper} or the REST
 * controller) mirrors {@link IdentityKeyResolver}, {@link LocalityResolver} and {@link MembershipLevelResolver} and
 * gives household derivation a single home. Like {@link IdentityKeyResolver} it collaborates with a field normalizer,
 * so it is a Spring component rather than a static utility.
 */
@Component
public class HouseholdResolver {

    private final AddressNormalizer addressNormalizer;

    public HouseholdResolver(AddressNormalizer addressNormalizer) {
        this.addressNormalizer = addressNormalizer;
    }

    /**
     * Derives a household's stable identifier, formatted {@code HH-<12 hex chars>}, from the case-insensitive,
     * whitespace-collapsed last name and the normalized address (see {@link AddressNormalizer}). Being a pure function
     * of those fields, it is identical for every owner in the same household and never changes over time.
     *
     * @param lastName the household's last name
     * @param address  the household's address
     * @return the household identifier
     */
    public String deriveHouseholdId(String lastName, String address) {
        String key = collapseWhitespace(lastName).toLowerCase()
            + "\n" + addressNormalizer.normalize(address).toLowerCase();
        return "HH-" + HexDigest.upperHexPrefix(key, 12);
    }

    /**
     * Canonicalizes a value for household comparison by trimming it and collapsing every run of whitespace to a single
     * space. Case is deliberately preserved here; callers apply their own case handling ({@link #deriveHouseholdId}
     * lower-cases the result, while household matching compares it case-insensitively).
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
