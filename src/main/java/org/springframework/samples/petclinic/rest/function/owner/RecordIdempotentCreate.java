package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records that this request's {@code Idempotency-Key} created the just-saved owner, so a later
 * create repeating the key replays the same owner. Runs after {@link SaveOwner} (the owner's id is
 * assigned); a request without a key records nothing.
 */
public class RecordIdempotentCreate {

    public void service(@Val Owner owner, ServerHttpConnection connection, IdempotencyRegistry registry) {
        registry.record(CheckIdempotentCreate.idempotencyKey(connection), owner.getId());
    }
}
