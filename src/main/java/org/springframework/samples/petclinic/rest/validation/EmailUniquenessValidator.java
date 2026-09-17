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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;

import org.springframework.samples.petclinic.rest.error.DuplicateEmailException;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Rejects creating an owner whose email address, compared by its lower-cased form, is already used
 * by another owner. An owner without an email address is left unchecked, since email is optional.
 */
@Component
public class EmailUniquenessValidator {

    private final ClinicService clinicService;

    public EmailUniquenessValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param email the owner's email address (already normalized), or {@code null}/blank when none
     * was supplied
     * @throws DuplicateEmailException if another owner already uses the same email, compared
     * case-insensitively
     */
    public void validate(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        String key = email.toLowerCase(Locale.ROOT);
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .map(existing -> existing.getEmail())
            .filter(StringUtils::hasText)
            .anyMatch(existing -> existing.toLowerCase(Locale.ROOT).equals(key));
        if (duplicate) {
            throw new DuplicateEmailException(email);
        }
    }
}
