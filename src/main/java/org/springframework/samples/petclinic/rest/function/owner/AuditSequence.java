package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Hands out monotonically increasing sequence numbers for audit events, so each emitted
 * event carries a strictly ordered {@code seq} across every create. Thread-safe and
 * application-scoped; the first number handed out is 1.
 */
@Component
public class AuditSequence {

    private final AtomicLong counter = new AtomicLong();

    /** The next sequence number, one greater than the previous. */
    public long next() {
        return this.counter.incrementAndGet();
    }
}
