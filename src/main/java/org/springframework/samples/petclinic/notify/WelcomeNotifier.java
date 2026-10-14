package org.springframework.samples.petclinic.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.audit.OwnerCreatedEvent;
import org.springframework.stereotype.Component;

/**
 * Enqueues welcome notifications for newly created owners by emitting them to the {@code NOTIFY} logger.
 */
@Component
public class WelcomeNotifier {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Enqueues a welcome notification for the owner described by the given created event.
     */
    public void welcome(OwnerCreatedEvent event) {
        NOTIFY.info("Welcome notification enqueued: ownerId={}, memberId={}", event.ownerId(), event.memberId());
    }
}
