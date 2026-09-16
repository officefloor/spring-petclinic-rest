package org.springframework.samples.petclinic.rest.audit;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Hands out monotonically increasing sequence numbers for structured audit events, so each
 * emitted event can be ordered against the others regardless of the emitting pipeline.
 *
 * <p>Kept as a process-wide singleton and safe for concurrent creates via {@link AtomicLong};
 * the first number handed out is {@code 1}.
 */
@Component
public class AuditEventSequence {

    private final AtomicLong counter = new AtomicLong();

    /** The next sequence number; strictly greater than every number returned before it. */
    public long next() {
        return this.counter.incrementAndGet();
    }
}
