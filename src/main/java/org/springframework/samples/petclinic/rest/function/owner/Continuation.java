package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single-method flow interface used to select one of a step's {@code outputs:} branches.
 * Invoke it to take that branch; the annotation {@code @Flow("<name>")} on the parameter
 * names the branch and not invoking a branch short-circuits it.
 */
@FunctionalInterface
public interface Continuation {

    void run();
}
