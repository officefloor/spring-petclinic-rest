package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After the owner is saved (so its id is assigned), remembers the request's {@code Idempotency-Key}
 * against that owner id, so a later create carrying the same key replays this owner instead of
 * creating a duplicate. A request with no {@code Idempotency-Key} header is a no-op.
 */
public class RecordIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            @Val Owner owner, IdempotencyKeyRegistry registry) {
        registry.record(idempotencyKey, owner.getId());
    }
}
