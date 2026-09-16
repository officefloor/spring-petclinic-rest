package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Remembers the owner a new {@code Idempotency-Key} just created, so a later create
 * repeating the key replays this owner instead of duplicating it (see
 * {@link CheckIdempotencyKey}). Runs after {@link SaveOwner}, once the owner has its
 * generated id; a request without the header records nothing.
 */
public class RecordIdempotencyKey {

    public void service(@Val Owner owner, ServerHttpConnection connection, IdempotencyStore store) {
        String key = IdempotencyKeyHeader.read(connection);
        if (key != null) {
            store.record(key, owner.getId());
        }
    }
}
