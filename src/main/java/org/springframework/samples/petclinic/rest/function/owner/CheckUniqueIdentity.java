package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create whose whole {@code identityKey} already belongs to another owner,
 * before {@link SaveOwner} runs. Consolidates the former telephone, email and household
 * duplicate checks: only an exact full-key match is a duplicate, so two members of the
 * same household with different telephones are both allowed. Handled with 409 by
 * {@code DuplicateIdentityHandler}. Runs after {@link AssignHouseholdId} so the key can
 * include the household part.
 */
public class CheckUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String key = identityKey(owner);
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId()) && key.equals(identityKey(other))) {
                throw new DuplicateIdentityException(key);
            }
        }
    }

    /** Derived duplicate key: normalizedTelephone + '|' + (email or empty) + '|' + householdId. */
    public static String identityKey(Owner owner) {
        return blank(owner.getTelephone()) + "|" + blank(owner.getEmail()) + "|"
                + blank(owner.getHouseholdId());
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }
}
