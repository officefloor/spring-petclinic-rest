package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.support.IdempotencyKeyStore;

/**
 * Runs after {@code SaveOwner} (so the owner id is populated) on the create path. When the request
 * carried an {@code Idempotency-Key}, records key -&gt; created owner id so a later repeat of the same
 * key {@link CheckIdempotencyKey replays} this owner instead of creating a duplicate. A request with
 * no key records nothing.
 */
public class RecordIdempotencyKey {

    public void service(ServerHttpConnection connection, @Val Owner owner, IdempotencyKeyStore store) {
        String key = CheckIdempotencyKey.idempotencyKey(connection);
        if (key != null) {
            store.record(key, owner.getId());
        }
    }
}
