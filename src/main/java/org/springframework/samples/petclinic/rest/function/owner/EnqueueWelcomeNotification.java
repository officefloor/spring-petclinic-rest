package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner by emitting a line to the dedicated
 * {@code NOTIFY} logger carrying the assigned owner id and the unified {@code memberId}.
 *
 * <p>Runs after {@link SaveOwner}, so the generated id is available.
 */
public class EnqueueWelcomeNotification {

    private static final Logger logger = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        logger.info("Welcome notification enqueued: id={} memberId={}",
                owner.getId(), owner.getMemberId());
    }
}
