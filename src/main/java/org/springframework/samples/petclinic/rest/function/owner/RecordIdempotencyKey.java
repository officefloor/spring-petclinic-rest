package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a create has saved the owner, remembers the {@code Idempotency-Key} &rarr; owner id so a
 * later repeat with the same key replays this owner (see {@link CheckIdempotencyKey}). A request
 * without a key is a no-op. Runs after {@code save}, so the owner has its generated id.
 */
public class RecordIdempotencyKey {

    public void service(@Val Owner owner, ServerHttpConnection connection, IdempotencyStore store) {
        String key = IdempotencyKeys.from(connection);
        if (key != null) {
            store.record(key, owner.getId());
        }
    }
}
