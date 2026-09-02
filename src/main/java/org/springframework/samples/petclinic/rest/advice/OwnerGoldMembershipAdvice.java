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
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Promotes an owner response to the {@code GOLD} membership tier when its household (owners sharing
 * the same last name and address, compared case- and whitespace-insensitively) has three or more
 * members after this create. Ordered ahead of the unordered namesake aspect so its after-returning
 * advice fires last, overriding the BRONZE/SILVER tier already set. Derived on the way out, so it
 * never affects storage.
 */
@Aspect
@Component
@Order(0)
public class OwnerGoldMembershipAdvice {

    private final ClinicService clinicService;

    public OwnerGoldMembershipAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @AfterReturning(pointcut = "execution(* org.springframework.samples.petclinic.rest.controller.v1."
        + "OwnerRestControllerV1.addOwner(..)) || execution(* org.springframework.samples.petclinic.rest."
        + "controller.v1.OwnerRestControllerV1.getOwner(..))", returning = "response")
    public void promoteGoldHousehold(ResponseEntity<?> response) {
        Object body = response == null ? null : response.getBody();
        if (!(body instanceof OwnerDto owner) || owner.getLastName() == null || owner.getAddress() == null) {
            return;
        }
        String household = key(owner.getLastName(), owner.getAddress());
        long members = clinicService.findAllOwners().stream()
            .filter(other -> household.equals(key(other.getLastName(), other.getAddress())))
            .count();
        if (members >= 3) {
            owner.setMembershipTier("GOLD");
        }
    }

    private static String key(String lastName, String address) {
        return normalize(lastName) + '\n' + normalize(address);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase();
    }
}
