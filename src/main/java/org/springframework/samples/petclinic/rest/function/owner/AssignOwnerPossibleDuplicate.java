package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a new owner as a possible (soft) duplicate. A hard duplicate is already rejected earlier by
 * {@link CheckUniqueOwnerIdentity}; this step runs after {@link BuildOwner} and before
 * {@link SaveOwner}, so it compares against the owners already stored, not the one being created.
 * When an existing owner shares this owner's last name (case-insensitive) and postcode but has a
 * different telephone, {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to that
 * owner's id; otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} null. An
 * owner that declared it shares a household ({@code sharesHousehold} true) is never flagged — a
 * declared household member is not a suspected duplicate. Both values are persisted so they are
 * returned unchanged on later reads of the owner.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        if (postcode == null) {
            return; // no postcode to share, so never a soft-match
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsIgnoreCase(lastName, existing.getLastName())
                    && postcode.equals(existing.getPostcode())
                    && !equals(telephone, existing.getTelephone())) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
