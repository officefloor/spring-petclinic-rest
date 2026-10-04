package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner by emitting a line on the
 * dedicated {@code NOTIFY} logger carrying the owner's id and unified {@code memberId}.
 * Runs after the owner has been saved (so it has an id) and the memberId assigned.
 */
public class EnqueueWelcomeNotification {

    private static final Logger notify = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        notify.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
