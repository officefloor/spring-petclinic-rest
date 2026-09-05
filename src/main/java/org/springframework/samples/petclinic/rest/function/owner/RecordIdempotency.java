package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Remembers the owner just created against its {@code Idempotency-Key}, so a later create with
 * the same key replays it. Does nothing when the request carried no key.
 */
public class RecordIdempotency {

    public void service(@Val String idempotencyKey, @Val Owner owner, IdempotencyKeys keys) {
        if (idempotencyKey != null) {
            keys.record(idempotencyKey, owner.getId());
        }
    }
}
