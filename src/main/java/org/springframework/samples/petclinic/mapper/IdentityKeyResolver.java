package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.stereotype.Component;

/**
 * Derives the stable identifiers an owner is matched on. Today that is the shared household id, computed as a pure
 * function of the household's last name and address; keeping it here (rather than in {@link OwnerMapper} or the REST
 * controller) mirrors {@link LocalityResolver} and {@link MembershipLevelResolver} and gives owner-identity derivation a
 * single home. Unlike those two it collaborates with {@link AddressNormalizer}, so it is a Spring component rather than a
 * static utility.
 */
@Component
public class IdentityKeyResolver {

    private final AddressNormalizer addressNormalizer;

    public IdentityKeyResolver(AddressNormalizer addressNormalizer) {
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
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return "HH-" + hex;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
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
