package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner. Runs after {@link SaveOwner} so the
 * generated id is available, emitting a single line to the dedicated {@code NOTIFY} logger that
 * carries the owner's id and member id.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome notification enqueued: id={} memberId={}",
                owner.getId(), owner.getMemberId());
    }
}
