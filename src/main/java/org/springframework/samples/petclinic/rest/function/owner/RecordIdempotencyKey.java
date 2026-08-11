package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.support.IdempotencyStore;

/**
 * Records the {@code Idempotency-Key} of a just-created owner so a later create repeating that key
 * returns this same owner (see {@link CheckIdempotencyKey}) instead of creating a duplicate. Runs
 * after {@link SaveOwner} — the owner now has an id — and does nothing when the request carried no
 * key. The first id recorded for a key wins.
 */
public class RecordIdempotencyKey {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyStore idempotencyStore) {
        if (idempotencyKey != null && owner.getId() != null) {
            idempotencyStore.putIfAbsent(idempotencyKey, owner.getId());
        }
    }
}
