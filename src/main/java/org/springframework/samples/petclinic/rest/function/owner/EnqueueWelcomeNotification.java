package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * On a successful create, enqueues the welcome notification by emitting a line on the dedicated
 * {@code NOTIFY} logger carrying the new owner's id and its unified {@code memberId}. Runs after
 * {@link SaveOwner} (so the id is assigned) and {@link AssignMemberId} (so the memberId is set).
 */
public class EnqueueWelcomeNotification {

    private static final Logger notifyLogger = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        notifyLogger.info("Welcome notification queued ownerId={} memberId={}",
                owner.getId(), owner.getMemberId());
    }
}
