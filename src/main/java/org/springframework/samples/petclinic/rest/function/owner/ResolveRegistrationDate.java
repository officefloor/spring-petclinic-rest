package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the effective registration date for a create request and publishes it as a
 * variable. The effective date is the one supplied in the request, or the server's current
 * date when omitted; either way, a weekend is rolled forward to the next business day (see
 * {@link BusinessDays}). Runs before {@link EnsureDailyLimit} and {@link ApplyRegistrationDate}
 * so the per-day limit counts against, and the built owner is stamped with, the same adjusted
 * business day — and so everything derived from it (e.g. the membership-number year segment)
 * uses the adjusted date.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        registrationDate.set(BusinessDays.rollForward(effective));
    }
}
