package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a successful create, remember the new owner's id under the request's
 * {@code Idempotency-Key} so a later repeat returns this owner instead of a duplicate.
 */
public class RecordIdempotentCreate {

    public void service(ServerHttpConnection connection, @Val Owner owner) {
        IdempotencyStore.record(IdempotencyStore.keyOf(connection), owner.getId());
    }
}
