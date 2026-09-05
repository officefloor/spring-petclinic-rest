package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification on successful owner creation by emitting a line
 * via the dedicated {@code NOTIFY} logger carrying the owner id and the
 * {@code memberId}.
 *
 * <p>Runs after {@code SaveOwner} so the owner id has been assigned.
 */
public class NotifyWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
