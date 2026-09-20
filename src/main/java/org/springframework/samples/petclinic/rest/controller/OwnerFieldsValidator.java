/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Enforces that every mandatory owner field is present and non-blank.
 * <p>
 * A field is rejected when it is missing (null), empty or whitespace-only. Registered on the
 * owner request binder so it runs during the standard {@code @Valid} pass, letting its rejections
 * surface through the same {@code BindingResult} as the Bean Validation constraints.
 */
@Component
public class OwnerFieldsValidator implements Validator {

    private final AddressNormalizer addressNormalizer;

    public OwnerFieldsValidator(AddressNormalizer addressNormalizer) {
        this.addressNormalizer = addressNormalizer;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        rejectIfBlank(errors, "firstName", owner.getFirstName());
        rejectIfBlank(errors, "lastName", owner.getLastName());
        rejectIfBlank(errors, "address", addressNormalizer.normalize(owner.getAddress()));
        rejectIfBlank(errors, "city", owner.getCity());
        rejectIfBlank(errors, "telephone", owner.getTelephone());
    }

    private void rejectIfBlank(Errors errors, String field, String value) {
        if (!StringUtils.hasText(value)) {
            errors.rejectValue(field, "required", "must not be blank");
        }
    }
}
