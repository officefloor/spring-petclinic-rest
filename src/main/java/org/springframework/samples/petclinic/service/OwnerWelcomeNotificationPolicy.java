package org.springframework.samples.petclinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on successful creation a welcome notification is enqueued by emitting a line via the
 * dedicated {@code NOTIFY} logger carrying the new owner's id and its {@code memberId}.
 * Kept as a small, self-contained unit so the rule can be applied from the create flow after the
 * owner has been saved without adding complexity to the controller or service.
 */
public final class OwnerWelcomeNotificationPolicy {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    private OwnerWelcomeNotificationPolicy() {
    }

    /** Enqueue the welcome notification for a freshly saved owner. */
    public static void enqueueWelcome(Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
