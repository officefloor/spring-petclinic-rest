package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link SaveOwner} on {@code POST /api/owners}, once the owner has been persisted and
 * assigned both an id and a {@code memberId}. Enqueues the owner's welcome notification by emitting a
 * line carrying the owner id and member id via the dedicated {@code NOTIFY} logger.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
