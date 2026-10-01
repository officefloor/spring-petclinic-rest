package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.OwnerRegion;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field.
 *
 * <p>Runs first in the {@code POST /api/owners} pipeline. It binds the body once (this is the only
 * step that reads {@code @RequestBody}) and republishes it for {@link BuildOwner}. firstName,
 * lastName, city and telephone must be present and non-blank, and an address must be supplied in
 * either the structured form (a non-blank addressLine1, with an optional addressLine2) or the flat
 * 'address' form; any missing field is collected and reported together via
 * {@link OwnerFieldsInvalidException}. A supplied registrationDate must not be later than the
 * server's current date.
 *
 * <p>The telephone is converted to E.164 form (see {@link OwnerTelephone#toE164}). The E.164 value
 * replaces the submitted one on the republished body, so it is what gets stored and returned. A
 * number that cannot form valid E.164 is rejected as an invalid {@code telephone}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            errors.add("lastName");
        }
        // The address may be supplied in a structured form (addressLine1 plus an optional
        // addressLine2) or the flat 'address' form; the structured form is preferred when present.
        // Each supplied field is normalized (whitespace collapsed, upper-cased, abbreviations
        // expanded). An owner is valid when it supplies an address in EITHER form — a non-blank
        // addressLine1 or a non-blank flat address — so earlier minimal (flat) owners stay accepted.
        // The normalized values replace the submitted ones, so they are what get stored, returned
        // and (via the composed 'address') compared; the composed 'address' is the normalized
        // addressLine1 with the normalized addressLine2 appended after a single space when present.
        String addressLine1 = OwnerAddress.normalize(request.getAddressLine1());
        String flatAddress = OwnerAddress.normalize(request.getAddress());
        if (!addressLine1.isEmpty()) {
            String addressLine2 = OwnerAddress.normalize(request.getAddressLine2());
            request.setAddressLine1(addressLine1);
            request.setAddressLine2(addressLine2.isEmpty() ? null : addressLine2);
            request.setAddress(OwnerAddress.compose(addressLine1, addressLine2));
        }
        else if (!flatAddress.isEmpty()) {
            request.setAddressLine1(null);
            request.setAddressLine2(null);
            request.setAddress(flatAddress);
        }
        else {
            errors.add("address");
        }
        if (isBlank(request.getCity())) {
            errors.add("city");
        }
        if (isBlank(request.getTelephone())) {
            errors.add("telephone");
        }
        else {
            String telephone = OwnerTelephone.toE164(request.getTelephone());
            if (telephone == null) {
                errors.add("telephone");
            }
            else {
                request.setTelephone(telephone);
            }
        }
        // Email is optional. When present it must be syntactically valid and its domain must not be a
        // known disposable provider; it is normalized to lower case so it is stored and returned
        // lower-cased.
        String email = request.getEmail();
        if (email != null) {
            if (!OwnerEmail.isValid(email) || OwnerEmail.isDisposable(email)) {
                errors.add("email");
            }
            else {
                request.setEmail(OwnerEmail.normalize(email));
            }
        }
        // Postcode is optional. When present it must be exactly four digits and, for a city with a
        // known region, fall within that region's postcode range; a city with no known region
        // accepts any 4-digit postcode. An absent postcode leaves the owner backward-compatible.
        String postcode = request.getPostcode();
        if (postcode != null && !OwnerRegion.isPostcodeValid(request.getCity(), postcode)) {
            errors.add("postcode");
        }
        // Registration date is optional and defaults to the server date when omitted. When supplied
        // it may not be in the future: a date later than the server's current date is rejected.
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            errors.add("registrationDate");
        }
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
