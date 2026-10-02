package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a successfully created owner by emitting, on the dedicated
 * {@code NOTIFY} logger, a line carrying the owner id (assigned by the preceding save) and the
 * {@link Owner#getMemberId() memberId}.
 */
public class EnqueueWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome notification queued id={} memberId={}", owner.getId(),
                owner.getMemberId());
    }
}
