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
package org.springframework.samples.petclinic.rest.advice;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Household;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Rejects a new owner whose {@link Household#id household id} — derived from its last name and
 * postcode — already belongs to an existing owner, since sharing a household is now the same as
 * being a duplicate. Setting {@code sharesHousehold} declares the owner a member of that household
 * and bypasses the block.
 */
@Aspect
@Component
public class OwnerHouseholdDuplicateAdvice {

    private final ClinicService clinicService;

    public OwnerHouseholdDuplicateAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Before("execution(* org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1.addOwner(..)) && args(fields)")
    public void rejectHouseholdDuplicate(OwnerFieldsDto fields) {
        String household = Household.id(fields.getLastName(), fields.getPostcode());
        if (household == null || Boolean.TRUE.equals(fields.getSharesHousehold())) {
            return;
        }
        boolean taken = clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> household.equals(Household.id(existing.getLastName(), existing.getPostcode())));
        if (taken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another owner is already in this household");
        }
    }
}
