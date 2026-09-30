package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Records the just-saved owner against the request's {@code Idempotency-Key}, so a later create
 * repeating that key returns this owner instead of creating a duplicate. Runs after
 * {@link SaveOwner} (so the id is populated); a request without a key is a no-op.
 */
public class RecordOwnerIdempotency {

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Val Owner owner, IdempotencyStore idempotencyStore) {
        idempotencyStore.remember(idempotencyKey, owner.getId());
    }
}
