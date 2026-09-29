package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import net.officefloor.web.HttpHeaderParameter;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Entry step of the create-owner pipeline, implementing idempotent create. If the request carries an
 * {@code Idempotency-Key} header whose value was already used to create an owner, that original owner
 * is published and the {@code existing} branch responds with it (200) — the rest of the create
 * pipeline (and its duplicate/identity 409) is skipped. Otherwise the {@code create} branch runs the
 * normal pipeline, and {@link RecordIdempotencyKey} remembers the key against the new owner.
 *
 * <p>The header is optional: a request without it (or with a blank value) always takes the
 * {@code create} branch, so ordinary creates are unaffected.
 */
public class CheckIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore idempotencyStore, OwnerRepository ownerRepository, Out<Owner> existingOwner,
            @Flow("existing") ContinueFlow existing, @Flow("create") ContinueFlow create) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer existingId = idempotencyStore.find(idempotencyKey);
            if (existingId != null) {
                Owner owner = loadOrNull(ownerRepository, existingId);
                if (owner != null) {
                    existingOwner.set(owner);
                    existing.proceed();
                    return;
                }
            }
        }

        create.proceed();
    }

    private static Owner loadOrNull(OwnerRepository ownerRepository, int ownerId) {
        try {
            return ownerRepository.findById(ownerId);
        } catch (DataAccessException ex) {
            return null; // the recorded owner is gone — fall back to a fresh create
        }
    }
}
