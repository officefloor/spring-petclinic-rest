package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The "create" branch out of {@link CheckIdempotencyKey}: proceed into the normal owner-create
 * pipeline (validate → … → respond). Declared as a {@code @Flow} so the check step can take exactly
 * one of its branches, never both.
 */
@FunctionalInterface
public interface CreateFlow {

    void proceed();
}
