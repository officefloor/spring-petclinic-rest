package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Thrown by the create pipeline when the request repeats an already-seen {@code Idempotency-Key}.
 * Carries the owner originally created for that key; handled by {@link IdempotentReplayExceptionHandler},
 * which replays it with 200 instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final transient OwnerDto owner;

    public IdempotentReplayException(OwnerDto owner) {
        super("Idempotent replay of a previously created owner");
        this.owner = owner;
    }

    public OwnerDto getOwner() {
        return this.owner;
    }
}
