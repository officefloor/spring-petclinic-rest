package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.samples.petclinic.model.Owner;
import tools.jackson.databind.ObjectMapper;

/**
 * Emits the immutable {@link OwnerCreatedEvent} for a newly created owner to the dedicated
 * {@code AUDIT} logger as a single JSON line. Owns the monotonically increasing sequence
 * shared across all creates; being a singleton Spring bean, the counter spans the
 * application lifetime rather than a single request.
 */
@Component
public class OwnerCreatedEventPublisher {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AtomicLong sequence = new AtomicLong();

    private final ObjectMapper objectMapper;

    public OwnerCreatedEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Publish the create event for {@code owner}, assigning it the next sequence number. */
    public void publish(Owner owner) {
        OwnerCreatedEvent event = OwnerCreatedEvent.of(this.sequence.incrementAndGet(), owner);
        AUDIT.info(this.objectMapper.writeValueAsString(event));
    }
}
