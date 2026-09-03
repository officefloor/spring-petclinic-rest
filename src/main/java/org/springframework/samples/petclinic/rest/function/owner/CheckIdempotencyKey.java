package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * Reads the optional {@code Idempotency-Key} header and publishes it for {@link RecordIdempotencyKey}.
 * When the key was already used to create an owner, replays that owner with 200 (via escalation)
 * instead of running the create again. An empty string stands for "no key present".
 */
public class CheckIdempotencyKey {

    public void service(ServerHttpConnection connection, IdempotencyStore store, Out<String> keyOut)
            throws IdempotentReplayException {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = header == null ? "" : header.getValue();
        keyOut.set(key);
        if (!key.isEmpty()) {
            OwnerDto existing = store.find(key);
            if (existing != null) {
                throw new IdempotentReplayException(existing);
            }
        }
    }
}
