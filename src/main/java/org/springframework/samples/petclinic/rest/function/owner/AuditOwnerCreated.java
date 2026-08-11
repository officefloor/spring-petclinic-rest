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
 * carrying the newly-assigned owner id, its customerCode, its registrationDate, its
 * numeric membershipLevel and its membershipNumber. Runs after {@code save}, so the
 * owner's generated id is available. The logged level is the household-capped one, so it
 * matches what the response and later reads return.
 *
 * <p>Alongside the human-readable line, emits an immutable structured event on the same
 * {@code AUDIT} logger: a JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}} where
 * {@code seq} is a monotonically increasing integer across creates. The event carries the
 * owner's current primary identifier via {@link #primaryIdentifier(Owner)} — the
 * customerCode today, and whatever replaces it later.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates in this instance. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository) {
        int membershipLevel = HouseholdLevelCap.cappedMembershipLevel(owner, ownerMapper, ownerRepository);
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            membershipLevel, ownerMapper.membershipNumber(owner));

        long seq = SEQ.incrementAndGet();
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"customerCode\":{},\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            seq, owner.getId(), jsonString(primaryIdentifier(owner)), membershipLevel);
    }

    /**
     * The owner's current primary identifier carried by the structured event. Today that is the
     * {@code customerCode}; when the customerCode is later unified into the {@code memberId},
     * return that here instead and the event carries the memberId with no other change.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
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
