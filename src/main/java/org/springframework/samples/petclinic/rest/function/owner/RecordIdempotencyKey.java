package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyKey;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;

/**
 * Runs after the owner is saved: when the request carried an {@code Idempotency-Key}, records the
 * just-created owner against it so a later create with the same key replays this owner instead of
 * creating a duplicate. Stores the response DTO (mapped while the session is still open) so the
 * replay needs no further database access.
 */
public class RecordIdempotencyKey {

    public void service(@Val IdempotencyKey key, @Val Owner owner, OwnerMapper ownerMapper,
            IdempotencyStore store) {
        if (key != null && key.isPresent()) {
            store.record(key.value(), ownerMapper.toOwnerDto(owner));
        }
    }
}
