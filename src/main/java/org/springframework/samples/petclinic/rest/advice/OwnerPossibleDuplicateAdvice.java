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
 * Populates the read-only {@code possibleDuplicate} / {@code possibleDuplicateOf} on an owner
 * response: an owner is a possible (soft) duplicate when an earlier owner shares its last name and
 * postcode but has a different telephone. Such owners are still created; this flag is derived on the
 * way out, so it never affects storage or the request. {@code possibleDuplicateOf} carries the id of
 * the earliest matching owner, or is left unset when there is no match.
 */
@Aspect
@Component
public class OwnerPossibleDuplicateAdvice {

    private final ClinicService clinicService;

    public OwnerPossibleDuplicateAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @AfterReturning(pointcut = "execution(* org.springframework.samples.petclinic.rest.controller.v1."
        + "OwnerRestControllerV1.addOwner(..)) || execution(* org.springframework.samples.petclinic.rest."
        + "controller.v1.OwnerRestControllerV1.getOwner(..))", returning = "response")
    public void flagPossibleDuplicate(ResponseEntity<?> response) {
        Object body = response == null ? null : response.getBody();
        if (!(body instanceof OwnerDto owner) || owner.getId() == null) {
            return;
        }
        Integer match = clinicService.findAllOwners().stream()
            .filter(other -> other.getId() != null && other.getId() < owner.getId()
                && sameText(other.getLastName(), owner.getLastName())
                && sameText(other.getPostcode(), owner.getPostcode())
                && !sameText(other.getTelephone(), owner.getTelephone()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
        owner.setPossibleDuplicate(match != null);
        owner.setPossibleDuplicateOf(match);
    }

    private static boolean sameText(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
