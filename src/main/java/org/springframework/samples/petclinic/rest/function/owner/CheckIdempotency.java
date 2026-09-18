package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Optional;

import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of create-owner: when the request carries an {@code Idempotency-Key} that an earlier
 * create already used, short-circuits the pipeline by throwing {@link IdempotentReplayException}
 * with the original owner's id, so the duplicate is never validated, built or persisted. A new or
 * absent key simply lets the pipeline proceed to {@link ValidateRequiredOwnerFields}.
 */
public class CheckIdempotency {

    public void service(ServerHttpConnection connection, IdempotencyStore idempotencyStore)
            throws IdempotentReplayException {
        Optional<Integer> existingOwnerId = IdempotencyStore.keyOf(connection).flatMap(idempotencyStore::find);
        if (existingOwnerId.isPresent()) {
            throw new IdempotentReplayException(existingOwnerId.get());
        }
    }
}
