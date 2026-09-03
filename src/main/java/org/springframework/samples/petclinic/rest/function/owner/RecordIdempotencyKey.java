package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.RequestHeader;

/** After a successful create, remember the new owner under its {@code Idempotency-Key}. */
public class RecordIdempotencyKey {

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            @Val Owner owner, IdempotencyStore idempotencyStore) {
        if (key != null) {
            idempotencyStore.record(key, owner.getId());
        }
    }
}
