package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.stereotype.Component;

/**
 * Derives the owner-identity values matching keys off: the owner's {@code memberId} and the single
 * {@code identityKey} that all duplicate detection is expressed through. The identity key is the SHA-256 of the
 * owner's normalized telephone, email and last-name Soundex (see {@link SoundexResolver}). Keeping this
 * here (rather than in {@link OwnerMapper} or the REST controller) mirrors {@link HouseholdResolver},
 * {@link LocalityResolver} and {@link MembershipLevelResolver} and gives owner-identity derivation a single home.
 * Unlike the static utilities it collaborates with the field normalizers, so it is a Spring component.
 */
@Component
public class IdentityKeyResolver {

    /**
     * The fixed version tag mixed into every derived owner identifier by the version-2 owner identity. It is folded into
     * the region segment of the {@code memberId} and into the hashed input of the {@code memberId}'s HASH8 segment and
     * the {@code identityKey} (and, sharing this same tag, the {@code householdId}; see
     * {@link HouseholdResolver#deriveHouseholdId}), so every identifier changes and no value produced under version 1 is
     * produced again. It is deliberately absent from the user-facing {@code locality} (see
     * {@link LocalityResolver#deriveLocality}), which stays the plain region code.
     */
    public static final String VERSION_TAG = "V2";

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    public IdentityKeyResolver(TelephoneNormalizer telephoneNormalizer,
                               EmailNormalizer emailNormalizer) {
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
    }

    /**
     * Derives the owner's {@code identityKey}, the single value all duplicate detection is expressed through. It is the
     * full lower-case SHA-256 hex digest (64 characters) of
     * {@code <VERSION_TAG>|<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>}, mixing in the fixed version-2
     * {@link #VERSION_TAG} and joining the canonical E.164 telephone (see {@link TelephoneNormalizer#canonicalize}), the
     * normalized email (see {@link EmailNormalizer#normalize}, empty when the owner has none) and the American Soundex of
     * the owner's last name (see {@link SoundexResolver#soundex}). Because the telephone is part of the key, two owners that differ only
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
        return HexDigest.lowerHex(VERSION_TAG + "|" + telephone + "|" + lowerEmail + "|" + lastNameSoundex);
    }

    /**
     * Derives an owner's {@code memberId}, the single unified identifier formatted
     * {@code <REGION><FY><HASH8><CHK>}. {@code REGION} is the version-2 region segment: the region code derived from the
     * owner's postcode (see {@link Region#forPostcode}, falling back to {@link LocalityResolver#UNKNOWN} when the
     * postcode maps to no known region) with the fixed {@link #VERSION_TAG} mixed in; {@code FY} is the two-digit
     * fiscal-year segment of the owner's business-day-adjusted registration date
     * (see {@link FiscalYearResolver#fiscalYearSuffix}); {@code HASH8} is the first 8 upper-case hex characters of the
     * SHA-256 digest of the {@link #VERSION_TAG}, the normalized telephone (see {@link TelephoneNormalizer#canonicalize})
     * and the owner's last name; and {@code CHK} is a single Luhn check digit (see {@link CheckDigitResolver#deriveCheckDigit})
     * computed over the digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSWV2271A2B3C4D9}). Being a pure function of the
     * postcode, registration date, telephone and last name it carries no sequence number and is stable for a given owner.
     *
     * @param owner the newly mapped owner about to be saved, with its telephone already normalized and its registration
     *              date resolved
     * @return the generated member id
     */
    public String deriveMemberId(Owner owner) {
        String base = regionCode(owner) + FiscalYearResolver.fiscalYearSuffix(owner.getRegistrationDate())
            + hash8(owner);
        return base + CheckDigitResolver.deriveCheckDigit(base);
    }

    /**
     * Returns the version-2 {@code REGION} segment of the owner's identifier: the owner's plain region code (see
     * {@link LocalityResolver#regionCode}, derived from the owner's postcode, {@link LocalityResolver#UNKNOWN} when it
     * maps to no known region) with the fixed {@link #VERSION_TAG} mixed in. Factored out of {@link #deriveMemberId} so
     * the region segment has a single home, shared by every identifier the owner's region prefixes. The plain region code
     * (without the version tag) remains the owner's user-facing {@code locality} (see
     * {@link LocalityResolver#deriveLocality}), so the two are deliberately kept distinct.
     *
     * @param owner the owner whose region segment should be derived
     * @return the version-2 region segment, the plain region code (or {@link LocalityResolver#UNKNOWN}) with the version
     * tag mixed in
     */
    private String regionCode(Owner owner) {
        return LocalityResolver.regionCode(owner.getPostcode()) + VERSION_TAG;
    }

    /**
     * Returns the version-2 {@code HASH8} segment of the owner's identifier: the first 8 upper-case hex characters of the
     * SHA-256 digest (see {@link HexDigest#upperHexPrefix}) of the fixed {@link #VERSION_TAG}, the owner's canonical
     * telephone (see {@link TelephoneNormalizer#canonicalize}) and the owner's last name. Factored out of
     * {@link #deriveMemberId} so the hash segment has a single home, shared by every identifier the owner's hash
     * contributes to.
     *
     * @param owner the owner whose hash segment should be derived, with its telephone already normalized
     * @return the 8-character upper-case hash segment
     */
    private String hash8(Owner owner) {
        String telephone = telephoneNormalizer.canonicalize(owner.getTelephone());
        return HexDigest.upperHexPrefix(VERSION_TAG + telephone + owner.getLastName(), 8);
    }
}
