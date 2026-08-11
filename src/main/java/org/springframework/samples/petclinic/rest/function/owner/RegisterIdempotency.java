package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the request's {@code Idempotency-Key} (when present) against the id of the owner just
 * saved, so a later create repeating the same key returns this owner instead of creating a
 * duplicate. Runs after {@link SaveOwner} so the generated id is available.
 */
public class RegisterIdempotency {

    public void service(@Val Owner owner, ServerHttpConnection connection, IdempotencyStore store) {
        store.record(CheckIdempotencyKey.header(connection), owner.getId());
    }
}
