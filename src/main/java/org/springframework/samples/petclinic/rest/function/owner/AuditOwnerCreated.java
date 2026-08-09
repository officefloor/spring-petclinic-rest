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
 * {@code memberId}, the effective {@code registrationDate} and the numeric
 * {@code membershipLevel}; and</li>
 * <li>an immutable structured schema-version-2 event as a JSON object
 * {@code {schemaVersion:2, seq, ownerId, customerCode, membershipLevel, ownerSegment,
 * event:'OWNER_CREATED'}}, where {@code seq} is a process-wide monotonically
 * increasing integer across creates and {@code ownerSegment} is recomputed from the
 * version-2 identity.</li>
 * </ul>
 *
 * <p>The structured event carries the owner's <em>current primary identifier</em>,
 * now the {@code memberId}, read through {@link #primaryIdentifier(Owner)}. The
 * JSON key is retained as {@code customerCode} for backwards compatibility, but
 * its value tracks whatever the primary identifier is.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Process-wide monotonically increasing sequence across all owner creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                ownerMapper.membershipLevel(owner));

        long seq = SEQUENCE.incrementAndGet();
        AUDIT.info(event(seq, owner.getId(), primaryIdentifier(owner),
                ownerMapper.membershipLevel(owner), ownerMapper.ownerSegment(owner)));
    }

    /**
     * The owner's current primary identifier: the {@code memberId} that unifies the
     * former customerCode and membershipNumber. This is the single place to switch,
     * and every emitted event follows.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Render the immutable structured event as a schema-version-2 JSON object
     * {@code {schemaVersion:2, seq, ownerId, customerCode, membershipLevel, ownerSegment,
     * event:'OWNER_CREATED'}}. Version 2 adds {@code schemaVersion} and the
     * {@code ownerSegment}, recomputed from the version-2 identity.
     */
    private static String event(long seq, Integer ownerId, String customerCode,
            Integer membershipLevel, String ownerSegment) {
        return "{"
                + "\"schemaVersion\":2,"
                + "\"seq\":" + seq + ","
                + "\"ownerId\":" + ownerId + ","
                + "\"customerCode\":" + quote(customerCode) + ","
                + "\"membershipLevel\":" + membershipLevel + ","
                + "\"ownerSegment\":" + quote(ownerSegment) + ","
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
