package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the immutable structured {@link OwnerCreatedEvent} as JSON on the dedicated
 * {@code AUDIT} logger, in addition to the human-readable audit line from
 * {@link AuditOwnerCreated}. Runs after {@code save}, so the owner has a generated id.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates (process-wide).
 */
public class EmitOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQ = new AtomicLong();

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public void service(@Val Owner owner) {
        OwnerCreatedEvent event = OwnerCreatedEvent.of(SEQ.incrementAndGet(), owner);
        AUDIT.info(JSON.writeValueAsString(event));
    }
}
