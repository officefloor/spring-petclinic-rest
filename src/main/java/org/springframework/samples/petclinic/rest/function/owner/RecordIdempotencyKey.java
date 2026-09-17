package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;

/**
 * After a fresh owner is saved, registers the request's {@code Idempotency-Key} against the generated
 * owner id so a later repeat of the same key replays this owner (see {@link CheckIdempotency}) rather
 * than creating a duplicate. A request without a key is left untouched. Runs after {@link SaveOwner},
 * so the owner already carries its id.
 */
public class RecordIdempotencyKey {

    public void service(
            @HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            @Val Owner owner, IdempotencyStore idempotencyStore) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyStore.record(idempotencyKey, owner.getId());
        }
    }
}
