package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a successfully created owner by emitting a line to the
 * dedicated {@code NOTIFY} logger carrying the owner id and the unified {@code memberId}. Runs after
 * the owner has been saved (so the generated id is available) and before the response is sent.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome notification enqueued: id={} memberId={}", owner.getId(),
                owner.getMemberId());
    }
}
