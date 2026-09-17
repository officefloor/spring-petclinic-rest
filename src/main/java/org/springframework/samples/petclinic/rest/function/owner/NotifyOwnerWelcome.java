package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier;

/**
 * Enqueues a welcome notification once a new owner has been persisted, by emitting a line to the
 * dedicated {@code NOTIFY} log carrying the generated owner id together with the assigned memberId
 * (the unified {@code <REGION><FY><HASH8><CHK>} identity, see {@link OwnerPrimaryIdentifier}).
 */
public class NotifyOwnerWelcome {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    public void service(@Val Owner owner) {
        NOTIFY.info("Welcome owner: id={} memberId={}", owner.getId(),
                OwnerPrimaryIdentifier.of(owner));
    }
}
