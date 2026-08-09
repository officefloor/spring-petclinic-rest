package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.reflect.Method;

import net.officefloor.plugin.variable.Val;
import org.springframework.core.MethodParameter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Normalizes the telephone number when creating an owner into canonical E.164 form and writes it
 * back onto the request, so that {@link BuildOwner} persists — and the response returns — the E.164
 * string.
 *
 * <p>Rules: a leading {@code '+'} and country code are kept when present; otherwise country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national digits.
 * Spaces, dashes and brackets are stripped. The digits following the {@code '+'} must number between
 * 8 and 15. So {@code '0412 345 678'} becomes {@code '+61412345678'}.
 *
 * <p>A value that cannot form valid E.164 is rejected as a 400, mirroring the schema-validation
 * failures produced by {@link ValidateOwner}.
 */
public class NormalizeOwnerTelephone {

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final Method SERVICE_METHOD;

    static {
        try {
            SERVICE_METHOD = NormalizeOwnerTelephone.class.getMethod("service", OwnerFieldsDto.class);
        }
        catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void service(@Val OwnerFieldsDto request) throws MethodArgumentNotValidException {
        String telephone = request.getTelephone();
        String e164 = toE164(telephone);
        if (e164 == null) {
            reject(request);
        }
        request.setTelephone(e164);
    }

    /**
     * Convert an input telephone to E.164, or return {@code null} when it cannot form a valid one.
     */
    private static String toE164(String telephone) {
        String raw = telephone == null ? "" : telephone.trim();
        boolean hasCountryCode = raw.startsWith("+");
        String body = hasCountryCode ? raw.substring(1) : raw;

        // Strip spaces, dashes and brackets; anything else remaining is invalid.
        String cleaned = body.replaceAll("[\\s\\-()\\[\\]]", "");
        if (!cleaned.matches("[0-9]+")) {
            return null;
        }

        String digits;
        if (hasCountryCode) {
            digits = cleaned;
        }
        else {
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }

        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            return null;
        }
        return "+" + digits;
    }

    private static void reject(OwnerFieldsDto request) throws MethodArgumentNotValidException {
        BindingResult binding = new BeanPropertyBindingResult(request, "ownerFieldsDto");
        binding.rejectValue("telephone", "Telephone",
                "must form a valid E.164 telephone number (8 to 15 digits after the '+')");
        throw new MethodArgumentNotValidException(new MethodParameter(SERVICE_METHOD, 0), binding);
    }
}
