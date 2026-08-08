package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits audit output for a newly created owner via the dedicated {@code AUDIT}
 * logger. Runs after {@link SaveOwner} so the persisted owner id is available.
 *
 * <p>Two things are emitted:
 * <ul>
 * <li>a human-readable audit line carrying the owner id, the assigned
 * {@code customerCode}, the effective {@code registrationDate}, the numeric
 * {@code membershipLevel} and the {@code membershipNumber}; and</li>
 * <li>an immutable structured event as a JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}},
 * where {@code seq} is a process-wide monotonically increasing integer across
 * creates.</li>
 * </ul>
 *
 * <p>The structured event carries the owner's <em>current primary
 * identifier</em>. Today that identifier is the {@code customerCode}; it is read
 * through {@link #primaryIdentifier(Owner)} so that when the customerCode is
 * later unified into the memberId, the event automatically carries the memberId
 * instead — the JSON key stays {@code customerCode} but its value tracks
 * whatever the primary identifier has become.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Process-wide monotonically increasing sequence across all owner creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                ownerMapper.membershipLevel(owner), ownerMapper.membershipNumber(owner));

        long seq = SEQUENCE.incrementAndGet();
        AUDIT.info(event(seq, owner.getId(), primaryIdentifier(owner),
                ownerMapper.membershipLevel(owner)));
    }

    /**
     * The owner's current primary identifier. Currently the {@code customerCode};
     * when the customerCode is unified into the memberId, this becomes the single
     * place to switch, and every emitted event follows.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * Render the immutable structured event as a JSON object
     * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}.
     */
    private static String event(long seq, Integer ownerId, String customerCode, Integer membershipLevel) {
        return "{"
                + "\"seq\":" + seq + ","
                + "\"ownerId\":" + ownerId + ","
                + "\"customerCode\":" + quote(customerCode) + ","
                + "\"membershipLevel\":" + membershipLevel + ","
                + "\"event\":\"OWNER_CREATED\""
                + "}";
    }

    /** JSON-encode a string value, or {@code null} literal when absent. */
    private static String quote(String value) {
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
