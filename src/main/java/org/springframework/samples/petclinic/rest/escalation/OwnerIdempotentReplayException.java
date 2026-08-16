package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Thrown by the create-owner pipeline when the request repeats an already-seen
 * {@code Idempotency-Key}: rather than create a duplicate (which the later identity check would
 * reject 409), the pipeline replays the originally created owner.
 *
 * <p>Carries the owner already mapped to its DTO — mapped while the transaction (and Hibernate
 * session) is still open in the first step — so {@link OwnerIdempotentReplayExceptionHandler} can
 * respond 200 with it without touching the persistence layer.
 */
public class OwnerIdempotentReplayException extends Exception {

    private final OwnerDto owner;

    public OwnerIdempotentReplayException(OwnerDto owner) {
        super("Idempotent replay of owner " + owner.getId());
        this.owner = owner;
    }

    public OwnerDto getOwner() {
        return owner;
    }
}
