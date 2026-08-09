package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.core.MethodParameter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Resolves the EFFECTIVE registration date and rolls it onto a business day.
 *
 * <p>The effective date is the one supplied in the request, or the server's current date when the
 * request omits it. A supplied date later than the server's current date is rejected with 400 via
 * {@link MethodArgumentNotValidException} — an owner cannot be registered in the future. Otherwise,
 * when that date lands on a Saturday, Sunday or listed public holiday it is rolled forward to the
 * next non-holiday business day. The adjusted
 * date is published as a variable so every later step derives from the same value: the daily
 * create-limit counts owners per adjusted business day, {@link BuildOwner} stores it as the owner's
 * {@code registrationDate}, and {@link AssignMembershipNumber} takes the membership number's year
 * segment from it.
 *
 * <p>Runs before {@link EnsureDailyOwnerLimit} so the limit is checked against the adjusted date.
 */
public class NormalizeRegistrationDate {

    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    private static final Method SERVICE_METHOD;

    static {
        try {
            SERVICE_METHOD = NormalizeRegistrationDate.class.getMethod("service", OwnerFieldsDto.class, Out.class);
        }
        catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate)
            throws MethodArgumentNotValidException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            BindingResult binding = new BeanPropertyBindingResult(request, "ownerFieldsDto");
            binding.rejectValue("registrationDate", "FutureDate",
                "registration date must not be later than the current date");
            throw new MethodArgumentNotValidException(new MethodParameter(SERVICE_METHOD, 0), binding);
        }
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        registrationDate.set(toBusinessDay(effective));
    }

    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
