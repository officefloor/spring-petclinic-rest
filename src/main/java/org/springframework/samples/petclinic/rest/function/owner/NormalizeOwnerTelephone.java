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
 * <p>When the caller supplies an explicit country code, the national-number length is additionally
 * validated against that country: {@code '+61'} requires 9 national digits and {@code '+1'} requires
 * 10. A number whose national part is the wrong length for its country is rejected as a 400.
 *
 * <p>A value that cannot form valid E.164 is rejected as a 400, mirroring the schema-validation
 * failures produced by {@link ValidateOwner}.
 */
public class NormalizeOwnerTelephone {

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Known country codes mapped to the exact number of national digits they require, longest code
     * first so {@code '61'} is matched before the shorter {@code '1'}. When an explicit country code
     * matches one of these, the national-number length is enforced; other codes fall back to the
     * generic {@link #MIN_DIGITS}..{@link #MAX_DIGITS} bound.
     */
    private static final java.util.List<CountryRule> COUNTRY_RULES = java.util.List.of(
            new CountryRule("61", 9), new CountryRule("1", 10));

    private record CountryRule(String code, int nationalDigits) {
    }

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

        // When the caller gave an explicit country code, hold the national part to that country's
        // required length ('+61' => 9 digits, '+1' => 10). This is the only place we can tell the
        // country apart from the assumed default, so it is scoped to explicitly-coded numbers.
        if (hasCountryCode) {
            for (CountryRule rule : COUNTRY_RULES) {
                if (digits.startsWith(rule.code())) {
                    String national = digits.substring(rule.code().length());
                    if (national.length() != rule.nationalDigits()) {
                        return null;
                    }
                    break;
                }
            }
        }
        return "+" + digits;
    }

    private static void reject(OwnerFieldsDto request) throws MethodArgumentNotValidException {
        BindingResult binding = new BeanPropertyBindingResult(request, "ownerFieldsDto");
        binding.rejectValue("telephone", "Telephone",
                "must form a valid E.164 telephone number (8 to 15 digits after the '+', "
                        + "and the national-number length required by the country code: +61 => 9, +1 => 10)");
        throw new MethodArgumentNotValidException(new MethodParameter(SERVICE_METHOD, 0), binding);
    }
}
