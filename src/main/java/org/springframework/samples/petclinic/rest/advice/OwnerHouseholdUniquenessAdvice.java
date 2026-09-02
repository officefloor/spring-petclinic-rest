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
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Rejects creating an owner who shares another owner's household, i.e. the same last name and the
 * same address compared case-insensitively with runs of whitespace collapsed. The check is skipped
 * when the request explicitly opts in with {@code sharesHousehold} set to true.
 */
@Aspect
@Component
public class OwnerHouseholdUniquenessAdvice {

    private final ClinicService clinicService;

    public OwnerHouseholdUniquenessAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Before("execution(* org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1.addOwner(..)) && args(fields)")
    public void rejectDuplicateHousehold(OwnerFieldsDto fields) {
        if (Boolean.TRUE.equals(fields.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(fields.getLastName());
        String address = normalize(fields.getAddress());
        if (lastName == null || address == null) {
            return;
        }
        boolean taken = clinicService.findAllOwners().stream().anyMatch(existing ->
            lastName.equals(normalize(existing.getLastName())) && address.equals(normalize(existing.getAddress())));
        if (taken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another owner already shares this household");
        }
    }

    private static String normalize(String value) {
        return value == null ? null : value.strip().replaceAll("\\s+", " ").toLowerCase();
    }
}
