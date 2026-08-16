package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the just-created owner against the request's {@code Idempotency-Key} (when one was
 * supplied), so a later create repeating that key resolves back to this owner in
 * {@link CheckIdempotencyKey}. Runs after {@link SaveOwner} so the owner has its assigned id, and is
 * a no-op when the request carried no key.
 */
public class RecordIdempotencyKey {

    public void service(ServerHttpConnection connection, @Val Owner owner,
            IdempotencyStore idempotencyStore) {
        String key = CheckIdempotencyKey.key(connection);
        if (key != null) {
            idempotencyStore.record(key, owner.getId());
        }
    }
}
