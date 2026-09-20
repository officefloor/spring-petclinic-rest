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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Guards owner creation against duplicate emails: an email is unique across owners once
 * compared in its canonical (trimmed, lower-cased) form. Owners without an email are never
 * in conflict.
 */
@Component
public class EmailUniquenessValidator {

    private final ClinicService clinicService;

    private final EmailNormalizer emailNormalizer;

    public EmailUniquenessValidator(ClinicService clinicService, EmailNormalizer emailNormalizer) {
        this.clinicService = clinicService;
        this.emailNormalizer = emailNormalizer;
    }

    /**
     * Reject {@code candidate} when its canonical email already belongs to another owner.
     *
     * @param candidate the owner about to be created
     * @throws DuplicateEmailException if another owner already uses the candidate's email
     */
    public void validate(Owner candidate) {
        String email = emailNormalizer.normalize(candidate.getEmail());
        if (email == null) {
            return;
        }
        boolean taken = clinicService.findAllOwners().stream()
            .map(existing -> emailNormalizer.normalize(existing.getEmail()))
            .anyMatch(email::equals);
        if (taken) {
            throw new DuplicateEmailException(email);
        }
    }
}
