package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * First step of {@code POST /api/owners}. Reads the optional {@code Idempotency-Key} request header and
 * makes a create idempotent: if a key that has already produced an owner is replayed, the create is
 * short-circuited via {@link IdempotentReplayException} (handled with a 200 returning the originally
 * created owner) instead of running the pipeline and creating a duplicate (which would otherwise 409).
 *
 * <p>Runs before {@link ValidateOwner} and before any governance begins, so a replay does no work and
 * touches no transaction. The header is optional — when absent the create proceeds normally. The key
 * (or {@code null}) is published for {@link RecordIdempotency}, which remembers it once the owner has
 * been persisted. This step does not bind the request body, so {@link ValidateOwner} remains the single
 * body-reading step.
 */
public class CheckIdempotency {

    static final String HEADER = "Idempotency-Key";

    public void service(ServerHttpConnection connection, IdempotencyStore store, Out<String> idempotencyKey)
            throws IdempotentReplayException {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        String key = (header == null) ? null : header.getValue();
        if (key != null) {
            Integer existingOwnerId = store.find(key);
            if (existingOwnerId != null) {
                throw new IdempotentReplayException(existingOwnerId);
            }
        }
        idempotencyKey.set(key);
    }
}
