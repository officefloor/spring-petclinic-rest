package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line via the dedicated {@code AUDIT} logger once an owner has been
 * created, carrying the owner id, memberId and registrationDate. Alongside the
 * human-readable line it emits an immutable structured {@link OwnerCreatedEvent}.
 */
public class LogAuditOnCreate {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            MembershipLevel.of(MembershipPoints.of(owner)));
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getCustomerCode());
        AUDIT.info(new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
            owner.getCustomerCode(), MembershipLevel.of(MembershipPoints.of(owner))).toJson());
    }

    /**
     * Immutable structured owner-created event. Carries the owner's unified primary
     * identifier, the {@code memberId}.
     */
    private record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel) {
        String toJson() {
            return "{\"seq\":" + seq + ",\"ownerId\":" + ownerId
                + ",\"memberId\":\"" + memberId + "\""
                + ",\"membershipLevel\":" + membershipLevel
                + ",\"event\":\"OWNER_CREATED\"}";
        }
    }
}
