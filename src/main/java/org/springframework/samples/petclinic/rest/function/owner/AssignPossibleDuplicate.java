package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-match duplicate signal. A create request that clears the hard duplicate
 * check (see {@link EnsureUniqueIdentity}) may still resemble an existing owner: when the new owner
 * shares an existing owner's {@code soundex(lastName)} (see {@link Soundex}) and postcode while its
 * whole {@code identityKey} (see {@link OwnerIdentity}) differs — typically a different telephone —
 * it is still created but flagged with {@code possibleDuplicate} true and {@code possibleDuplicateOf}
 * set to that existing owner's id. Otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} null.
 *
 * <p>A matching identity key would already have been rejected as a hard duplicate, so requiring the
 * key to differ here simply excludes that (impossible-to-reach) case and keeps the soft match to
 * genuinely distinct owners who merely sound like a household member.
 *
 * <p>A declared household member ({@code sharesHousehold} true) is never flagged: it shares an
 * existing member's surname and postcode by design, so it is a known household member rather than a
 * suspected duplicate.
 *
 * <p>Only owners with a postcode participate: a match requires both postcodes to be present and
 * equal. When several existing owners match, the earliest (lowest id) is reported. Soft-deleted
 * owners are ignored. Runs before {@code save}, so the new owner is not compared against itself.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // a declared household member is not a suspected duplicate
        }
        String soundex = Soundex.of(owner.getLastName());
        String postcode = owner.getPostcode();
        String identityKey = OwnerIdentity.of(owner);
        Owner match = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.isDeleted()) {
                    continue; // a soft-deleted owner is not a soft-match candidate
                }
                if (soundex.equals(Soundex.of(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())
                        && !identityKey.equals(OwnerIdentity.of(existing))) {
                    if (match == null || lessThan(existing.getId(), match.getId())) {
                        match = existing;
                    }
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }

    private static boolean lessThan(Integer a, Integer b) {
        if (a == null) {
            return false;
        }
        if (b == null) {
            return true;
        }
        return a < b;
    }
}
