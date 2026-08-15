package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that enqueues a welcome notification for a successful create.
 * Runs after {@link SaveOwner} (so the owner's generated id is available) and before
 * {@link RespondWithOwnerCreated}.
 *
 * <p>Emits a single line to the dedicated {@code NOTIFY} logger carrying the owner id and the
 * assigned {@code memberId}, so the newly registered owner can be greeted.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
