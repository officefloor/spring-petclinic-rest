package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of create-owner. Reads the optional {@code Idempotency-Key} header and, when
 * it has already created an owner, publishes that originally created owner and takes the
 * {@code existing} branch to respond 200 — so a repeated create with a seen key does not
 * create a duplicate (which the later identity check would otherwise reject with 409).
 *
 * <p>Otherwise (no key, or a key not yet seen) it returns normally and the pipeline
 * continues to {@link ValidateOwnerFields}. The key is always published as an
 * {@link IdempotencyKey} so {@link RecordIdempotentCreate} can record the mapping after a
 * fresh owner is saved; a {@code null} value means no key was supplied.
 */
public class CheckIdempotentCreate {

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore idempotencyStore, OwnerRepository ownerRepository,
            Out<IdempotencyKey> idempotencyKey, Out<Owner> existingOwner,
            @Flow("existing") ExistingOwnerFlow existing) {
        idempotencyKey.set(new IdempotencyKey(key));
        if (key == null || key.isBlank()) {
            return; // no key — continue to create as normal
        }
        Integer ownerId = idempotencyStore.find(key);
        if (ownerId == null) {
            return; // key not seen — continue to create as normal
        }
        Owner owner = ownerRepository.findById(ownerId);
        if (owner == null) {
            return; // original owner is gone — treat as a fresh create
        }
        existingOwner.set(owner);
        existing.flow(); // respond 200 with the originally created owner; suppresses next
    }
}
