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

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.error.DuplicateHouseholdException;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Rejects creating an owner that would share a household with an existing one - that is, another
 * owner with the same last name and the same address, compared case-insensitively and with
 * collapsed whitespace. The request may opt in to the shared household via
 * {@link OwnerFieldsDto#getSharesHousehold()}, in which case the duplicate is allowed.
 */
@Component
public class HouseholdDuplicateValidator {

    private final ClinicService clinicService;

    private final HouseholdKey householdKey;

    public HouseholdDuplicateValidator(ClinicService clinicService, HouseholdKey householdKey) {
        this.clinicService = clinicService;
        this.householdKey = householdKey;
    }

    /**
     * @param owner the submitted owner fields
     * @throws DuplicateHouseholdException if another owner already shares the same last name and
     * address and the request did not set {@code sharesHousehold} to {@code true}
     */
    public void validate(OwnerFieldsDto owner) {
        if (Boolean.TRUE.equals(owner.getSharesHousehold())) {
            return;
        }
        String key = this.householdKey.of(owner.getLastName(), owner.getAddress());
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> this.householdKey.of(existing.getLastName(), existing.getAddress())
                .equals(key));
        if (duplicate) {
            throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
        }
    }
}
