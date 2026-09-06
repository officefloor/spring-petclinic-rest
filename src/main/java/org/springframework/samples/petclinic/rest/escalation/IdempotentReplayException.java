package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckIdempotency} when
 * a create repeats with an {@code Idempotency-Key} that has already been seen. It carries the owner
 * created the first time the key was used so the create is not repeated; handled by
 * {@link IdempotentReplayExceptionHandler}, which responds 200 with that owner.
 *
 * <p>The owner is captured as its response DTO while a transaction is still open, so the handler
 * only needs to send it.
 */
public class IdempotentReplayException extends Exception {

    private final OwnerDto owner;

    public IdempotentReplayException(OwnerDto owner) {
        super("Idempotent replay of previously created owner " + (owner == null ? null : owner.getId()));
        this.owner = owner;
    }

    public OwnerDto getOwner() {
        return this.owner;
    }
}
