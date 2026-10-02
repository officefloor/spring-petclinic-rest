package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates a new owner before {@link BuildOwner} runs: every one of firstName, lastName, address,
 * city and telephone must be present and non-blank. Any that are missing or blank are collected and
 * reported together as a {@link MissingOwnerFieldsException} (a 400), so the body is never persisted.
 *
 * <p>The telephone is then normalized to E.164 (keep a leading '+' and country code when present,
 * otherwise assume '+61' and drop a single leading '0' from the national digits, stripping spaces,
 * dashes and brackets and requiring 8 to 15 digits after the '+'); otherwise an
 * {@link InvalidTelephoneException} (a 400) is raised. The E.164 value is written back onto the request
 * so downstream steps store and return it.
 *
 * <p>Deliberately does <em>not</em> use {@code @Valid}: bean validation would raise a
 * {@code MethodArgumentNotValidException} with a different response shape, whereas this rule must
 * answer with an {@code errors} array naming each offending field.
 */
public class ValidateNewOwner {

    /**
     * Syntactic email check (the HTML5/Hibernate-Validator {@code @Email} form): a local part of the
     * permitted characters, an {@code @}, then a dot-separated domain of label segments. Used only to
     * decide between accept and 400; a valid value is then lower-cased and written back onto the request.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?"
                    + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    /**
     * Disposable-email providers whose domains are rejected: a syntactically valid address on one of
     * these is a 400, so throwaway inboxes cannot be used to register an owner. Lower-cased to compare
     * against the canonicalized (lower-cased) email domain.
     */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException,
            DisposableEmailException {
        // This list mirrors OwnerFieldsDto's required fields by hand: it must track the schema's
        // `required` set. We check presence/non-blankness only, a narrower rule than the DTO's full
        // constraints (@Size/@Pattern), so bean validation is intentionally not used here.
        // Normalize whichever address fields are supplied first, so the required-field check rejects an
        // address that is blank after normalization (e.g. "   "), and so downstream steps store/return
        // and compare the canonical form. The structured form (addressLine1 [+ addressLine2]) is
        // preferred when present; the flat 'address' input stays accepted for backward compatibility.
        // OwnerAddress.normalize yields "" for a null or all-whitespace value.
        request.setAddressLine1(OwnerAddress.normalizeToNull(request.getAddressLine1()));
        request.setAddressLine2(OwnerAddress.normalizeToNull(request.getAddressLine2()));
        // The stored/returned 'address' is the effective address: the composed structured lines when
        // addressLine1 is present, otherwise the normalized flat address. Written back so BuildOwner
        // persists it and the response (and every later reader of the address) sees the canonical form.
        String effectiveAddress = OwnerAddress.effective(
                request.getAddressLine1(), request.getAddressLine2(), request.getAddress());
        request.setAddress(effectiveAddress);

        List<String> missing = new ArrayList<>();
        require(missing, "firstName", request.getFirstName());
        require(missing, "lastName", request.getLastName());
        // Valid when an address is supplied in EITHER form: a non-blank addressLine1 or the flat
        // address. effectiveAddress is "" only when neither was supplied.
        require(missing, "address", effectiveAddress);
        require(missing, "city", request.getCity());
        require(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }

        // Normalize the telephone to E.164. Store the canonical value back so BuildOwner/SaveOwner
        // persist it and the response echoes the E.164 form.
        String e164 = OwnerTelephone.toE164(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);

        // Email is optional: only an email that is present (non-blank) must be a valid address. A valid
        // value is lower-cased and written back (like telephone above) so downstream steps store and
        // return the canonical form.
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new InvalidEmailException(email);
            }
            String canonical = email.toLowerCase();
            // Reject throwaway inboxes: a syntactically valid address on a disposable-email domain is a
            // 400, so the owner is never persisted. Domain is everything after the last '@'.
            String domain = canonical.substring(canonical.lastIndexOf('@') + 1);
            if (DISPOSABLE_DOMAINS.contains(domain)) {
                throw new DisposableEmailException(email);
            }
            request.setEmail(canonical);
        }

        validated.set(request);
    }

    private static void require(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
