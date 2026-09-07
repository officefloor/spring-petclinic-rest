package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Emits the create audit trail. Runs after the owner is saved (so its id is
 * assigned) and before the response is sent.
 *
 * <p>Two things are written to the dedicated {@code AUDIT} logger:
 * <ol>
 * <li>the human-readable audit line carrying the new owner's id, customer code,
 *     registration date, derived membership level and membership number; and</li>
 * <li>an immutable, machine-readable {@code OWNER_CREATED} event as a single JSON
 *     object {@code {seq, ownerId, customerCode, membershipLevel, event}}.</li>
 * </ol>
 *
 * <p>{@code seq} is a monotonically increasing integer across creates. The event
 * carries the owner's <em>current primary identifier</em> — the customer code today;
 * see {@link #primaryIdentifier(Owner)} for where this changes when the customer code
 * is later unified into the member id.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates (process-wide). */
    private static final AtomicLong SEQ = new AtomicLong();

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner), membershipNumber(owner));

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), membershipLevel(owner));
        AUDIT.info(MAPPER.writeValueAsString(event));
    }

    /**
     * The owner's current primary identifier. Today this is the customer code; when the
     * customer code is unified into the member id, this returns the member id instead and
     * every emitted event follows automatically.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /** Stored membership level if the pipeline has stamped one, else the derived level. */
    private static int membershipLevel(Owner owner) {
        return owner.getMembershipLevel() != null ? owner.getMembershipLevel() : MembershipLevel.of(owner);
    }

    private static String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
    }

    /**
     * Immutable structured create event. Field order is the serialized JSON key order:
     * {@code {seq, ownerId, customerCode, membershipLevel, event}}.
     */
    public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel,
            String event) {

        public OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel) {
            this(seq, ownerId, customerCode, membershipLevel, "OWNER_CREATED");
        }
    }
}
