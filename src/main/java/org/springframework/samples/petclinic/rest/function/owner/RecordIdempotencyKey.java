package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;

/**
 * Records {@code Idempotency-Key -> owner id} once the new owner is saved (so its generated id is
 * available), letting a later repeat of the same key replay this owner instead of creating a
 * duplicate. A no-op when the request carries no key.
 */
public class RecordIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            @Val Owner owner, IdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
