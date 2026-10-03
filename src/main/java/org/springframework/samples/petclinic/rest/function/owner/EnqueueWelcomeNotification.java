package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a freshly created owner by emitting a line via the
 * dedicated {@code NOTIFY} logger carrying the owner id and the memberId. Runs after
 * {@link SaveOwner} and {@link AssignMemberId}, so both the owner id and memberId have
 * been assigned.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome notification enqueued: ownerId={} memberId={}",
                owner.getId(), owner.getMemberId());
    }
}
