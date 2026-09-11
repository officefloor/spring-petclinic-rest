package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.stereotype.Component;

/**
 * Derives the owner-identity values matching keys off: the owner's {@code customerCode} and the single
 * {@code identityKey} that all duplicate detection is expressed through. The identity key is the SHA-256 of the
 * owner's normalized telephone, email and last-name Soundex (see {@link SoundexResolver}). Keeping this
 * here (rather than in {@link OwnerMapper} or the REST controller) mirrors {@link HouseholdResolver},
 * {@link LocalityResolver} and {@link MembershipLevelResolver} and gives owner-identity derivation a single home.
 * Unlike the static utilities it collaborates with the field normalizers, so it is a Spring component.
 */
@Component
public class IdentityKeyResolver {

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    public IdentityKeyResolver(TelephoneNormalizer telephoneNormalizer,
                               EmailNormalizer emailNormalizer) {
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
    }

    /**
     * Derives the owner's {@code identityKey}, the single value all duplicate detection is expressed through. It is the
     * full lower-case SHA-256 hex digest (64 characters) of {@code <normalizedTelephone>|<lowerEmail>|<soundex(lastName)>},
     * joining the canonical E.164 telephone (see {@link TelephoneNormalizer#canonicalize}), the normalized email (see
     * {@link EmailNormalizer#normalize}, empty when the owner has none) and the American Soundex of the owner's last
     * name (see {@link SoundexResolver#soundex}). Because the telephone is part of the key, two owners that differ only
     * in telephone — such as two members of the same household — have different keys; only owners whose whole key
     * matches are duplicates.
     *
     * @param owner the owner whose identity key should be derived
     * @return the owner's identity key
     */
    public String deriveIdentityKey(Owner owner) {
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        String email = emailNormalizer.normalize(owner.getEmail());
        String lowerEmail = (email == null || email.isBlank()) ? "" : email.toLowerCase();
        String lastNameSoundex = SoundexResolver.soundex(owner.getLastName());
        return HexDigest.lowerHex(telephone + "|" + lowerEmail + "|" + lastNameSoundex);
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
        return regionCode(owner) + "-" + hash8(owner);
    }

    /**
     * Returns the {@code REGION} segment of the owner's identifier: the region code derived from the owner's postcode
     * (see {@link Region#forPostcode}), falling back to {@link LocalityResolver#UNKNOWN} when the postcode maps to no
     * known region. Factored out of {@link #deriveCustomerCode} so the region segment has a single home, shared by every
     * identifier the owner's region prefixes.
     *
     * @param owner the owner whose region segment should be derived
     * @return the region code, or {@link LocalityResolver#UNKNOWN} when the postcode maps to no known region
     */
    private String regionCode(Owner owner) {
        Region region = Region.forPostcode(owner.getPostcode());
        return region == null ? LocalityResolver.UNKNOWN : region.name();
    }

    /**
     * Returns the {@code HASH8} segment of the owner's identifier: the first 8 upper-case hex characters of the SHA-256
     * digest (see {@link HexDigest#upperHexPrefix}) of the owner's canonical telephone (see
     * {@link TelephoneNormalizer#canonicalize}) concatenated with the owner's last name. Factored out of
     * {@link #deriveCustomerCode} so the hash segment has a single home, shared by every identifier the owner's hash
     * contributes to.
     *
     * @param owner the owner whose hash segment should be derived, with its telephone already normalized
     * @return the 8-character upper-case hash segment
     */
    private String hash8(Owner owner) {
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        return HexDigest.upperHexPrefix(telephone + owner.getLastName(), 8);
    }
}
