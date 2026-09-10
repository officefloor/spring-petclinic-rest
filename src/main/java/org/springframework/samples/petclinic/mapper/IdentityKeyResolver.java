package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.stereotype.Component;

/**
 * Derives the stable identifiers an owner is matched on: the shared household id (a pure function of the household's
 * last name and address) and the single {@code identityKey} that all duplicate detection is expressed through. Keeping
 * this here (rather than in {@link OwnerMapper} or the REST controller) mirrors {@link LocalityResolver} and
 * {@link MembershipLevelResolver} and gives owner-identity derivation a single home. Unlike those two it collaborates
 * with the field normalizers, so it is a Spring component rather than a static utility.
 */
@Component
public class IdentityKeyResolver {

    private final AddressNormalizer addressNormalizer;

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    public IdentityKeyResolver(AddressNormalizer addressNormalizer,
                               TelephoneNormalizer telephoneNormalizer,
                               EmailNormalizer emailNormalizer) {
        this.addressNormalizer = addressNormalizer;
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
    }

    /**
     * Derives the owner's {@code identityKey}, the single value all duplicate detection is expressed through. It is
     * formatted {@code <normalizedTelephone>|<email or empty>|<householdId or empty>}, joining the canonical E.164
     * telephone (see {@link TelephoneNormalizer#canonicalize}), the normalized email (see
     * {@link EmailNormalizer#normalize}, empty when the owner has none) and the shared {@code householdId} (empty when
     * the owner belongs to no household). Because the telephone is part of the key, two owners that differ only in
     * telephone — such as two members of the same household — have different keys; only owners whose whole key matches
     * are duplicates.
     *
     * @param owner the owner whose identity key should be derived
     * @return the owner's identity key
     */
    public String deriveIdentityKey(Owner owner) {
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        String email = emailNormalizer.normalize(owner.getEmail());
        String householdId = owner.getHouseholdId();
        return telephone
            + "|" + (email == null || email.isBlank() ? "" : email)
            + "|" + (householdId == null ? "" : householdId);
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
        return "HH-" + upperHexDigest(key, 12);
    }

    /**
     * Derives an owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}. {@code REGION} is the region code
     * derived from the owner's postcode (see {@link Region#forPostcode}), falling back to {@link LocalityResolver#UNKNOWN}
     * when the postcode maps to no known region; {@code HASH8} is the first 8 upper-case hex characters of the SHA-256
     * digest of the normalized telephone (see {@link TelephoneNormalizer#canonicalize}) concatenated with the owner's
     * last name (e.g. {@code NSW-1A2B3C4D}). Being a pure function of the postcode, telephone and last name it carries
     * no sequence number and is stable for a given owner.
     *
     * @param owner the newly mapped owner about to be saved, with its telephone already normalized
     * @return the generated customer code
     */
    public String deriveCustomerCode(Owner owner) {
        Region region = Region.forPostcode(owner.getPostcode());
        String regionCode = region == null ? LocalityResolver.UNKNOWN : region.name();
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        String hash8 = upperHexDigest(telephone + owner.getLastName(), 8);
        return regionCode + "-" + hash8;
    }

    /**
     * Returns the first {@code chars} upper-case hex characters of the SHA-256 digest of the UTF-8 bytes of
     * {@code value}. Shared by {@link #deriveHouseholdId} and {@link #deriveCustomerCode} so both express the same
     * hashing in one place.
     *
     * @param value the value to hash
     * @param chars the number of leading hex characters to return
     * @return the leading upper-case hex characters of the digest
     */
    private String upperHexDigest(String value, int chars) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02X", b));
            }
            return hex.substring(0, chars);
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
