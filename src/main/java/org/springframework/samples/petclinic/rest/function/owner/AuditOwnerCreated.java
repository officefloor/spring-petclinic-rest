package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYears;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Emits an audit record for a freshly created owner via the dedicated {@code AUDIT}
 * logger. Runs after the owner is saved, so its generated id is populated. The line
 * carries the owner id, the assigned customerCode, the registrationDate, the
 * assigned membershipLevel and the membershipNumber.
 *
 * <p>Besides that human-readable line, it emits an immutable structured event as a
 * JSON object {@code {seq, ownerId, customerCode, membershipLevel, event}} with
 * {@code event = "OWNER_CREATED"}, where {@code seq} is a process-wide monotonically
 * increasing integer across creates. The event carries the owner's <em>current
 * primary identifier</em> ({@link #primaryIdentifier(Owner)}): the customerCode here,
 * and whatever replaces it later (e.g. a unified memberId).
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Process-wide sequence, monotonically increasing across every create. */
    private static final AtomicLong SEQ = new AtomicLong(0);

    public void service(@Val Owner owner) {
        String membershipNumber = owner.getCustomerCode() == null || owner.getRegistrationDate() == null
                ? null
                : owner.getCustomerCode() + "-M" + String.format("%02d", FiscalYears.yearSegment(owner.getRegistrationDate()));
        int membershipLevel = MembershipLevels.forOwner(owner);
        audit.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                membershipLevel, membershipNumber);

        ObjectNode event = JSON.createObjectNode();
        event.put("seq", SEQ.incrementAndGet());
        event.put("ownerId", owner.getId());
        event.put("customerCode", primaryIdentifier(owner));
        event.put("membershipLevel", membershipLevel);
        event.put("event", "OWNER_CREATED");
        audit.info(JSON.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier. Today that is the customerCode; when the
     * customerCode is later unified into the memberId, change this single method so the
     * event carries the memberId instead.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
