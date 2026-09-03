package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an immutable structured {@code OWNER_CREATED} event to the {@code AUDIT} logger
 * once the owner has been saved. The event carries a monotonically increasing {@code seq}
 * across creates, the owner id, its membership level and its current primary identifier
 * (the customerCode today; whatever unifies it later, e.g. the memberId). Keeping the
 * identifier read in {@link #identifier(Owner)} means only that one line moves when the
 * primary identifier changes.
 */
public class AuditOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        long seq = SEQ.incrementAndGet();
        int membershipLevel = ownerMapper.toOwnerDto(owner).getMembershipLevel();
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"memberId\":\"{}\",\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            seq, owner.getId(), identifier(owner), membershipLevel);
    }

    /** The owner's primary identifier, the unified memberId. */
    private static String identifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
