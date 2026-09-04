package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line via the dedicated {@code AUDIT} logger once an owner has been
 * created, carrying the owner id, customerCode and registrationDate. Alongside the
 * human-readable line it emits an immutable structured {@link OwnerCreatedEvent}.
 */
public class LogAuditOnCreate {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            MembershipLevel.of(MembershipPoints.of(owner)),
            owner.getCustomerCode() + "-M" + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100));
        AUDIT.info(new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
            owner.getCustomerCode(), MembershipLevel.of(MembershipPoints.of(owner))).toJson());
    }

    /**
     * Immutable structured owner-created event. Carries the owner's current primary
     * identifier: the {@code customerCode} today, and whatever replaces it later (once
     * the customerCode is unified into the memberId, that value is carried instead).
     */
    private record OwnerCreatedEvent(long seq, int ownerId, String customerCode, int membershipLevel) {
        String toJson() {
            return "{\"seq\":" + seq + ",\"ownerId\":" + ownerId
                + ",\"customerCode\":\"" + customerCode + "\""
                + ",\"membershipLevel\":" + membershipLevel
                + ",\"event\":\"OWNER_CREATED\"}";
        }
    }
}
