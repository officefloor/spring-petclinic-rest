package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft (non-hard) duplicate. Runs after {@link RejectDuplicateOwnerIdentity} has
 * already rejected exact {@code identityKey} matches (409), so any owner reaching this step
 * is <em>not</em> a hard duplicate. When the new owner's {@code identityKey} nonetheless
 * differs from an existing owner's while their {@code soundex(lastName)} and {@code postcode}
 * both match, the create still proceeds and the new owner is marked as a possible duplicate:
 * its {@code possibleDuplicateOf} is set to the matching owner's id (the earliest such owner
 * when several match). This is exactly the household-look-alike case that is no longer a hard
 * duplicate: same phonetic surname and postcode but a different telephone (or email) yields a
 * different key, so it is admitted and flagged rather than rejected. Mutates the {@link Owner}
 * in place so the value is persisted by {@link SaveOwner} and surfaces as
 * {@code possibleDuplicate}/{@code possibleDuplicateOf} on the response.
 *
 * <p>A <em>declared</em> household member — one carrying {@code sharesHousehold} — is
 * deliberately never flagged: it shares the household by design, so it is not a suspected
 * duplicate.
 *
 * <p>No-ops (leaving {@code possibleDuplicateOf} null, so {@code possibleDuplicate} is
 * false) when the owner declared a shared household, has no last name or postcode, or no
 * matching existing owner exists.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = owner.getPostcode();
        if (owner.getLastName() == null || postcode == null || postcode.isBlank()) {
            return; // nothing to match on
        }
        String soundex = Soundex.encode(owner.getLastName());
        String identityKey = OwnerIdentity.identityKey(owner);
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never match the owner against itself
            }
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                continue; // an identical key is a hard, not a possible, duplicate
            }
            if (!soundex.equals(Soundex.encode(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (match == null || existing.getId() < match.getId()) {
                match = existing; // earliest matching owner
            }
        }
        if (match != null) {
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
