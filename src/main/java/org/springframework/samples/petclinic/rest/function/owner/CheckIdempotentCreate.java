package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;

/**
 * Runs first in the create-owner pipeline. If the request carries an {@code Idempotency-Key} header
 * that {@link IdempotencyStore} has already seen, and the owner it created still exists, throws
 * {@link IdempotentReplayException} carrying that owner so the pipeline short-circuits to a 200 replay
 * before any duplicate can be created (which would otherwise 409). Otherwise it does nothing and the
 * normal validate/build/save pipeline proceeds; {@link RecordIdempotentCreate} records the key once
 * the owner has been saved.
 */
public class CheckIdempotentCreate {

    public static final String HEADER = "Idempotency-Key";

    public void service(ServerHttpConnection connection, IdempotencyStore store,
            OwnerRepository ownerRepository) throws IdempotentReplayException {
        String key = idempotencyKey(connection);
        if (key == null) {
            return;
        }
        Integer ownerId = store.find(key);
        if (ownerId == null) {
            return;
        }
        Owner owner = ownerRepository.findById(ownerId);
        if (owner != null && !Boolean.TRUE.equals(owner.getDeleted())) {
            throw new IdempotentReplayException(owner);
        }
    }

    /** The trimmed {@code Idempotency-Key} header value, or {@code null} when absent or blank. */
    static String idempotencyKey(ServerHttpConnection connection) {
        HttpHeader header = connection.getRequest().getHeaders().getHeader(HEADER);
        if (header == null) {
            return null;
        }
        String value = header.getValue();
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
