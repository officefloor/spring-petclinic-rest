package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft duplicate: a create that shares an existing owner's last name and postcode
 * while carrying a <em>different</em> telephone. The owner is still created; the response
 * merely carries {@code possibleDuplicate}/{@code possibleDuplicateOf} pointing at the matched
 * owner.
 *
 * <p>A declared household member — one created with {@code sharesHousehold} — deliberately
 * shares an existing owner's last name and postcode, so it is <em>not</em> a suspected
 * duplicate and is left unflagged.
 *
 * <p>Runs after {@link BuildOwner} but before the owner is saved, so it compares against the
 * owners that existed before this create (and so never matches the owner being built), and
 * mutates the built {@link Owner} in place. When several owners match, the one with the lowest
 * id is reported, so the result is deterministic. Last names are compared case-insensitively
 * (as elsewhere, e.g. {@link AssignOwnerNamesakeCount}); postcode and telephone are compared in
 * their stored, normalized form.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String lastName = canonical(owner.getLastName());
        String postcode = trimmed(owner.getPostcode());
        String telephone = trimmed(owner.getTelephone());
        if (postcode == null) {
            return; // no postcode to share, so no soft match
        }
        Owner match = null;
        for (Owner existing : Owners.active(ownerRepository.findAll())) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && postcode.equals(trimmed(existing.getPostcode()))
                    && !Objects.equals(telephone, trimmed(existing.getTelephone()))
                    && (match == null || existing.getId() < match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    private static String canonical(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimmed(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
