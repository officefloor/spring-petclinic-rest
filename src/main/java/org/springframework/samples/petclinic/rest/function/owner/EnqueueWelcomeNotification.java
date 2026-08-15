package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues the welcome notification for a newly created owner by emitting a single line via the
 * dedicated {@code NOTIFY} logger, carrying the owner's generated id and its {@code memberId}.
 *
 * <p>Runs after {@link SaveOwner} so the generated id is present and {@link AssignMemberId} so the
 * {@code memberId} is set, within the same write transaction as the insert.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
