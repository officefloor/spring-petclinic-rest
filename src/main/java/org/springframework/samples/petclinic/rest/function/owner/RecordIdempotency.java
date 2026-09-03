package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Records the {@code Idempotency-Key} against the just-created owner, so a later create with the same
 * key returns this owner instead of creating a duplicate (see {@link CheckIdempotency}). Runs after
 * {@link SaveOwner}, once the owner's id is assigned. A request without a key records nothing.
 */
public class RecordIdempotency {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey, @Val Owner owner,
            IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
