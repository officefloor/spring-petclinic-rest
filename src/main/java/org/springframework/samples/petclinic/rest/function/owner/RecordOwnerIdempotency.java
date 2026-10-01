package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Records the {@code Idempotency-Key} of a freshly created owner so a later repeat of the same key
 * replays this owner instead of creating a duplicate (see {@link CheckOwnerIdempotency}).
 *
 * <p>Runs after {@link SaveOwner}, once the owner has an assigned id. Requests without the header are
 * left untracked.
 */
public class RecordOwnerIdempotency {

    public void service(HttpServletRequest request, @Val Owner owner, OwnerIdempotencyStore store) {
        String idempotencyKey = IdempotencyKey.fromRequest(request);
        if (idempotencyKey != null) {
            store.record(idempotencyKey, owner.getId());
        }
    }
}
