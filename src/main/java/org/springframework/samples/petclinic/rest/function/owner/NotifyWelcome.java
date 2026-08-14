package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that enqueues a welcome notification for the
 * newly created owner by emitting a line on the dedicated {@code NOTIFY} logger.
 *
 * <p>Runs after the owner is saved (so the id is assigned) and after
 * {@link AssignMemberId} (so the memberId is set); the emitted line carries both
 * the owner id and the memberId.
 */
public class NotifyWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
