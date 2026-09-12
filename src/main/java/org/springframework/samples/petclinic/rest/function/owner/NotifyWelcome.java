package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a successfully created owner by emitting, on the
 * dedicated {@code NOTIFY} logger, a line carrying the owner id and the memberId. Runs
 * after {@link SaveOwner} (so the generated id is available) and after
 * {@link AssignMemberId} (so the memberId is set).
 */
public class NotifyWelcome {

    private static final Logger notify = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        notify.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
