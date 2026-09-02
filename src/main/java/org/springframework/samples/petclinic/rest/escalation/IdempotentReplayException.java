package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Thrown by {@code CheckIdempotencyKey} when a create repeats with an already-seen
 * {@code Idempotency-Key}. Carries the originally created owner so
 * {@link IdempotentReplayExceptionHandler} can return it with 200 instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final transient OwnerDto owner;

    public IdempotentReplayException(OwnerDto owner) {
        this.owner = owner;
    }

    public OwnerDto getOwner() {
        return owner;
    }
}
