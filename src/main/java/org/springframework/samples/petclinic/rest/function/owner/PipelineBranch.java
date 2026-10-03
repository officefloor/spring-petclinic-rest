package org.springframework.samples.petclinic.rest.function.owner;

/**
 * A conditional branch out of a pipeline step, taken by calling {@link #take()} on a
 * parameter annotated {@code @Flow("<name>")}. Not calling a branch leaves it untaken.
 */
@FunctionalInterface
public interface PipelineBranch {
    void take();
}
