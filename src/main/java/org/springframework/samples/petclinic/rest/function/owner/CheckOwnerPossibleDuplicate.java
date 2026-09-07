package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate. Runs after {@link CheckOwnerIdentityUnique} has already rejected hard
 * duplicates, so the owner reaching this step is not a hard duplicate. When an existing owner
 * shares the new owner's lastName (case-insensitive) and postcode but has a different telephone,
 * the new owner is still created but marked with {@link Owner#setPossibleDuplicate(boolean)} true
 * and {@link Owner#setPossibleDuplicateOf(Integer)} set to that owner's id. When more than one
 * existing owner matches, the one with the lowest id is chosen for a stable result. Otherwise
 * {@code possibleDuplicate} stays false and {@code possibleDuplicateOf} absent.
 *
 * <p>A declared household member ({@code sharesHousehold} true, which shares the new owner's
 * lastName and postcode) is deliberately NOT flagged: a declared member is not a suspected
 * duplicate.
 */
public class CheckOwnerPossibleDuplicate {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return; // a declared household member is not a suspected duplicate
        }
        String lastName = key(owner.getLastName());
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode to match on
        }
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                continue; // skip the owner being created
            }
            if (!lastName.equals(key(existing.getLastName()))) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (Objects.equals(telephone, existing.getTelephone())) {
                continue; // same telephone is a hard duplicate territory, not a soft match
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

    private static String key(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
