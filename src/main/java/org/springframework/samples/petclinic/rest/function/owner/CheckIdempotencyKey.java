package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of create-owner: when the request repeats an already-seen {@code Idempotency-Key},
 * escalate {@link IdempotentReplayException} so the original owner is replayed with 200; otherwise
 * fall through to the normal create pipeline.
 */
public class CheckIdempotencyKey {

    public void service(ServerHttpConnection connection, IdempotencyStore store)
            throws IdempotentReplayException {
        String key = IdempotencyKey.read(connection);
        if (key == null) {
            return;
        }
        OwnerDto existing = store.find(key);
        if (existing != null) {
            throw new IdempotentReplayException(existing);
        }
    }
}
