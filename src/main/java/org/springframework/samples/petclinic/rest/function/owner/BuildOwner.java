package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built,
            Out<Boolean> sharesHousehold)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException,
            InvalidPostcodeException, FutureRegistrationDateException {
        // Publish the request-only 'sharesHousehold' flag for the household duplicate check.
        // Not part of the Owner entity, so it travels as a separate variable.
        sharesHousehold.set(Boolean.TRUE.equals(request.getSharesHousehold()));
        // Normalize the address up front so the required-field check below rejects an address that
        // is blank once normalized, and every later comparison sees the stored normalized form.
        request.setAddress(Addresses.normalize(request.getAddress()));
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(request.getAddress())) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        // Normalize the telephone to E.164 form (keep '+'/country code, else assume '+61').
        request.setTelephone(Telephones.toE164(request.getTelephone()));
        // Email is optional; when present it must be syntactically valid and is stored lower-cased.
        request.setEmail(OwnerEmails.normalize(request.getEmail()));
        // Postcode is optional; when present it must be valid for the city's region
        // (a city with no known region accepts any 4-digit postcode). The 4-digit shape
        // is enforced by the request schema, so only the region range is checked here.
        Postcodes.validate(request.getCity(), request.getPostcode());
        Owner owner = ownerMapper.toOwner(request);
        // The effective registration date is the supplied value, or the server's current date when
        // none is supplied. Either way it must fall on a business day: a Saturday or Sunday rolls
        // forward to the next Monday. Everything derived from the registration date (membership
        // number year, daily create-limit) then sees this adjusted business day.
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        } else if (registrationDate.isAfter(LocalDate.now())) {
            // A supplied registration date may not be in the future.
            throw new FutureRegistrationDateException(
                    "registrationDate " + registrationDate + " is later than the server date");
        }
        owner.setRegistrationDate(toBusinessDay(registrationDate));
        built.set(owner);
    }

    /** Roll a weekend date forward to the next Monday; business days are returned unchanged. */
    private static LocalDate toBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
