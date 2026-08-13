package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the effective registration date for a create-owner request and publishes it
 * for the rest of the {@code POST /api/owners} pipeline.
 *
 * <p>The effective date is the one supplied in the request body, or the server's
 * current date when the request omits it. Either way it must fall on a business day, so
 * a weekend date is rolled forward to the next Monday (see {@link BusinessDay}). This
 * single adjusted date drives every value derived from the registration date — the
 * per-day create-limit ({@link EnsureDailyCapacity}), the stored registration date and
 * the membership number's year segment ({@link BuildOwner}) — so they all agree.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which publishes the request body) and
 * before {@link EnsureDailyCapacity}.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        registrationDate.set(BusinessDay.roll(effective));
    }
}
