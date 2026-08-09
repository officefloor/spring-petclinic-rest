package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.HttpHeaderParameter;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Entry step for {@code POST /api/owners}. Implements idempotent create.
 *
 * <p>When the request carries an {@code Idempotency-Key} header whose value has already produced an
 * owner (recorded by {@link RecordIdempotencyKey}) and that owner still exists, this step takes the
 * {@code replay} branch — {@link RespondWithExistingOwner} returns the original owner with 200 — so
 * a repeated create never persists a duplicate (which would otherwise be a 409). Otherwise (no
 * header, an unseen key, or the original owner gone) it takes the {@code create} branch into the
 * normal create pipeline.
 *
 * <p>This step has no {@code next}: it always calls exactly one of the two {@code @Flow} branches,
 * so control continues down precisely one path.
 */
public class CheckIdempotencyKey {

    public void service(@HttpHeaderParameter("Idempotency-Key") String idempotencyKey,
            IdempotencyKeyRegistry registry, OwnerRepository ownerRepository,
            @Flow("replay") ReplayFlow replay, @Flow("create") CreateFlow create) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer ownerId = registry.find(idempotencyKey);
            if (ownerId != null) {
                Owner existing = ownerRepository.findById(ownerId);
                // A soft-deleted owner is treated as absent, so a repeat after deletion re-creates.
                if (existing != null && !Boolean.TRUE.equals(existing.getDeleted())) {
                    replay.replay(existing);
                    return;
                }
            }
        }
        create.proceed();
    }
}
