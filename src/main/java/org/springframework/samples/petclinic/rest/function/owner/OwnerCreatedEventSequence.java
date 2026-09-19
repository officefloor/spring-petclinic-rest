package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Hands out the monotonically increasing {@code seq} carried by each {@link OwnerCreatedEvent}.
 * A single application-wide sequence (this is a singleton bean) so the numbers strictly increase
 * across every owner create, independent of the per-request function instances.
 */
@Component
public class OwnerCreatedEventSequence {

    private final AtomicLong seq = new AtomicLong();

    /** The next sequence number; the first call returns {@code 1}. */
    public long next() {
        return this.seq.incrementAndGet();
    }
}
