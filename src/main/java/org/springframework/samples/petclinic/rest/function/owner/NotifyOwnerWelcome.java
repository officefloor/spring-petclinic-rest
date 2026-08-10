package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification on successful create by emitting, via the dedicated
 * {@code NOTIFY} logger, a line carrying the new owner's id and its {@code memberId}.
 *
 * <p>Runs after {@link SaveOwner}, so the owner has been assigned its generated id and its
 * {@code memberId}, and before {@link RespondWithOwnerCreated}, so the notification is enqueued
 * only once the create has succeeded.
 */
public class NotifyOwnerWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
