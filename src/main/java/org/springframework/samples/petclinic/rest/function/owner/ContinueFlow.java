package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single-method flow interface for the conditional branches out of {@link CheckIdempotencyKey}.
 * The branch actually taken is named by the {@code @Flow(...)} value on the parameter (matched to a
 * step in the endpoint's {@code outputs:} map), so this same type serves every branch.
 */
@FunctionalInterface
public interface ContinueFlow {

    void proceed();
}
