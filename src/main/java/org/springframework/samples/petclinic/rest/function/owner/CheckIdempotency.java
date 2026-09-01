package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.variable.Out;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * First step of the create pipeline: if the request carries an {@code Idempotency-Key} already
 * seen, branch to respond with the originally created owner; otherwise publish the key for the
 * recording step and continue the normal create.
 */
public class CheckIdempotency {

    @FunctionalInterface
    public interface Existing {
        void send(Integer ownerId);
    }

    public void service(@RequestHeader(name = "Idempotency-Key", required = false) String key,
            IdempotencyStore store, Out<String> idempotencyKey, @Flow("existing") Existing existing) {
        if (key != null) {
            Integer ownerId = store.find(key);
            if (ownerId != null) {
                existing.send(ownerId);
                return;
            }
            idempotencyKey.set(key);
        }
    }
}
