package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerIdempotentReplayException;

/**
 * First step of the create-owner pipeline. When the request carries an {@code Idempotency-Key}
 * header that was already seen (a completed create), replays the originally created owner by
 * throwing {@link OwnerIdempotentReplayException}, which {@code HandleOwnerIdempotentReplay}
 * turns into a 200 — short-circuiting the pipeline before the identity check would reject the
 * repeat as a 409 duplicate.
 *
 * <p>Otherwise (no header, or a first-time key) it does nothing and the pipeline proceeds via
 * {@code next} to the normal validation/create chain; {@link RecordIdempotentCreate} records the
 * key at the end so the next repeat replays. The owner is mapped to its DTO here, while the
 * governing transaction is still open, so the handler never touches the persistence layer.
 */
public class RequireIdempotentCreate {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyStore store, OwnerRepository ownerRepository, OwnerMapper ownerMapper)
            throws OwnerIdempotentReplayException {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return; // no key: always a fresh create
        }
        Integer ownerId = store.find(idempotencyKey);
        if (ownerId == null) {
            return; // first time this key is seen: create normally, then record it
        }
        Owner owner = ownerRepository.findById(ownerId);
        if (owner == null) {
            return; // recorded owner is gone (e.g. rolled back): fall through and create afresh
        }
        throw new OwnerIdempotentReplayException(ownerMapper.toOwnerDto(owner));
    }
}
