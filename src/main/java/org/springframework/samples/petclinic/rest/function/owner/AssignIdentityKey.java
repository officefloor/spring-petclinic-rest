package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's derived identity key before it is saved, so the value returned in the
 * response matches the key used for duplicate detection. Derived via {@link OwnerIdentity} from
 * the owner's normalized telephone, email and household id. Runs after {@link BuildOwner} has
 * produced the entity and before {@link SaveOwner} persists it, mutating the built owner in place.
 */
public class AssignIdentityKey {

    public void service(@Val Owner owner) {
        owner.setIdentityKey(OwnerIdentity.of(owner));
    }
}
