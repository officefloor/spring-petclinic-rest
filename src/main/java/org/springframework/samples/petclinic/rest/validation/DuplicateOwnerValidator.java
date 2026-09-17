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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.error.DuplicateOwnerException;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Rejects creating an owner that hard-duplicates an existing one. A new owner is a duplicate only
 * when its whole {@link Owner#getIdentityKey() identity key} (normalized telephone, email and
 * household id) equals that of an existing owner, so housemates that share a household but hold
 * different telephones are allowed (and merely flagged as {@link PossibleDuplicateDetector possible
 * duplicates}). The caller bypasses this check when the create deliberately declares a shared
 * household.
 */
@Component
public class DuplicateOwnerValidator {

    private final ClinicService clinicService;

    public DuplicateOwnerValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param owner the owner being created, with its telephone, email and household id already
     * resolved so its {@link Owner#getIdentityKey() identityKey} is final
     * @throws DuplicateOwnerException if another owner already holds the same identity key
     */
    public void validate(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (duplicate) {
            throw new DuplicateOwnerException(identityKey);
        }
    }
}
