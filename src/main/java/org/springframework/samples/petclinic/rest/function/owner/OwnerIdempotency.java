package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared idempotency lookup for the create-owner pipeline. An {@code Idempotency-Key} header
 * ties a create request to the owner it produced, so a retried create with an already-seen key
 * returns the original owner instead of creating a duplicate.
 *
 * <p>A missing or blank key means "no idempotency", and a soft-deleted owner never counts as the
 * original — matching how the rest of the pipeline (see {@link RequireUniqueIdentity}) ignores
 * deleted owners.
 */
final class OwnerIdempotency {

    private OwnerIdempotency() {
    }

    /** Whether {@code key} is a usable idempotency key (present and non-blank). */
    static boolean hasKey(String key) {
        return key != null && !key.isBlank();
    }

    /**
     * The owner previously created under {@code key}, or {@code null} when the key is absent or no
     * live owner carries it.
     */
    static Owner findByKey(OwnerRepository ownerRepository, String key) {
        if (!hasKey(key)) {
            return null;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (!existing.isDeleted() && key.equals(existing.getIdempotencyKey())) {
                return existing;
            }
        }
        return null;
    }
}
