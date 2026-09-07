package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues the welcome notification for a newly created owner. Runs after {@link SaveOwner} (so the
 * owner's id is assigned) and after {@link DeduplicateMemberId} (so the member id is final).
 *
 * <p>The notification is emitted as a single line to the dedicated {@code NOTIFY} logger carrying
 * the new owner's id and unified {@link MemberId member id}.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome notification queued for owner id={} memberId={}",
                owner.getId(), owner.getMemberId());
    }
}
