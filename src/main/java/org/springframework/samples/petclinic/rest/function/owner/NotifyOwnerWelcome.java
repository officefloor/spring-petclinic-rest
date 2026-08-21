package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner. Runs after the owner has been saved so
 * the assigned id is available. Emits a single line to the dedicated {@code NOTIFY} logger carrying
 * the owner id and the memberId.
 */
public class NotifyOwnerWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome notification: id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
