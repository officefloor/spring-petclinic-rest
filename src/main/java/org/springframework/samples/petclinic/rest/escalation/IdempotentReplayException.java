package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Thrown by the create pipeline when the request's {@code Idempotency-Key} has already been seen,
 * to short-circuit the create and replay the originally created owner. Carries the already-mapped
 * {@link OwnerDto} (mapped while the persistence context is still open) so
 * {@link IdempotentReplayExceptionHandler} can respond {@code 200 OK} with it, instead of creating
 * a duplicate (which would otherwise be a 409).
 */
public class IdempotentReplayException extends Exception {

    private final transient OwnerDto owner;

    public IdempotentReplayException(OwnerDto owner) {
        super("Idempotent replay of owner id=" + (owner == null ? null : owner.getId()));
        this.owner = owner;
    }

    public OwnerDto getOwner() {
        return this.owner;
    }
}
