package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.idempotency.IdempotencyStore;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Runs first in the create-owner pipeline. When the request carries an {@code Idempotency-Key}
 * header that a previous create already used, this replays that create: it loads the originally
 * created owner, publishes it, and branches to the {@code existing} output which responds 200 -
 * short-circuiting the rest of the pipeline so no duplicate is created.
 *
 * <p>Otherwise (no header, or a key not seen before, or a key whose owner no longer exists) it
 * publishes the key for {@link RecordIdempotencyKey} to remember after the owner is saved, and
 * branches to {@code create} to run the normal validate-through-save pipeline.
 */
public class CheckIdempotencyKey {

    @FunctionalInterface
    public interface ExistingFlow {
        void proceed();
    }

    @FunctionalInterface
    public interface CreateFlow {
        void proceed();
    }

    public void service(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            IdempotencyStore idempotencyStore, OwnerRepository ownerRepository,
            Out<String> keyOut, Out<Owner> existingOwner,
            @Flow("existing") ExistingFlow existing, @Flow("create") CreateFlow create) {
        // Publish the key (possibly null) so the post-save recording step can read it on the create
        // path; the existing path never reaches that step.
        keyOut.set(idempotencyKey);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer priorId = idempotencyStore.find(idempotencyKey);
            if (priorId != null) {
                Owner owner = ownerRepository.findById(priorId);
                if (owner != null) {
                    existingOwner.set(owner);
                    existing.proceed();
                    return;
                }
            }
        }
        create.proceed();
    }
}
