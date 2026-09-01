package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.MemberId;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.OwnerSegment;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger once a create has succeeded.
 * Runs after {@code SaveOwner} so the persisted owner's id is available alongside the
 * assigned memberId and the (business-day adjusted) registrationDate.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    public void service(@Val Owner owner, OwnerMapper mapper) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                mapper.membershipLevel(owner));
        AUDIT.info(new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                owner.getMemberId(), mapper.membershipLevel(owner),
                OwnerSegment.of(mapper.membershipLevel(owner), MemberId.region(owner.getMemberId()))).toJson());
    }

    /**
     * Immutable structured audit event (schema version 2). {@code identifier} is the owner's primary
     * identifier, the unified memberId; {@code ownerSegment} is recomputed from the version-2 identity.
     */
    private record OwnerCreatedEvent(int seq, Integer ownerId, String identifier, Integer membershipLevel,
            String ownerSegment) {
        String toJson() {
            return String.format(
                    "{\"schemaVersion\":2,\"seq\":%d,\"ownerId\":%d,\"memberId\":%s,\"membershipLevel\":%d,\"ownerSegment\":%s,\"event\":\"OWNER_CREATED\"}",
                    seq, ownerId, quote(identifier), membershipLevel, quote(ownerSegment));
        }

        private static String quote(String value) {
            return value == null ? "null" : "\"" + value + "\"";
        }
    }
}
