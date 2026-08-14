package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link SaveOwner} has assigned the owner its id. If the
 * request carried an {@code Idempotency-Key}, records it against the new owner id in
 * {@link IdempotencyStore} so a later repeat of the same create replays this owner (200) via
 * {@link CheckIdempotentCreate} rather than creating a duplicate. A request without the header is left
 * untouched.
 */
public class RecordIdempotentCreate {

    public void service(@Val Owner owner, ServerHttpConnection connection, IdempotencyStore store) {
        String key = CheckIdempotentCreate.idempotencyKey(connection);
        if (key != null) {
            store.record(key, owner.getId());
        }
    }
}
