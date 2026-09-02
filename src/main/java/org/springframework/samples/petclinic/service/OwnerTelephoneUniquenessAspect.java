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
package org.springframework.samples.petclinic.service;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Enforces that a newly created owner does not reuse another owner's telephone. The telephone
 * has already been normalized (non-digits stripped to ten digits) by the field validation advice,
 * so a plain equality comparison against the stored owners is sufficient. Updates to an existing
 * owner keep their own number and are left untouched.
 */
@Aspect
@Component
public class OwnerTelephoneUniquenessAspect {

    private final OwnerRepository ownerRepository;

    public OwnerTelephoneUniquenessAspect(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Before("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public void rejectDuplicateTelephone(Owner owner) {
        String telephone = owner.getTelephone();
        if (!owner.isNew() || telephone == null) {
            return;
        }
        boolean taken = ownerRepository.findAll().stream()
            .anyMatch(existing -> telephone.equals(existing.getTelephone()));
        if (taken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Telephone already in use by another owner");
        }
    }
}
