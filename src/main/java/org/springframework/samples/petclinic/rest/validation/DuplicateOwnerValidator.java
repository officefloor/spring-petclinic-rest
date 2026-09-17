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
 * Rejects creating an owner that duplicates an existing household. The household is keyed on (last
 * name, postcode) through the {@link HouseholdKey}, so a new owner is a duplicate whenever an
 * existing owner already belongs to the same household. The caller bypasses this check when the
 * create deliberately declares a shared household.
 */
@Component
public class DuplicateOwnerValidator {

    private final ClinicService clinicService;

    private final HouseholdKey householdKey;

    public DuplicateOwnerValidator(ClinicService clinicService, HouseholdKey householdKey) {
        this.clinicService = clinicService;
        this.householdKey = householdKey;
    }

    /**
     * @param owner the owner being created, with its telephone, email and household id already
     * resolved so its {@link Owner#getIdentityKey() identityKey} is final
     * @throws DuplicateOwnerException if another owner already belongs to the same household
     */
    public void validate(Owner owner) {
        String key = this.householdKey.of(owner.getLastName(), owner.getPostcode());
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> key.equals(
                this.householdKey.of(existing.getLastName(), existing.getPostcode())));
        if (duplicate) {
            throw new DuplicateOwnerException(owner.getIdentityKey());
        }
    }
}
