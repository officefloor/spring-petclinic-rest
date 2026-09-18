package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.notify.WelcomeNotifier;

/**
 * Enqueues the welcome notification for a successful owner create in {@code POST /api/owners},
 * delegating to {@link WelcomeNotifier} which emits it to the dedicated {@code NOTIFY} logger. Runs
 * after {@link SaveOwner} so the generated id is available, and before the response is sent.
 */
public class EnqueueWelcomeNotification {

    public void service(@Val Owner owner, WelcomeNotifier welcomeNotifier) {
        welcomeNotifier.welcome(owner);
    }
}
