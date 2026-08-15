package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;

/**
 * First step of the create-owner pipeline. Reads the optional {@code Idempotency-Key} request header
 * and, when the key was already used to create an owner, short-circuits the pipeline by throwing
 * {@link IdempotentReplayException} — the handler replays the original owner with 200 instead of
 * running the validation, duplicate and insert steps again.
 *
 * <p>Otherwise it publishes the key (or a {@code null} key when the header is absent) so
 * {@link RecordIdempotencyKey} can record it against the created owner once the insert succeeds.
 */
public class CheckIdempotencyKey {

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            Out<IdempotencyKey> keyOut) throws IdempotentReplayException {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header == null ? null : header.getValue();
        Integer existingOwnerId = store.find(key);
        if (existingOwnerId != null) {
            throw new IdempotentReplayException(existingOwnerId);
        }
        keyOut.set(new IdempotencyKey(key));
    }
}
