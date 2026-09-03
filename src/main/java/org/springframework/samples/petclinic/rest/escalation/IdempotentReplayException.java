package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Thrown when a create-owner request repeats an already-seen {@code Idempotency-Key}. Carries the
 * owner first created under that key; handled by {@link IdempotentReplayExceptionHandler}, which
 * replays it with 200 instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final OwnerDto owner;

    public IdempotentReplayException(OwnerDto owner) {
        super("Idempotent replay of owner " + owner.getId());
        this.owner = owner;
    }

    public OwnerDto getOwner() {
        return this.owner;
    }
}
