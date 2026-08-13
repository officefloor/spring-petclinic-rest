package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification on successful create by emitting a single line — carrying the
 * newly-persisted owner's id and {@code memberId} — to the dedicated {@code NOTIFY} logger. Runs
 * after {@link SaveOwner} (so the owner has its generated id) and its assigned
 * {@link Owner#getMemberId() memberId}.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
