package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a successful create, remembers the new owner under its {@code Idempotency-Key} so a
 * repeat can be replayed by {@link CheckIdempotencyKey}. A no-op when no key was supplied.
 */
public class RecordIdempotencyKey {

    public void service(@Val String key, @Val Owner owner, IdempotencyStore store, OwnerMapper ownerMapper) {
        if (!key.isEmpty()) {
            store.record(key, ownerMapper.toOwnerDto(owner));
        }
    }
}
