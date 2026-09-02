package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of the create pipeline. Reads the optional {@code Idempotency-Key} header and publishes
 * it for the recording step. If the key has already produced an owner, replays that owner (200) via
 * {@link IdempotentReplayException} instead of running the create; otherwise the pipeline continues.
 */
public class CheckIdempotencyKey {

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            Out<IdempotencyToken> token) throws IdempotentReplayException {
        HttpHeader header = connection.getRequest().getHeaders().getHeader("Idempotency-Key");
        String key = (header == null) ? null : header.getValue();
        token.set(new IdempotencyToken(key));
        OwnerDto existing = store.find(key);
        if (existing != null) {
            throw new IdempotentReplayException(existing);
        }
    }
}
