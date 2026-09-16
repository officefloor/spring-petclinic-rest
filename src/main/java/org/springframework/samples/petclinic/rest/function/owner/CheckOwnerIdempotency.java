package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.idempotency.OwnerIdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Entry point of {@code POST /api/owners}: enforces idempotent creates. When the request
 * carries an {@code Idempotency-Key} already seen (see {@link OwnerIdempotencyStore}), the
 * originally created owner is published and the {@code existing} branch responds with it
 * (200), skipping creation entirely — so a repeat is neither a duplicate (409) nor a second
 * insert. Otherwise the {@code proceed} branch runs the normal create pipeline.
 *
 * <p>Runs before any body binding/validation: a repeat short-circuits regardless of the body,
 * which is exactly the idempotency guarantee. {@link RecordOwnerIdempotency} stores the key
 * once the create succeeds.
 */
public class CheckOwnerIdempotency {

    @FunctionalInterface
    public interface Existing {
        void existing();
    }

    @FunctionalInterface
    public interface Proceed {
        void proceed();
    }

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            OwnerIdempotencyStore store, OwnerRepository ownerRepository, Out<Owner> existingOwner,
            @Flow("existing") Existing existing, @Flow("proceed") Proceed proceed) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer ownerId = store.find(idempotencyKey);
            if (ownerId != null) {
                Owner owner = findExisting(ownerRepository, ownerId);
                if (owner != null) {
                    existingOwner.set(owner);
                    existing.existing();
                    return;
                }
            }
        }
        proceed.proceed();
    }

    private static Owner findExisting(OwnerRepository ownerRepository, int ownerId) {
        try {
            return ownerRepository.findById(ownerId);
        } catch (DataAccessException notFound) {
            return null; // owner is gone; fall back to creating afresh
        }
    }
}
