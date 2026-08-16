package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that enqueues a welcome notification for a newly created owner
 * by emitting a line on the dedicated {@code NOTIFY} logger. Runs after {@link SaveOwner} so the
 * owner has its persisted id, and after {@link AssignMemberId} so the {@code memberId} is set; the
 * emitted line carries both the owner id and the assigned {@code memberId}.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
