package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits an audit line on the dedicated {@code AUDIT} logger after a successful create,
 * carrying the newly-assigned owner id, its memberId, its registrationDate and its
 * numeric membershipLevel. Runs after {@code save}, so the owner's generated id is
 * available. The logged level is the household-capped one, so it matches what the response
 * and later reads return.
 *
 * <p>Alongside the human-readable line, emits an immutable structured event on the same
 * {@code AUDIT} logger. The event is now schema version 2: a JSON object
 * {@code {seq, schemaVersion:2, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}} where
 * {@code seq} is a monotonically increasing integer across creates. The event carries the
 * owner's primary identifier via {@link #primaryIdentifier(Owner)} — the version-2 memberId.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates in this instance. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository) {
        int membershipLevel = HouseholdLevelCap.cappedMembershipLevel(owner, ownerMapper, ownerRepository);
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);

        long seq = SEQ.incrementAndGet();
        AUDIT.info("{\"seq\":{},\"schemaVersion\":2,\"ownerId\":{},\"memberId\":{},\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            seq, owner.getId(), jsonString(primaryIdentifier(owner)), membershipLevel);
    }

    /** The owner's primary identifier carried by the structured event: the {@code memberId}. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /** Renders a value as a JSON string literal (quoted, with the minimal escaping), or {@code null}. */
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
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
