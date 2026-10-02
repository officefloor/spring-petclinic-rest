package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The {@code replay} branch out of {@link CheckIdempotency}: skip creation and return the owner the
 * already-seen idempotency key first created. The previously created owner id is passed on to the
 * loading step as its {@code @Parameter}.
 */
@FunctionalInterface
public interface ReplayFlow {
    void replay(Integer ownerId);
}
