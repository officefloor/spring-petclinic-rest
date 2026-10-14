package org.springframework.samples.petclinic.audit;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Publishes structured audit events as single-line JSON to the {@code AUDIT} logger.
 */
@Component
public class AuditEventPublisher {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AtomicLong ownerCreatedSeq = new AtomicLong();

    private final ObjectMapper objectMapper;

    public AuditEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes an {@link OwnerCreatedEvent} for a saved owner, assigning it the next sequence number.
     */
    public OwnerCreatedEvent ownerCreated(Owner owner, Integer membershipLevel) {
        OwnerCreatedEvent event = OwnerCreatedEvent.of(ownerCreatedSeq.incrementAndGet(), owner, membershipLevel);
        AUDIT.info("{}", objectMapper.writeValueAsString(event));
        return event;
    }
}
