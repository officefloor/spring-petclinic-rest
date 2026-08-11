package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Raised by the first step of {@code POST /api/owners} when the request repeats an
 * {@code Idempotency-Key} that already created an owner. Rather than creating a duplicate
 * (which the identity/household rules would reject with a 409), the pipeline short-circuits
 * and the handler returns the originally created owner with 200.
 *
 * <p>Carries the already-created {@link Owner} (loaded with its eager relations) so the
 * handler can map it to the response body without re-running the create.
 */
public class IdempotentCreateReplayException extends Exception {

    private final transient Owner owner;

    public IdempotentCreateReplayException(Owner owner) {
        super("Idempotent replay of an already-created owner: " + owner.getId());
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }
}
