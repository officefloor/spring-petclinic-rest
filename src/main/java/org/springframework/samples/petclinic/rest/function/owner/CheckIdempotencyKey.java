package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Optional;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.IdempotentReplayException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of {@code POST /api/owners}. Reads the optional {@code Idempotency-Key} header and,
 * when it matches a create that already completed, short-circuits the pipeline by throwing
 * {@link IdempotentReplayException} so the original owner is returned with 200 rather than a
 * duplicate being created. Otherwise it publishes the key (or an {@link IdempotencyKey#absent()
 * absent} marker) for {@link RecordIdempotencyKey} to pin once the create succeeds.
 */
public class CheckIdempotencyKey {

    public void service(
            @RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore idempotencyStore, OwnerRepository ownerRepository,
            Out<IdempotencyKey> idempotencyKey)
            throws IdempotentReplayException, NotFoundException {

        idempotencyKey.set(new IdempotencyKey(key));

        if (key == null) {
            return; // no key: an ordinary create
        }

        Optional<Integer> createdId = idempotencyStore.find(key);
        if (createdId.isPresent()) {
            Owner original = Lookups.findOrNotFound(() -> ownerRepository.findById(createdId.get()),
                    "Owner not found: " + createdId.get());
            throw new IdempotentReplayException(original);
        }
    }
}
