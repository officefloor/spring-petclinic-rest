package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * The first step of {@code POST /api/owners}. When the request carries an {@code Idempotency-Key}
 * that has already created an owner, the create is not repeated: the originally created owner is
 * loaded and thrown as an {@link IdempotentReplayException} so the pipeline short-circuits to a 200
 * response (rather than reaching the duplicate check and returning 409).
 *
 * <p>Otherwise the key (which may be {@code null}) is published for {@link RecordIdempotency} to
 * remember once the new owner has been saved, and the pipeline proceeds to build the owner.
 */
public class CheckIdempotency {

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore idempotencyStore, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            Out<String> idempotencyKey) throws IdempotentReplayException {
        idempotencyKey.set(key);
        if (key == null || key.isBlank()) {
            return; // no key: always create
        }
        Integer existingId = idempotencyStore.find(key);
        if (existingId == null) {
            return; // key not seen yet: create, then RecordIdempotency remembers it
        }
        // Repeat of an already-seen key: return the originally created owner (mapped while a
        // transaction is still open) instead of creating a duplicate.
        Owner owner = ownerRepository.findById(existingId);
        throw new IdempotentReplayException(ownerMapper.toOwnerDto(owner));
    }
}
