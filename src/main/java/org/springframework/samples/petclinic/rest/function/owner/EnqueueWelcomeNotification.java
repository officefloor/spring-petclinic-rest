package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues the welcome notification for a newly created owner. Runs after {@link SaveOwner} has
 * persisted the entity and {@link AssignMemberId} has set the member id, so both the generated id
 * and the member id are available. On the dedicated {@code NOTIFY} logger it emits a single line
 * carrying the owner id and member id, the hand-off to whatever delivers the welcome message.
 * Purely a side-effect step; it leaves the owner unchanged for the responder.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner: id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
