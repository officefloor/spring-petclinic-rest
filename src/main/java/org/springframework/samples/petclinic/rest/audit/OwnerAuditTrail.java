package org.springframework.samples.petclinic.rest.audit;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

/**
 * Single sink for owner audit output. All entries go to the dedicated {@code AUDIT} logger so the
 * audit trail has one owner. On each create it emits both the human-readable audit line and an
 * immutable {@link OwnerCreatedEvent} rendered as JSON, stamping the event with the next value of a
 * process-wide monotonic sequence.
 */
@Component
public class OwnerAuditTrail {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final AtomicLong sequence = new AtomicLong();

    /**
     * Records a successful owner create: the human-readable line and the structured
     * {@code OWNER_CREATED} event. The {@code membershipNumber} is derived by the caller so this
     * sink stays independent of the web mapping layer.
     */
    public void ownerCreated(Owner owner, String membershipNumber) {
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), membershipNumber);
        OwnerCreatedEvent event = OwnerCreatedEvent.of(this.sequence.incrementAndGet(), owner);
        AUDIT.info("{}", event.toJson(JSON));
    }
}
