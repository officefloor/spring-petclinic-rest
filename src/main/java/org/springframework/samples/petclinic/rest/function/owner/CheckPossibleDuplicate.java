package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Soft-duplicate flagging step of {@code POST /api/owners}. Runs after {@link CheckOwnerIdentityUnique}
 * (so the new owner is already known not to be a hard, {@code identityKey} duplicate) and before
 * {@link SaveOwner}.
 *
 * <p>A new owner is a <em>possible</em> duplicate when, though its {@code identityKey} differs from an
 * existing owner's (so it is not a hard duplicate), it shares that owner's surname sound and postcode:
 * {@link Soundex} of the {@code lastName} is equal and the postcode is equal. This is what makes two
 * owners with the same last name and postcode but different telephones a soft match rather than a hard
 * household duplicate — their telephones (part of the {@code identityKey}) differ, so the keys differ,
 * but the surname sound and postcode still coincide. Such an owner is still created, but flagged:
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the matching existing
 * owner's id (the lowest id when more than one matches). When there is no match the owner is flagged
 * {@code possibleDuplicate} false. The new owner is still unsaved, so it is not among the owners
 * iterated here and cannot match itself.
 *
 * <p>A <em>declared</em> household member — one whose request set {@code sharesHousehold} — is
 * deliberately exempt: it is a known, acknowledged second member of the household, not a suspected
 * duplicate, so it is never flagged.
 */
public class CheckPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member is not a suspected duplicate
        }

        String postcode = OwnerHousehold.normalizePostcode(owner.getPostcode());
        if (postcode.isEmpty()) {
            return; // no postcode to match a household on
        }
        String identityKey = OwnerIdentity.of(owner);
        String lastNameCode = Soundex.encode(owner.getLastName());

        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                continue; // a hard-identity match is not a soft match
            }
            if (lastNameCode.equals(Soundex.encode(existing.getLastName()))
                    && postcode.equals(OwnerHousehold.normalizePostcode(existing.getPostcode()))) {
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
