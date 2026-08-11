package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a freshly created {@link Owner} by emitting a line on the
 * dedicated {@code NOTIFY} logger carrying the owner's id and {@code memberId}.
 *
 * <p>Runs after {@link SaveOwner} so the owner's generated id is available, alongside the
 * {@code memberId} assigned earlier by {@link AssignMemberId}.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome notification enqueued: id={} memberId={}",
                owner.getId(), owner.getMemberId());
    }
}
