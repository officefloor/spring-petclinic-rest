package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.core.MethodParameter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Normalizes the optional owner email. Email is optional: a missing (null) value is left untouched.
 * When present the value must be a syntactically valid address; it is stored — and returned — lower-cased.
 *
 * <p>A value that is present but not a valid address is rejected as a 400, mirroring the schema-validation
 * failures produced by {@link ValidateOwner}. Validation here is independent of the generated DTO's bean
 * constraints so the rule holds regardless of how the request was bound.
 */
public class NormalizeOwnerEmail {

    /**
     * Pragmatic single-{@code @} address check: a non-empty local part, an {@code @}, then a domain with at
     * least one dot-separated label. Matches the semantics of {@code jakarta.validation.constraints.Email}
     * for the addresses this application accepts.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final Method SERVICE_METHOD;

    static {
        try {
            SERVICE_METHOD = NormalizeOwnerEmail.class.getMethod("service", OwnerFieldsDto.class);
        }
        catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void service(@Val OwnerFieldsDto request) throws MethodArgumentNotValidException {
        String email = request.getEmail();
        if (email == null) {
            return; // email is optional
        }
        if (!EMAIL.matcher(email).matches()) {
            BindingResult binding = new BeanPropertyBindingResult(request, "ownerFieldsDto");
            binding.rejectValue("email", "Email", "must be a valid email address");
            throw new MethodArgumentNotValidException(new MethodParameter(SERVICE_METHOD, 0), binding);
        }
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
