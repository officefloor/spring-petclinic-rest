package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link SaveOwner}, once the owner has been persisted and
 * its id assigned. Enqueues a welcome notification by emitting a single line on the dedicated
 * {@code NOTIFY} logger carrying the new owner's id and {@code memberId}, so downstream delivery can
 * greet the member by their live external handle.
 *
 * Read-only: it inspects the stored owner without mutating it, then hands off to the next step.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner: id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
