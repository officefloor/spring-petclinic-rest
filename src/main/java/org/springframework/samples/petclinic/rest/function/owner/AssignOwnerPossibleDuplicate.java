package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft duplicate: a create that shares an existing owner's surname Soundex and postcode
 * but carries a <em>different</em> {@link OwnerIdentity identity key} — i.e. a different
 * telephone or email. (An identical identity key is a hard duplicate and was already rejected
 * with a 409 by {@link RejectDuplicateOwnerIdentity}, so it never reaches here.) The owner is
 * still created; the response merely carries {@code possibleDuplicate}/{@code possibleDuplicateOf}
 * pointing at the matched owner.
 *
 * <p>A declared household member — one created with {@code sharesHousehold} — deliberately
 * shares an existing owner's surname and postcode, so it is <em>not</em> a suspected duplicate
 * and is left unflagged.
 *
 * <p>Runs after {@link BuildOwner} but before the owner is saved, so it compares against the
 * owners that existed before this create (and so never matches the owner being built), and
 * mutates the built {@link Owner} in place. When several owners match, the one with the lowest
 * id is reported, so the result is deterministic. Surnames are compared by their {@link Soundex}
 * code; postcode is compared in its stored, normalized form.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = trimmed(owner.getPostcode());
        if (postcode == null) {
            return; // no postcode to share, so no soft match
        }
        String soundex = Soundex.encode(owner.getLastName());
        String identityKey = OwnerIdentity.of(owner);
        Owner match = null;
        for (Owner existing : Owners.active(ownerRepository.findAll())) {
            if (soundex.equals(Soundex.encode(existing.getLastName()))
                    && postcode.equals(trimmed(existing.getPostcode()))
                    && !identityKey.equals(OwnerIdentity.of(existing))
                    && (match == null || existing.getId() < match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static String trimmed(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
