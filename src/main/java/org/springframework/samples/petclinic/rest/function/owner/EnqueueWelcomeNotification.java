package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner by emitting a line via the dedicated
 * {@code NOTIFY} logger carrying the persisted owner's id and {@code memberId}. Wired after
 * {@link SaveOwner} (so the id is assigned) and after {@link AssignOwnerMemberId} /
 * {@link DeduplicateOwnerMemberId} (so the member id is settled), so the enqueued line matches
 * what {@code GET /owners/{id}} later returns. Runs only on the create branch of the pipeline,
 * not on an idempotent repeat.
 */
public class EnqueueWelcomeNotification {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
