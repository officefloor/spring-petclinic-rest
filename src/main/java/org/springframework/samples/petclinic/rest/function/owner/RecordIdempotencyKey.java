package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link SaveOwner}, once the created owner has an id: records the request's
 * {@code Idempotency-Key} against that id in the {@link IdempotencyStore}, so a later create carrying
 * the same key is answered as a replay ({@link CheckIdempotencyKey}). A no-op when no key was sent.
 */
public class RecordIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            @Val Owner owner, IdempotencyStore store) {
        store.record(idempotencyKey, owner.getId());
    }
}
