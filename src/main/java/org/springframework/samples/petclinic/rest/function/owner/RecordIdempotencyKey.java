package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a create succeeds, associates the request's {@code Idempotency-Key} (if any) with the newly
 * created owner's id, so a later repeat with the same key replays this owner rather than creating a
 * duplicate. Runs after {@link SaveOwner} so the id is assigned; a no-op when the request carried no
 * key.
 */
public class RecordIdempotencyKey {

    public void service(ServerHttpConnection connection, @Val Owner owner, IdempotencyStore idempotencyStore) {
        IdempotencyStore.keyOf(connection).ifPresent(key -> idempotencyStore.record(key, owner.getId()));
    }
}
