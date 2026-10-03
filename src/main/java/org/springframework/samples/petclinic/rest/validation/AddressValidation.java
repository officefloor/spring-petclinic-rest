package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AddressValidator.class)
@Documented
public @interface AddressValidation {

    String message() default "Address must not be blank after normalization";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
