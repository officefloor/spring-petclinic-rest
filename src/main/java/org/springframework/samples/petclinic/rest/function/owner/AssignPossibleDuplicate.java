package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (non-hard) duplicate. The new owner is created regardless, but when its
 * {@code identityKey} differs from an existing owner's yet they share a surname — the same
 * {@link Soundex} code for the last name — and the same postcode, it is recorded as a possible
 * duplicate of that owner.
 *
 * <p>A <em>declared</em> household member — a request that set {@code sharesHousehold} — is never a
 * suspected duplicate: it was accepted deliberately, so this step clears the flag and returns.
 *
 * <p>Otherwise sets {@code possibleDuplicate} true and {@code possibleDuplicateOf} to the matching
 * owner's id (the lowest-id match when several qualify, so the value is deterministic); otherwise
 * {@code possibleDuplicate} is set false and {@code possibleDuplicateOf} left null. Hard duplicates —
 * an exact identity-key match — never reach here: they are rejected earlier by
 * {@link EnsureOwnerIdentityUnique}.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone}/{@link NormalizeOwnerEmail} (so the identity key
 * compares in canonical form) and after {@link BuildOwner} (so the entity, its last name and postcode
 * exist), and before {@link SaveOwner} so the flag is persisted with the new row and read back by
 * {@code GET /api/owners/{id}}.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // a declared household member is not a suspected duplicate
        }
        String surname = Soundex.encode(owner.getLastName());
        String postcode = owner.getPostcode();
        String identityKey = owner.getIdentityKey();

        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                    continue;
                }
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // a soft-deleted owner is treated as absent
                }
                if (identityKey.equals(existing.getIdentityKey())) {
                    continue; // an exact identity match is a hard duplicate, not a soft one
                }
                if (surname.equals(Soundex.encode(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())) {
                    if (matchId == null || existing.getId() < matchId) {
                        matchId = existing.getId();
                    }
                }
            }
        }

        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
