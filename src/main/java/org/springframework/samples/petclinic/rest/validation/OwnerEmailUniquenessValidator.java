/*
 * Copyright 2016-2017 the original author or authors.
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

import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Enforces that an owner's normalized email is unique across all owners. Emails are compared in
 * their canonical storage form (see {@link EmailNormalizer}), so two owners whose submitted emails
 * differ only in letter case are still treated as duplicates. Email is optional, so an absent email
 * is never a duplicate.
 */
@Component
public class OwnerEmailUniquenessValidator {

    private final ClinicService clinicService;

    public OwnerEmailUniquenessValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Rejects a normalized email that is already used by any existing owner. A {@code null} email
     * (none supplied) is always accepted.
     *
     * @param normalizedEmail the normalized email of the owner being created, or {@code null}
     * @throws DuplicateOwnerEmailException if another owner already uses this email
     */
    public void validateUnique(String normalizedEmail) {
        if (normalizedEmail == null) {
            return;
        }
        if (!this.clinicService.findOwnerByEmail(normalizedEmail).isEmpty()) {
            throw new DuplicateOwnerEmailException(normalizedEmail);
        }
    }
}
