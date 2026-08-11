package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners}: flags a soft match. The new owner has already cleared the hard
 * duplicate check ({@link RequireUniqueIdentity}), so it is not a full identity duplicate. When it
 * nonetheless shares an existing owner's last name (compared case-insensitively) and postcode while
 * carrying a different telephone, it is still created but recorded as a possible duplicate: its
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the matching owner's id
 * (the earliest-created match when there is more than one). Otherwise {@code possibleDuplicate} is
 * set false and {@code possibleDuplicateOf} left null.
 *
 * <p>A <em>declared</em> household member &mdash; one that opted in with {@code sharesHousehold} to
 * join an existing household (same last name and postcode) rather than be rejected as a household
 * duplicate &mdash; is never a suspected duplicate: its shared last name and postcode are exactly
 * what it declared, so it is left unflagged.
 *
 * <p>Runs after {@link BuildOwner} (so it can mutate the built owner in place) and before
 * {@link SaveOwner}, under the write transaction. A new owner with no postcode never soft-matches.
 */
public class FlagPossibleDuplicate {

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
        String lastName = normalize(owner.getLastName());
        String telephone = owner.getTelephone();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (!lastName.equals(normalize(existing.getLastName()))
                    || !postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (telephone != null && telephone.equals(existing.getTelephone())) {
                continue; // same telephone is not a soft match
            }
            if (match == null || existing.getId() < match.getId()) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
