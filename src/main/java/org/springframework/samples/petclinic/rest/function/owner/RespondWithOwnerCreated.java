package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence stamped on every OWNER_CREATED event across all creates. */
    private static final AtomicLong EVENT_SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        auditLogger.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), dto.getMembershipLevel());
        auditLogger.info(ownerCreatedEvent(EVENT_SEQ.incrementAndGet(), owner, dto.getMembershipLevel()));
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }

    /**
     * Renders the immutable structured OWNER_CREATED event. The {@code memberId} field carries the
     * owner's primary identifier, now that the customerCode and membershipNumber have been unified
     * into the memberId; {@link #primaryIdentifier(Owner)} is the single place that resolves it.
     */
    private static String ownerCreatedEvent(long seq, Owner owner, int membershipLevel) {
        return "{\"seq\":" + seq
                + ",\"ownerId\":" + owner.getId()
                + ",\"memberId\":" + jsonString(primaryIdentifier(owner))
                + ",\"membershipLevel\":" + membershipLevel
                + ",\"event\":\"OWNER_CREATED\"}";
    }

    /** The owner's primary identifier: the unified memberId. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /** Renders a string as a JSON literal (quoted, with the mandatory escapes), or {@code null}. */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
