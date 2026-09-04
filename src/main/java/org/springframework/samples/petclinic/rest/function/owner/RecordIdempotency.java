package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * After a create succeeds, remember the new owner against its {@code Idempotency-Key} (if
 * one was supplied) so a repeat with the same key returns this owner instead of a duplicate.
 */
public class RecordIdempotency {

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            @Val Owner owner, IdempotencyStore store) {
        if (key != null) {
            store.record(key, owner.getId());
        }
    }
}
