package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a freshly created owner by emitting a line to the
 * dedicated {@code NOTIFY} logger. Runs after {@link SaveOwner} so the owner id and
 * {@code memberId} are assigned.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome notification queued for owner id={} memberId={}",
                owner.getId(), owner.getMemberId());
    }
}
