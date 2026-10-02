package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for each successful owner create by emitting a line on the
 * dedicated {@code NOTIFY} channel carrying the owner id and the memberId. Runs after the owner is
 * saved (so the id and memberId are populated) and before the responder.
 */
public class EnqueueWelcomeNotification {

    /** Dedicated notification channel; one welcome line per successful create. */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
