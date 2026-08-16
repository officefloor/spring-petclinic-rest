package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Resolves the effective registration date and rolls it onto a business day, publishing it for
 * later steps. The effective date is the one supplied in the request, or the server's current date
 * when none was supplied. A supplied date later than the server's current date is rejected with 400
 * (see {@link FutureRegistrationDateException}); back-dating is allowed. When the effective date
 * falls on a weekend (Saturday or Sunday) or a listed public holiday it rolls forward to the next
 * non-holiday business day, so the stored {@code registrationDate} is always a business day.
 *
 * <p>Publishing the adjusted date here (before {@link CheckDailyLimit} and {@link BuildOwner}) means
 * every value derived from it stays consistent: the daily create-limit counts owners per adjusted
 * business day, and the membership number's year segment uses the adjusted year.
 */
public class ResolveRegistrationDate {

    /** Fixed public holidays the business-day roll skips, in addition to weekends. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate)
            throws FutureRegistrationDateException {
        LocalDate effective = request.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        else if (effective.isAfter(LocalDate.now())) {
            // A supplied date later than the server date is rejected; back-dating is allowed.
            throw new FutureRegistrationDateException(effective);
        }
        // A weekend or public holiday rolls forward to the next non-holiday business day.
        while (effective.getDayOfWeek() == DayOfWeek.SATURDAY
                || effective.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(effective)) {
            effective = effective.plusDays(1);
        }
        registrationDate.set(effective);
    }
}
