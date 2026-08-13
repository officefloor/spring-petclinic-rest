package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner by emitting a line on the
 * dedicated {@code NOTIFY} logger carrying the new owner's id and unified
 * {@code memberId}.
 *
 * <p>Runs after {@link SaveOwner} (so the id is assigned) and {@link AssignMemberId}
 * (so the {@code memberId} is set) in the {@code POST /api/owners} pipeline.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
