package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.util.StringUtils;

/**
 * Flags the newly built owner as a possible (soft) duplicate of an existing one. A soft match is an
 * existing owner whose {@link Owner#getIdentityKey() identity key} differs — so it is not a hard
 * duplicate — yet whose last name sounds the same ({@link Soundex#encode(String) soundex}) and whose
 * postcode is identical. Since the telephone and email are part of the identity key, a household
 * member reachable on a different telephone (or email) is exactly such a case. When one is found the
 * earliest matching owner's id is recorded via {@link Owner#setPossibleDuplicateOf(Integer)}, which
 * drives the {@code possibleDuplicate} / {@code possibleDuplicateOf} response fields.
 *
 * <p>A {@link DeclaredHouseholdMember declared household member} — one admitted into an occupied
 * household via {@code sharesHousehold} — is deliberate, not suspected, so it is never flagged.
 * Owners without a postcode belong to no household and never soft-match. Deleted owners are ignored,
 * as in the identity check.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} and before {@link SaveOwner}, so the flag is persisted
 * with the owner.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val DeclaredHouseholdMember declaredMember,
            OwnerRepository ownerRepository) {
        if (declaredMember != null) {
            return;
        }
        String postcode = owner.getPostcode();
        if (!StringUtils.hasText(postcode)) {
            return;
        }
        String identityKey = owner.getIdentityKey();
        String soundex = Soundex.encode(owner.getLastName());
        ownerRepository.findAll().stream()
            .filter(existing -> !existing.isDeleted()
                && !identityKey.equals(existing.getIdentityKey())
                && postcode.equals(existing.getPostcode())
                && soundex.equals(Soundex.encode(existing.getLastName())))
            .map(Owner::getId)
            .filter(id -> id != null)
            .min(Integer::compareTo)
            .ifPresent(owner::setPossibleDuplicateOf);
    }
}
