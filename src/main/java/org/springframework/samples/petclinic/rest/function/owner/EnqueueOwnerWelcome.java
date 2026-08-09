package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a newly created owner by emitting a line via
 * the dedicated {@code NOTIFY} logger. Runs after {@link SaveOwner} so the persisted
 * owner id is available, and after {@link AssignOwnerMemberId} so the assigned
 * {@code memberId} is set. The emitted line carries both the owner id and the
 * {@code memberId} so downstream delivery can address the new member.
 */
public class EnqueueOwnerWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
