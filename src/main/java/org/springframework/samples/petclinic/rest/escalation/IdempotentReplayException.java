package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Thrown by the create-owner pipeline when the request repeats an already-seen
 * {@code Idempotency-Key}. It carries the originally created owner so the handler can replay that
 * response with 200 instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final transient OwnerDto owner;

    public IdempotentReplayException(OwnerDto owner) {
        super("Idempotency-Key already used; returning the originally created owner");
        this.owner = owner;
    }

    public OwnerDto getOwner() {
        return this.owner;
    }
}
