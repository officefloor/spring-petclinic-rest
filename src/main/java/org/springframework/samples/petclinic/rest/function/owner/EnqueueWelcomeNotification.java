package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a freshly created owner by emitting a line via the
 * dedicated {@code NOTIFY} logger. Runs after the owner is saved, so its generated id is
 * populated. The line carries the owner id and the assigned memberId.
 */
public class EnqueueWelcomeNotification {

    private static final Logger notify = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        notify.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
