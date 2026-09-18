package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Thrown by the create-owner pipeline when the request's {@code Idempotency-Key} matches a
 * create that already completed. Carries the originally created owner so
 * {@link IdempotentReplayExceptionHandler} can return it with 200 instead of creating a
 * duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final transient Owner owner;

    public IdempotentReplayException(Owner owner) {
        super("Idempotent replay of create; returning existing owner: " + owner.getId());
        this.owner = owner;
    }

    public Owner getOwner() {
        return this.owner;
    }
}
