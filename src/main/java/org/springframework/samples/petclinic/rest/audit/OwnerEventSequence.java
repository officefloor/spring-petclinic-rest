package org.springframework.samples.petclinic.rest.audit;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Source of the monotonically increasing sequence number carried by each
 * {@link OwnerCreatedEvent}. A single shared counter across all owner creations for the life of
 * the application, so successive creates receive strictly increasing sequence numbers.
 */
@Component
public class OwnerEventSequence {

    private final AtomicLong counter = new AtomicLong();

    /** The next sequence number, starting at 1 and increasing by one on each call. */
    public long next() {
        return this.counter.incrementAndGet();
    }
}
