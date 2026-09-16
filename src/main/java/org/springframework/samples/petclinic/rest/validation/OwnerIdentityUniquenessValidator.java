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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Enforces that no two owners share an {@link Owner#getIdentityKey() identity key} — the single
 * derived value ({@code normalizedTelephone + '|' + email + '|' + householdId}) that all duplicate
 * detection is based on. Only an exact whole-key match is a duplicate, so owners that differ in any
 * one component (for example two members of the same household with different telephones) are
 * allowed.
 */
@Component
public class OwnerIdentityUniquenessValidator {

    private final ClinicService clinicService;

    public OwnerIdentityUniquenessValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Rejects an owner whose identity key equals that of an existing owner. The owner's telephone,
     * email and household id must already be in their final form when this runs.
     *
     * @param owner the owner being created
     * @throws DuplicateOwnerIdentityException if another owner already has this identity key
     */
    public void validateUnique(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> existing.getIdentityKey().equals(identityKey));
        if (duplicate) {
            throw new DuplicateOwnerIdentityException(identityKey);
        }
    }
}
