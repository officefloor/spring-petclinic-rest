package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.reflect.Method;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.core.MethodParameter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 *
 * <p>Bean validation ({@code @Valid}) rejects missing or malformed fields. It does not, however, reject a
 * value that is present but blank (whitespace only) when the field has no pattern constraint — {@code address}
 * and {@code city}. This step additionally rejects any of the required owner fields that are missing or blank,
 * so a create/update never persists an owner with an empty required field.
 */
@Validated
public class ValidateOwner {

    private static final Method SERVICE_METHOD;

    static {
        try {
            SERVICE_METHOD = ValidateOwner.class.getMethod("service", OwnerFieldsDto.class, Out.class);
        }
        catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MethodArgumentNotValidException {
        BindingResult binding = new BeanPropertyBindingResult(request, "ownerFieldsDto");
        rejectIfBlank(binding, "firstName", request.getFirstName());
        rejectIfBlank(binding, "lastName", request.getLastName());
        // Address is required after normalization: a value that collapses to empty (e.g. whitespace
        // only) is rejected, matching how it will be stored and compared.
        rejectIfBlank(binding, "address", AddressNormalizer.normalize(request.getAddress()));
        rejectIfBlank(binding, "city", request.getCity());
        rejectIfBlank(binding, "telephone", request.getTelephone());
        if (binding.hasErrors()) {
            throw new MethodArgumentNotValidException(new MethodParameter(SERVICE_METHOD, 0), binding);
        }
        validated.set(request);
    }

    private static void rejectIfBlank(BindingResult binding, String field, String value) {
        if (value == null || value.isBlank()) {
            binding.rejectValue(field, "NotBlank", "must not be blank");
        }
    }
}
