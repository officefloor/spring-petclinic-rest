package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a freshly created owner by emitting a single line on the
 * dedicated {@code NOTIFY} logger carrying the owner id and its {@code memberId}. Runs after
 * {@link SaveOwner} so both the id and the {@code memberId} are assigned.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner: id={} memberId={}", owner.getId(), owner.getPrimaryIdentifier());
    }
}
