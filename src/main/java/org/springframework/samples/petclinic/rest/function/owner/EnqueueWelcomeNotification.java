package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner by emitting a line to the dedicated
 * {@code NOTIFY} logger. Runs after {@link SaveOwner} (so the owner id is assigned) and after
 * {@link AssignMemberId} (so the {@code memberId} is set). The line carries the owner id and the
 * {@code memberId}.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
