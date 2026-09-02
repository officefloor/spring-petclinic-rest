package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.escalation.IdempotentCreateException;

/**
 * First create step: when the request's {@code Idempotency-Key} has already produced an owner,
 * escalate to return that owner (200) rather than build and save a duplicate.
 */
public class CheckIdempotentCreate {

    public void service(ServerHttpConnection connection) throws IdempotentCreateException {
        Integer existingId = IdempotencyStore.find(IdempotencyStore.keyOf(connection));
        if (existingId != null) {
            throw new IdempotentCreateException(existingId);
        }
    }
}
