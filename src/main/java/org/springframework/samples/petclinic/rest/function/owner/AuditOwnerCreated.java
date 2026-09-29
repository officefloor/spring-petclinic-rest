package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.function.common.IdentityVersion;

/**
 * Emits an audit line via the dedicated {@code AUDIT} logger on successful create, carrying
 * the persisted owner's id, {@code memberId} and {@code registrationDate}, plus the derived
 * {@code membershipLevel}. Wired after {@link SaveOwner} so the id is assigned, and after the steps
 * that set the member id and business-day registration date, so every value logged matches what
 * {@code GET /owners/{id}} later returns.
 *
 * <p>The membership fields are derived, not stored. Rather than re-derive them here, this step reads
 * the {@link OwnerDto} published by {@link MapOwner}, the same DTO {@link RespondWithOwnerCreated}
 * sends, so the logged values and the response body cannot drift apart.
 *
 * <p>In addition to the human-readable line, it emits an immutable structured {@link OwnerCreatedEvent}
 * as a single JSON object, so audit consumers can parse creates without scraping text. The event's
 * identifier slot carries the owner's <strong>primary identifier</strong> via
 * {@link Owner#primaryIdentifier()} — the unified member id.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across creates, stamped onto each structured event. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner, @Val OwnerDto dto, ObjectMapper objectMapper) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                dto.getMembershipLevel());

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                owner.primaryIdentifier(), dto.getMembershipLevel(), IdentityVersion.VERSION);
        // Single-argument info(): the JSON braces are logged verbatim, not treated as SLF4J placeholders.
        AUDIT.info(objectMapper.writeValueAsString(event));
    }
}
