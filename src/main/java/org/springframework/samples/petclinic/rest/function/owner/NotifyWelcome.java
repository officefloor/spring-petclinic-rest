package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Enqueues a welcome notification on successful create by emitting a line to the dedicated
 * {@code NOTIFY} logger carrying the newly assigned owner id and the memberId.
 */
public class NotifyWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
