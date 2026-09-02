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

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Populates the read-only {@code namesakeCount} on an owner response: the number of owners
 * registered before this one (lower id) that share the same first and last name, compared
 * case-insensitively. It is derived on the way out, so it never affects storage or the request.
 */
@Aspect
@Component
public class OwnerNamesakeCountAdvice {

    private final ClinicService clinicService;

    public OwnerNamesakeCountAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @AfterReturning(pointcut = "execution(* org.springframework.samples.petclinic.rest.controller.v1."
        + "OwnerRestControllerV1.addOwner(..)) || execution(* org.springframework.samples.petclinic.rest."
        + "controller.v1.OwnerRestControllerV1.getOwner(..))", returning = "response")
    public void addNamesakeCount(ResponseEntity<?> response) {
        Object body = response == null ? null : response.getBody();
        if (!(body instanceof OwnerDto owner) || owner.getId() == null) {
            return;
        }
        int namesakeCount = (int) clinicService.findAllOwners().stream()
            .filter(other -> other.getId() != null && other.getId() < owner.getId()
                && matches(owner.getFirstName(), other.getFirstName())
                && matches(owner.getLastName(), other.getLastName()))
            .count();
        owner.setNamesakeCount(namesakeCount);
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        owner.setMembershipTier(namesakeCount == 0 && hasEmail ? "SILVER" : "BRONZE");
    }

    private static boolean matches(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
