package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners}: flags a soft match. The new owner has already cleared the hard
 * duplicate check ({@link RequireUniqueIdentity}), so its {@code identityKey} is not shared by any
 * existing owner. When it nonetheless shares an existing owner's {@code soundex(lastName)} and
 * postcode &mdash; a different identity key, but a phonetically identical surname at the same
 * postcode &mdash; it is still created but recorded as a possible duplicate: its
 * {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the matching owner's id
 * (the earliest-created match when there is more than one). Otherwise {@code possibleDuplicate} is
 * set false and {@code possibleDuplicateOf} left null.
 *
 * <p>Because the telephone is part of the identity key, two owners sharing a last name and postcode
 * but with different telephones now have different keys: they are no longer a hard household
 * duplicate but are surfaced here as a soft match.
 *
 * <p>A <em>declared</em> household member &mdash; one that opted in with {@code sharesHousehold} to
 * join an existing household (same last name and postcode) &mdash; is never a suspected duplicate:
 * its shared last name and postcode are exactly what it declared, so it is left unflagged.
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
        String soundex = OwnerIdentity.soundex(owner.getLastName());
        String identityKey = OwnerIdentity.key(Locality.of(owner.getCity(), owner.getPostcode()),
                owner.getTelephone(), owner.getEmail(), owner.getLastName());
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is not a duplicate to flag against
            }
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never match the new owner against itself
            }
            if (!postcode.equals(existing.getPostcode())
                    || !soundex.equals(OwnerIdentity.soundex(existing.getLastName()))) {
                continue;
            }
            String existingKey = OwnerIdentity.key(
                    Locality.of(existing.getCity(), existing.getPostcode()),
                    existing.getTelephone(), existing.getEmail(), existing.getLastName());
            if (identityKey.equals(existingKey)) {
                continue; // an identical identity key is a hard duplicate, not a soft match
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
}
