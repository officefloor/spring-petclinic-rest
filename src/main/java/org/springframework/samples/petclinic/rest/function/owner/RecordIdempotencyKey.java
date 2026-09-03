package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.server.http.ServerHttpConnection;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the saved owner under the request's {@code Idempotency-Key}, when one was supplied, so a
 * later repeat of the same key replays this owner rather than creating a duplicate.
 */
public class RecordIdempotencyKey {

    public void service(@Val Owner owner, OwnerMapper ownerMapper, ServerHttpConnection connection,
            IdempotencyStore store) {
        String key = IdempotencyKey.read(connection);
        if (key != null) {
            store.record(key, ownerMapper.toOwnerDto(owner));
        }
    }
}
