package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import org.springframework.samples.petclinic.rest.function.common.IdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * The entry step of {@code POST /api/owners}: honours an optional {@code Idempotency-Key} header.
 *
 * <p>When the request carries a key already seen (a repeated create), the branch short-circuits to
 * {@code existing}, which loads and returns the owner originally created for that key with 200 —
 * no duplicate is created. Otherwise (no key, or a new key) the {@code create} branch runs the
 * normal validate-through-respond pipeline; the key, if any, is recorded once the owner is saved by
 * {@link RecordOwnerIdempotency}.
 *
 * <p>This runs before {@link ValidateOwner} reads the body, so a duplicate retry never binds
 * {@code @RequestBody} — only the taken branch does.
 */
public class CheckOwnerIdempotency {

    @FunctionalInterface
    public interface ExistingOwnerFlow {
        void flow(Integer ownerId);
    }

    @FunctionalInterface
    public interface CreateOwnerFlow {
        void flow();
    }

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            IdempotencyStore idempotencyStore, @Flow("existing") ExistingOwnerFlow existing,
            @Flow("create") CreateOwnerFlow create) {
        Integer existingOwnerId = idempotencyStore.find(idempotencyKey);
        if (existingOwnerId != null) {
            existing.flow(existingOwnerId);
            return;
        }
        create.flow();
    }
}
