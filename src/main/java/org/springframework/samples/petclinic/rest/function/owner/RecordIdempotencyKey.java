package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a new owner is saved, remember which owner an {@code Idempotency-Key} created so a repeat
 * of that key replays the original owner (see {@link CheckIdempotencyKey}).
 */
public class RecordIdempotencyKey {

    public void service(@Val Owner owner, ServerHttpConnection connection) {
        String key = CheckIdempotencyKey.key(connection);
        if (key != null) {
            CheckIdempotencyKey.SEEN.put(key, owner.getId());
        }
    }
}
