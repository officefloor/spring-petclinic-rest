package org.springframework.samples.petclinic.rest.support;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Application-wide source of the monotonically increasing {@code seq} carried by the structured
 * {@code OWNER_CREATED} audit event. Each successful create draws the next value via
 * {@link #next()}; the counter is a process-wide singleton so sequence numbers strictly increase
 * across creates regardless of the request thread. The first value handed out is {@code 1}.
 */
@Component
public class OwnerEventSequence {

    private final AtomicLong counter = new AtomicLong();

    /** The next sequence value; strictly greater than every value previously returned. */
    public long next() {
        return counter.incrementAndGet();
    }
}
