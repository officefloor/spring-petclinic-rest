package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.RequiredFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field,
 * before {@link BuildOwner} runs. The names of the offending fields are reported
 * as a 400 by {@link org.springframework.samples.petclinic.rest.escalation.RequiredFieldsExceptionHandler}.
 *
 * <p>This is a manual guard rather than {@code @Valid} so that a missing or blank
 * required field yields the {@code errors} field-name array this endpoint promises,
 * instead of the generic schema-validation problem detail.
 */
public class RequireOwnerFields {

    /** A pragmatic syntactic email check: a local part, an '@', and a dotted domain,
     *  none containing whitespace or a second '@'. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Disposable email domains that are never accepted, matched case-insensitively. */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws RequiredFieldsException {
        List<String> errors = new ArrayList<>();
        checkField("firstName", request.getFirstName(), errors);
        checkField("lastName", request.getLastName(), errors);
        // Address: the structured fields are preferred when present; the flat 'address'
        // input stays accepted for backward compatibility. An owner is valid when it
        // supplies an address in EITHER form — a non-blank 'addressLine1' or the flat
        // 'address'. Normalization applies to whichever fields are supplied, before the
        // required check, so a blank-after-normalization address is rejected and the
        // stored/returned values are canonical.
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());
        boolean structured = !line1.isEmpty();
        String effectiveLine1 = structured ? line1 : flat;
        if (effectiveLine1.isEmpty()) {
            errors.add("address");
        }
        else {
            // The composed 'address' is the normalized addressLine1, with a single space
            // and the normalized addressLine2 appended when addressLine2 is present.
            String composed = (structured && !line2.isEmpty())
                    ? effectiveLine1 + " " + line2 : effectiveLine1;
            request.setAddress(composed);
            request.setAddressLine1(structured ? line1 : null);
            request.setAddressLine2(structured && !line2.isEmpty() ? line2 : null);
        }
        checkField("city", request.getCity(), errors);
        checkField("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new RequiredFieldsException(errors);
        }
        // Normalize the telephone to E.164 form; reject when it cannot form a valid one.
        String telephone = Telephones.toE164(request.getTelephone());
        if (telephone == null) {
            throw new RequiredFieldsException(List.of("telephone"));
        }
        request.setTelephone(telephone);
        // Email is optional; when present it must be syntactically valid and is
        // stored and returned lower-cased.
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new RequiredFieldsException(List.of("email"));
            }
            String lowerEmail = email.toLowerCase();
            // Reject disposable-domain addresses: the domain (text after the last '@')
            // must not be on the blocklist.
            String domain = lowerEmail.substring(lowerEmail.lastIndexOf('@') + 1);
            if (DISPOSABLE_EMAIL_DOMAINS.contains(domain)) {
                throw new RequiredFieldsException(List.of("email"));
            }
            request.setEmail(lowerEmail);
        }
        // Postcode is optional; when present it must be a 4-digit code that is valid for
        // the owner's city per the fixed region ranges (a city with no known region accepts
        // any 4-digit code). Validated here only when supplied, so a request without a
        // postcode stays accepted.
        if (!Postcodes.isValid(request.getPostcode(), request.getCity())) {
            throw new RequiredFieldsException(List.of("postcode"));
        }
        validated.set(request);
    }

    private static void checkField(String name, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(name);
        }
    }
}
