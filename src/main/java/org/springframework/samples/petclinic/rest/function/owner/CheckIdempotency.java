package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of {@code POST /api/owners}. When the request carries an {@code Idempotency-Key}
 * that a previous create has already recorded (and that owner still exists), publishes that
 * owner and takes the {@code hit} flow to respond with it and 200 OK, so no duplicate is made.
 * Otherwise publishes the key for {@link RecordIdempotency} and takes the {@code proceed} flow
 * into the normal validate/build/save pipeline.
 *
 * <p>Makes only the idempotency decision (key + store + existence); loading the owner for the
 * response and mapping it to a DTO are left to the responder on the {@code hit} branch.
 */
public class CheckIdempotency {

    public void service(
            @RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore store, OwnerRepository ownerRepository,
            Out<IdempotencyKey> keyOut, Out<Owner> ownerOut,
            @Flow("hit") FlowBranch hit, @Flow("proceed") FlowBranch proceed) {
        IdempotencyKey idempotencyKey = new IdempotencyKey(key);
        if (idempotencyKey.isPresent()) {
            Integer existingId = store.find(idempotencyKey.value());
            if (existingId != null) {
                Owner owner = ownerRepository.findById(existingId);
                if (owner != null) {
                    // Seen key with a live owner: respond with the original, create nothing.
                    ownerOut.set(owner);
                    hit.take();
                    return;
                }
            }
        }
        // First time (or a stale key): carry the key forward and run the normal create.
        keyOut.set(idempotencyKey);
        proceed.take();
    }
}
