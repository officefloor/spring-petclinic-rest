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

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates the optional {@code registrationDate} supplied on an {@link OwnerFieldsDto} as
 * part of the {@code @Valid} pass on the owner request body: a supplied date must not be
 * later than the server's current date, so an owner can never be registered in the future.
 * <p>
 * Registered on the owner controller through {@code @InitBinder} so a future date is recorded
 * as a field error on the shared {@link Errors}, surfacing through the usual bad-request
 * handling. When no date is supplied the rule does not apply — the effective date is later
 * defaulted to the server's current date by {@link RegistrationDateAssigner}.
 */
@Component
public class RegistrationDateValidator implements Validator {

    private final Clock clock;

    public RegistrationDateValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LocalDate registrationDate = ((OwnerFieldsDto) target).getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now(clock))) {
            errors.rejectValue("registrationDate", "registrationDate.future",
                "must not be later than the current date");
        }
    }
}
