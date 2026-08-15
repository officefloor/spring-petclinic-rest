package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a create whose firstName, lastName,
 * address, city or telephone is missing or blank before {@link BuildOwner} maps the body,
 * so an incomplete request is a 400 listing each field rather than a persisted owner.
 * Also normalizes the telephone to E.164 form (see {@link TelephoneE164}), storing the E.164
 * value on the body and rejecting a number that cannot form a valid E.164 string. An optional
 * email, when present, must be a syntactically valid address and is stored lower-cased.
 * Publishes the validated body for the later steps.
 */
public class RequireOwnerFields {

    /** Pragmatic syntactic check: one or more non-space/@ characters, an '@', a domain label,
     *  a dot, and a top-level label. Rejects inputs such as "not-an-email". */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        List<String> missing = new ArrayList<>();
        // Normalize the address up-front so the blank check below rejects an address that is
        // empty only after normalization (e.g. all-whitespace), and so the stored/returned value
        // and every later address comparison use the one canonical form.
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
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
        request.setTelephone(TelephoneE164.normalize(request.getTelephone()));
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            if (!EMAIL.matcher(normalized).matches()) {
                throw new InvalidEmailException(email);
            }
            request.setEmail(normalized);
        }
        else {
            request.setEmail(null);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
