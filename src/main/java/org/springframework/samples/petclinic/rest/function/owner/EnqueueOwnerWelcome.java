package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner by emitting, via the dedicated
 * {@code NOTIFY} logger, a single line carrying the new owner's id and its assigned
 * {@code memberId}. Runs after {@link SaveOwner} (so the id is populated) and before
 * {@link RespondWithOwnerCreated}.
 *
 * <p>Like {@link AuditOwnerCreated} it reads no repository but stays under the pipeline's
 * {@code transaction} governance so the create commits as a single boundary; an ungoverned
 * step here would split that boundary.
 */
public class EnqueueOwnerWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
