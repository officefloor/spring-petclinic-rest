package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Fiscal-year helpers. The fiscal year starts on 1 July, so a date in July..December
 * belongs to the fiscal year of the following calendar year. {@link #label} formats it as
 * {@code FY<YY>} from an owner's (business-day-adjusted) registration date, returning
 * {@code null} when that date is absent.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    public static int of(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    public static String label(Owner owner) {
        LocalDate registration = owner.getRegistrationDate();
        return registration == null ? null : String.format("FY%02d", of(registration) % 100);
    }
}
