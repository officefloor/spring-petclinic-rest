package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Supplies the monotonically increasing sequence numbers stamped on audit events, so events can be
 * totally ordered across owner creates.
 *
 * <p>Deliberately in-memory and non-transactional (like {@link IdempotencyStore}): the counter must
 * advance once per emitted event and never roll back with the request that consumed it, so the
 * ordering stays stable and strictly increasing for the life of the run.
 */
@Component
public class AuditSequence {

    private final AtomicLong counter = new AtomicLong();

    /** The next sequence number; strictly greater than every value previously returned. */
    public long next() {
        return this.counter.incrementAndGet();
    }
}
