package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.reflect.Method;

import net.officefloor.plugin.variable.Val;
import org.springframework.core.MethodParameter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Normalizes the telephone number when creating an owner. Every non-digit character is removed and
 * the remaining value must be exactly ten digits; the normalized value is written back onto the
 * request so that {@link BuildOwner} persists — and the response returns — the 10-digit form.
 *
 * <p>A value that is not exactly ten digits after stripping is rejected as a 400, mirroring the
 * schema-validation failures produced by {@link ValidateOwner}.
 */
public class NormalizeOwnerTelephone {

    private static final int REQUIRED_DIGITS = 10;

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
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != REQUIRED_DIGITS) {
            BindingResult binding = new BeanPropertyBindingResult(request, "ownerFieldsDto");
            binding.rejectValue("telephone", "Telephone",
                    "must contain exactly 10 digits after removing non-digit characters");
            throw new MethodArgumentNotValidException(new MethodParameter(SERVICE_METHOD, 0), binding);
        }
        request.setTelephone(digits);
    }
}
