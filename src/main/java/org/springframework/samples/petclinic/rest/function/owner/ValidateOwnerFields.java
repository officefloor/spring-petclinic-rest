package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any of firstName, lastName,
 * address, city or telephone, before {@link BuildOwner} runs. An address may be supplied in
 * either form — a non-blank structured {@code addressLine1} (optionally with {@code addressLine2})
 * or the flat {@code address}; the structured form is preferred and composed into {@code address}
 * (see {@link OwnerAddress}), so a request giving only structured fields still satisfies the
 * address requirement. Runs first so an incomplete body is a 400 naming the offending fields.
 * Also normalizes the telephone into E.164 form
 * (see {@link OwnerTelephone}), rejecting otherwise with a 400; the normalized value is
 * stored on the body so it is persisted and returned.
 * When an {@code email} is present it must be a syntactically valid address and is stored
 * lower-cased (rejected with 400 otherwise); an absent email is allowed.
 * Publishes the validated body for later steps.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidOwnerTelephoneException, InvalidOwnerEmailException,
            DisposableOwnerEmailException, InvalidOwnerPostcodeException, FutureRegistrationDateException {
        // Normalize whichever address fields are supplied up front, so the required-field check
        // below rejects a blank-after-normalization value and the persisted/returned value is the
        // normalized form. The structured fields are preferred when present: addressLine1 (with
        // addressLine2 appended when given) is composed into 'address', which every later step
        // reads; otherwise the flat 'address' input is used, keeping the contract backward-compatible.
        String line1 = OwnerAddress.normalize(request.getAddressLine1());
        String line2 = OwnerAddress.normalize(request.getAddressLine2());
        request.setAddressLine1(line1);
        request.setAddressLine2(line2);
        request.setAddress(OwnerAddress.normalize(request.getAddress()));
        if (line1 != null && !line1.isBlank()) {
            request.setAddress(OwnerAddress.compose(line1, line2));
        }
        List<String> missing = new ArrayList<>();
        checkField("firstName", request.getFirstName(), missing);
        checkField("lastName", request.getLastName(), missing);
        checkField("address", request.getAddress(), missing);
        checkField("city", request.getCity(), missing);
        checkField("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        OwnerEmail.normalize(request);
        OwnerPostcode.validate(request.getPostcode(), request.getCity());
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException("registrationDate must not be later than the server date");
        }
        validated.set(request);
    }

    private static void checkField(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
