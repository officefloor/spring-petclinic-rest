package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Defaults a create-owner request's registration date to the server's current date when the
 * client supplied none. A supplied date is left untouched, so it round-trips unchanged.
 *
 * <p>Runs after {@link BuildOwner} has mapped the request onto the entity and mutates that same
 * entity in place (via {@code @Val}), so {@link SaveOwner} persists the resolved date.
 */
public class DefaultRegistrationDate {

    public void service(@Val Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
