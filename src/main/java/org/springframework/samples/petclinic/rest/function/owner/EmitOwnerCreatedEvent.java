package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an immutable, structured {@code OWNER_CREATED} event on the {@code AUDIT} logger, alongside the
 * human-readable line from {@link AuditOwnerCreated}. Each event carries a monotonically increasing
 * {@code seq} across creates, the owner id, its membership level and the owner's current primary
 * identifier. That identifier is the customerCode today; when it is later unified into the memberId,
 * only {@link #primaryIdentifier(Owner)} changes and every event follows. Runs after Save so the id is
 * present.
 */
public class EmitOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"customerCode\":\"{}\",\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
                SEQ.incrementAndGet(), owner.getId(), primaryIdentifier(owner), OwnerMembershipLevel.of(owner));
    }

    /** The owner's current primary identifier — the customerCode, until it is unified into the memberId. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
