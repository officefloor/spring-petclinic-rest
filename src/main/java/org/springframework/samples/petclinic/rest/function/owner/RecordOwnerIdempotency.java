package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link SaveOwner} so the owner id is assigned. When the create carried an
 * {@code Idempotency-Key}, it binds the key to the newly created owner so a later repeat with
 * the same key is served this owner (via {@link CheckOwnerIdempotency}) instead of creating a
 * duplicate. A create without the header records nothing.
 */
public class RecordOwnerIdempotency {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            @Val Owner owner, OwnerIdempotencyStore idempotencyStore) {
        idempotencyStore.record(idempotencyKey, owner.getId());
    }
}
