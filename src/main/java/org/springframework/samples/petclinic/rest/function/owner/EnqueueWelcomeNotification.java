package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: enqueues the new owner's welcome notification. Runs after
 * {@link SaveOwner} so the owner carries its persisted id, emitting one line to the dedicated
 * {@code NOTIFY} logger with the owner id and {@code memberId} (see {@link OwnerIdentity}).
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), OwnerIdentity.primary(owner));
    }
}
