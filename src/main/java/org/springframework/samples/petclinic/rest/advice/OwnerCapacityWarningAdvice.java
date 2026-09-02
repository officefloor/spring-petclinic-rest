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
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Populates the read-only {@code capacityWarning} on an owner response: {@code true} once the
 * owner's city already holds between 40 and 49 owners (approaching the hard capacity limit of 50),
 * otherwise {@code false}. The city is compared case-insensitively with runs of whitespace
 * collapsed. Derived on the way out, so it never affects storage or the request.
 */
@Aspect
@Component
public class OwnerCapacityWarningAdvice {

    private static final int WARNING_LOW = 40;

    private static final int WARNING_HIGH = 49;

    private final ClinicService clinicService;

    public OwnerCapacityWarningAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @AfterReturning(pointcut = "execution(* org.springframework.samples.petclinic.rest.controller.v1."
        + "OwnerRestControllerV1.addOwner(..)) || execution(* org.springframework.samples.petclinic.rest."
        + "controller.v1.OwnerRestControllerV1.getOwner(..))", returning = "response")
    public void addCapacityWarning(ResponseEntity<?> response) {
        Object body = response == null ? null : response.getBody();
        if (!(body instanceof OwnerDto owner)) {
            return;
        }
        String city = normalize(owner.getCity());
        long owners = city == null ? 0 : clinicService.findAllOwners().stream()
            .filter(existing -> city.equals(normalize(existing.getCity())))
            .count();
        owner.setCapacityWarning(owners >= WARNING_LOW && owners <= WARNING_HIGH);
    }

    private static String normalize(String value) {
        return value == null ? null : value.strip().replaceAll("\\s+", " ").toLowerCase();
    }
}
