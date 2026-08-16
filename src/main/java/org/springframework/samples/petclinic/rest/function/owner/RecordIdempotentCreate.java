package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Last step before responding: when the request carried an {@code Idempotency-Key}, records the
 * key against the just-created owner id so a later repeat with the same key replays this owner
 * (see {@link RequireIdempotentCreate}) instead of creating a duplicate. Runs after
 * {@link SaveOwner}, so the owner has its persisted id. No key means nothing to record.
 */
public class RecordIdempotentCreate {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
