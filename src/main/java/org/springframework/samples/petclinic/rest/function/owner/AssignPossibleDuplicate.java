package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (non-hard) duplicate. The new owner is created regardless, but when it is not an exact
 * identity match yet shares an existing owner's last name and postcode while having a different
 * telephone, it is recorded as a possible duplicate of that owner.
 *
 * <p>A <em>declared</em> household member — a request that set {@code sharesHousehold} to join a known
 * household — is never a suspected duplicate: it was accepted deliberately, so this step clears the
 * flag and returns. (Without {@code sharesHousehold}, a second owner sharing the household is instead
 * rejected outright by {@link EnsureHouseholdUnique}, so it never reaches this step.)
 *
 * <p>Otherwise sets {@code possibleDuplicate} true and {@code possibleDuplicateOf} to the matching
 * owner's id (the lowest-id match when several qualify, so the value is deterministic); otherwise
 * {@code possibleDuplicate} is set false and {@code possibleDuplicateOf} left null. Hard duplicates —
 * an exact identity-key match — never reach here: they are rejected earlier by
 * {@link EnsureOwnerIdentityUnique}.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} (so telephones compare in canonical form) and after
 * {@link BuildOwner} (so the entity, its last name and postcode exist), and before {@link SaveOwner}
 * so the flag is persisted with the new row and read back by {@code GET /api/owners/{id}}.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // a declared household member is not a suspected duplicate
        }
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();

        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                    continue;
                }
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // a soft-deleted owner is treated as absent
                }
                if (equalsIgnoreCase(lastName, existing.getLastName())
                        && postcode.equals(existing.getPostcode())
                        && !equals(telephone, existing.getTelephone())) {
                    if (matchId == null || existing.getId() < matchId) {
                        matchId = existing.getId();
                    }
                }
            }
        }

        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
