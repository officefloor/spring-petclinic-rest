package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Runs first on {@code POST /api/owners}. When the request carries an {@code Idempotency-Key} that
 * was already used to create an owner, it throws {@link IdempotentReplayException} to short-circuit
 * the pipeline and replay that owner with 200 — before the duplicate-identity check would otherwise
 * reject the repeat with 409. Otherwise it republishes the key (which may be absent) so
 * {@link RecordIdempotency} can remember it once the new owner is saved. Does not read the request
 * body, leaving that to {@link ValidateOwnerFields}.
 */
public class CheckIdempotency {

    public void service(
            @RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore store, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            Out<String> idempotencyKey) throws IdempotentReplayException {
        idempotencyKey.set(key);
        if (key == null || key.isBlank()) {
            return; // no key: proceed with a normal create
        }
        Integer existingId = store.find(key);
        if (existingId == null) {
            return; // first time this key is seen: proceed and record after save
        }
        Owner existing = ownerRepository.findById(existingId);
        if (existing != null) {
            // Map while the persistence context is still open, then replay with 200.
            throw new IdempotentReplayException(ownerMapper.toOwnerDto(existing));
        }
    }
}
