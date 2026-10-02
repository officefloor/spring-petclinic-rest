package org.springframework.samples.petclinic.rest.function.owner;

/**
 * A conditional pipeline branch for {@link CheckIdempotency}: calling {@link #take()} runs the
 * wired-up step for that {@code @Flow} output, not calling it leaves the branch untaken. Used
 * for both the idempotent-hit response and the normal-create continuation.
 */
@FunctionalInterface
public interface FlowBranch {

    void take();
}
