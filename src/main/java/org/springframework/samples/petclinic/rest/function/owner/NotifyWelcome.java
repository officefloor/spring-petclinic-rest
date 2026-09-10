package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification for a successfully created owner by emitting a line,
 * via the dedicated {@code NOTIFY} logger, carrying the owner id and the assigned
 * memberId. Runs after Save so the generated id is populated.
 */
public class NotifyWelcome {

    private static final Logger notify = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        notify.info("Welcome owner: id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
