package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the create request's {@code Idempotency-Key} onto the new owner before it is saved, so a
 * later create with the same key is recognised as a repeat (see {@link CheckIdempotencyKey}). Does
 * nothing when the request carried no key.
 */
public class AssignOwnerIdempotencyKey {

    public void service(@Val Owner owner, @Val RequestIdempotencyKey idempotencyKey) {
        if (OwnerIdempotency.hasKey(idempotencyKey.value())) {
            owner.setIdempotencyKey(idempotencyKey.value());
        }
    }
}
