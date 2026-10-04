package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Soft-duplicate detection for {@code POST /owners}. A request that cleared the hard-duplicate
 * guard ({@link CheckOwnerIdentityUnique}) is still created, but is flagged as a <em>possible</em>
 * duplicate when its surname's Soundex ({@link OwnerSoundex}) and its {@code postcode} match an
 * existing owner's while its whole {@link OwnerIdentity identityKey} <em>differs</em>. The matching
 * owner's id is recorded in {@code possibleDuplicateOf}; when there is no such match both
 * {@code possibleDuplicate} is {@code false} and {@code possibleDuplicateOf} is {@code null}.
 *
 * <p>A <em>declared</em> household member — one created with {@code sharesHousehold} true, which
 * deliberately shares an existing owner's household — is never flagged: a declared member is not a
 * suspected duplicate. A non-declared owner with a surname that sounds like an existing owner's and
 * the same postcode but a different telephone (so a different identity key) is no longer rejected by
 * {@link CheckOwnerIdentityUnique} — it reaches this step and is flagged as a possible duplicate of
 * the matching owner.
 *
 * <p>Runs within the create transaction after {@link BuildOwner} (so the built owner's telephone is
 * already in its stored E.164 form) and before {@link SaveOwner} (so {@code findAll} excludes the
 * owner being created). When several existing owners match, the one with the lowest id is recorded
 * so the result is deterministic. A request without a postcode cannot share one, so it is never a
 * possible duplicate.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }

        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String lastNameSoundex = OwnerSoundex.of(owner.getLastName());
        String identityKey = OwnerIdentity.of(owner);

        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a possible duplicate
            }
            boolean sameSoundex = lastNameSoundex.equals(OwnerSoundex.of(existing.getLastName()));
            boolean samePostcode = postcode.equals(existing.getPostcode());
            boolean differentIdentity = !identityKey.equals(OwnerIdentity.of(existing));
            if (sameSoundex && samePostcode && differentIdentity) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
