package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues the welcome notification for a freshly created owner by emitting a single line on the
 * dedicated {@code NOTIFY} logger. Runs after {@link SaveOwner} so the owner's generated {@code id}
 * is available, alongside its {@code memberId}.
 *
 * <p>The line carries both the owner id and the {@code memberId} so downstream notification
 * delivery can identify the new member.
 */
public class EnqueueWelcomeNotification {

    private static final Logger notify = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        notify.info("Welcome notification enqueued: id={} memberId={}", owner.getId(),
                owner.getMemberId());
    }
}
