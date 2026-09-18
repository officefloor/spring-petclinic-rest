package org.springframework.samples.petclinic.rest.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Single sink for owner welcome notifications. Each enqueued notification is emitted to the
 * dedicated {@code NOTIFY} logger, keyed by the owner's id and {@code memberId}, so the
 * notification stream has one owner and is independent of the {@code AUDIT} trail.
 */
@Component
public class WelcomeNotifier {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /** Enqueues a welcome notification for a freshly created {@code owner}. */
    public void welcome(Owner owner) {
        NOTIFY.info("welcome enqueued: id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
