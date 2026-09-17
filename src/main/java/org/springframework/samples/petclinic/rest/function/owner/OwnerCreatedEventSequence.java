package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Monotonic sequence for {@link OwnerCreatedEvent}s. A single shared instance across all owner
 * creations so each emitted event carries a strictly increasing {@code seq}.
 */
@Component
public class OwnerCreatedEventSequence {

    private final AtomicLong seq = new AtomicLong();

    /**
     * The next sequence value.
     *
     * @return a value starting at 1 and increasing by one on each call.
     */
    public long next() {
        return this.seq.incrementAndGet();
    }
}
