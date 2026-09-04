package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.support.IdempotencyStore;

/**
 * After a successful create, remembers the new owner id against the request's
 * {@code Idempotency-Key} (when present), so a later repeat with the same key replays the
 * original owner (see {@link CheckIdempotencyKey}) rather than creating a duplicate. Runs
 * after {@link SaveOwner} so the owner id is assigned.
 */
public class RecordIdempotencyKey {

    public void service(@Val Owner owner, ServerHttpConnection connection, IdempotencyStore store) {
        String key = IdempotencyKeys.read(connection);
        if (key != null) {
            store.record(key, owner.getId());
        }
    }
}
