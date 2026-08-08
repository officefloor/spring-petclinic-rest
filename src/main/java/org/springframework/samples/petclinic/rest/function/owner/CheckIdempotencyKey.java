package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyKey;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;

/**
 * First step of the create pipeline. Reads the optional {@code Idempotency-Key} header and
 * publishes it for the recording step. When the key was already seen, short-circuits the whole
 * pipeline by throwing {@link IdempotentReplayException} carrying the originally created owner,
 * so the request replays that owner with 200 rather than creating a duplicate.
 */
public class CheckIdempotencyKey {

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            Out<IdempotencyKey> keyOut) throws IdempotentReplayException {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = (header != null) ? header.getValue() : null;
        keyOut.set(new IdempotencyKey(key));
        if (key != null && !key.isBlank()) {
            OwnerDto existing = store.find(key);
            if (existing != null) {
                throw new IdempotentReplayException(existing);
            }
        }
    }
}
