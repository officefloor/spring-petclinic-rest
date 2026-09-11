package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.EmailNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;
import org.springframework.stereotype.Component;

/**
 * Derives the owner-identity values matching keys off: the owner's {@code customerCode} and the single
 * {@code identityKey} that all duplicate detection is expressed through. The shared household id lives alongside in
 * {@link HouseholdResolver}; this resolver folds that id into the identity key but does not derive it. Keeping this
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
        String hash8 = HexDigest.upperHexPrefix(telephone + owner.getLastName(), 8);
        return regionCode + "-" + hash8;
    }
}
