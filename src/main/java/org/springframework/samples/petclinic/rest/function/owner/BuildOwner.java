package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.escalation.RegistrationDateInFutureException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    /** Simple syntactic email check: non-space local part, '@', non-space domain with a dot. */
    private static final java.util.regex.Pattern EMAIL =
            java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository, Out<Owner> built, Out<Boolean> sharesHousehold)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException,
            RegistrationDateInFutureException {
        // Normalize the address up front so the required-field check rejects an address that is
        // blank once trimmed/collapsed, and every later step (and the stored value) sees the
        // canonical form.
        String normalizedAddress = canonicalizeAddress(request);
        requireFields(request, normalizedAddress);
        request.setTelephone(TelephoneNormalizer.normalize(request.getTelephone()));
        request.setEmail(normalizeEmail(request.getEmail()));
        Owner owner = ownerMapper.toOwner(request);
        // The EFFECTIVE registration date (supplied or defaulted to the server date) must fall on
        // a business day: a Saturday or Sunday rolls forward to the next Monday. Every value
        // derived from the registration date (e.g. the membership number's year segment, the daily
        // create-limit's day) then uses this adjusted date.
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        java.time.LocalDate serverDate = java.time.LocalDate.now();
        if (registrationDate == null) {
            registrationDate = serverDate;
        } else if (registrationDate.isAfter(serverDate)) {
            // A supplied registration date cannot be in the future.
            throw new RegistrationDateInFutureException(registrationDate, serverDate);
        }
        owner.setRegistrationDate(toBusinessDay(registrationDate));
        owner.setCustomerCode(CustomerCode.of(owner));
        built.set(owner);
        sharesHousehold.set(Boolean.TRUE.equals(request.getSharesHousehold()));
    }

    /**
     * Canonicalize the address supplied on the request and write the canonical values back so the
     * mapped owner and every later step see them. Structured fields are preferred: the normalized
     * {@code addressLine1}/{@code addressLine2} are stored (or {@code null} when blank), and the
     * flat {@code address} becomes the composed, normalized string — the normalized addressLine1
     * with a single space and the normalized addressLine2 appended when present, else the
     * normalized flat address. Returns that composed address for the required-field check, so an
     * owner supplying an address in EITHER form (a non-blank addressLine1, or the flat address) is
     * accepted.
     */
    private static String canonicalizeAddress(OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String composed = AddressNormalizer.compose(line1, line2, request.getAddress());
        request.setAddressLine1(line1.isEmpty() ? null : line1);
        request.setAddressLine2(line2.isEmpty() ? null : line2);
        request.setAddress(composed);
        return composed;
    }

    /**
     * Enforce the owner's required fields, collecting every missing one so the caller reports them
     * together. {@code normalizedAddress} is the already-canonicalized address, so an address blank
     * once trimmed/collapsed counts as missing.
     */
    private static void requireFields(OwnerFieldsDto request, String normalizedAddress)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (normalizedAddress.isEmpty()) {
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
    }

    /**
     * Roll a registration date off the weekend: a Saturday or Sunday moves forward to the next
     * Monday; a weekday is returned unchanged.
     */
    private static java.time.LocalDate toBusinessDay(java.time.LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.plusDays(2);
            case SUNDAY:
                return date.plusDays(1);
            default:
                return date;
        }
    }

    /** When present, require a syntactically valid address and store it lower-cased; else reject with 400. */
    private static String normalizeEmail(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
