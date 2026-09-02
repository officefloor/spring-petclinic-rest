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

import java.time.LocalDate;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.BusinessDay;
import org.springframework.stereotype.Component;

/**
 * Populates the read-only {@code bulkSignupWarning} on an owner response: {@code true} once more
 * than 80 owners have already been created today (by their {@code registrationDate}), otherwise
 * {@code false}. Derived on the way out, so it never affects storage or the request.
 */
@Aspect
@Component
public class OwnerBulkSignupWarningAdvice {

    private static final int BULK_THRESHOLD = 80;

    private final ClinicService clinicService;

    public OwnerBulkSignupWarningAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @AfterReturning(pointcut = "execution(* org.springframework.samples.petclinic.rest.controller.v1."
        + "OwnerRestControllerV1.addOwner(..)) || execution(* org.springframework.samples.petclinic.rest."
        + "controller.v1.OwnerRestControllerV1.getOwner(..))", returning = "response")
    public void addBulkSignupWarning(ResponseEntity<?> response) {
        Object body = response == null ? null : response.getBody();
        if (!(body instanceof OwnerDto owner)) {
            return;
        }
        LocalDate today = BusinessDay.onOrNextBusinessDay(LocalDate.now());
        long createdToday = clinicService.findAllOwners().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        owner.setBulkSignupWarning(createdToday > BULK_THRESHOLD);
    }
}
