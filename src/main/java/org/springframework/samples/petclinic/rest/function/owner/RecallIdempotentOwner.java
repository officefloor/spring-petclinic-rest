package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of {@code POST /api/owners}: the idempotency gate. Reads the optional
 * {@code Idempotency-Key} header; if that key already produced an owner, the create is a repeat —
 * it escalates to {@link IdempotentReplayException} so the original owner is returned unchanged,
 * before any validation or the duplicate-identity check can run. Otherwise it publishes the key
 * for {@link RecordIdempotentOwner} and lets the create proceed.
 */
public class RecallIdempotentOwner {

    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    public void service(ServerHttpConnection connection, IdempotencyStore idempotencyStore,
            Out<String> idempotencyKey) throws IdempotentReplayException {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(IDEMPOTENCY_KEY);
        String key = header == null ? null : header.getValue();
        Integer existing = idempotencyStore.recall(key);
        if (existing != null) {
            throw new IdempotentReplayException(existing);
        }
        idempotencyKey.set(key);
    }
}
