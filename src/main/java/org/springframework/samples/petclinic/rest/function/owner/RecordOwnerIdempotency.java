package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.idempotency.OwnerIdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Remembers the owner just created against the request's {@code Idempotency-Key}, so a later
 * repeat with the same key is served the original owner by {@link CheckOwnerIdempotency}
 * instead of creating a duplicate. Runs after {@link SaveOwner} has assigned the id; a request
 * without the header records nothing.
 */
public class RecordOwnerIdempotency {

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Val Owner owner, OwnerIdempotencyStore store) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
