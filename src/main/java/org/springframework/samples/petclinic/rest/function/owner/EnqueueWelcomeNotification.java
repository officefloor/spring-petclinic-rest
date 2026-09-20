package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues the welcome notification for a successfully created owner by emitting, to the
 * dedicated {@code NOTIFY} logger, a line carrying the owner id and {@code memberId}. Runs
 * after {@link SaveOwner} (so the generated id exists) and {@link AssignOwnerMemberId} (so
 * the {@code memberId} is assigned).
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
