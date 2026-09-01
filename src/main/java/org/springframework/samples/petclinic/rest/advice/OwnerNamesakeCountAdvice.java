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
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Records, on each newly-created owner just before it is saved, how many existing owners already
 * share its first and last name (compared case-insensitively). Kept as its own small aspect so this
 * rule stays a self-contained unit rather than growing the controller or service.
 */
@Aspect
@Component
public class OwnerNamesakeCountAdvice {

    private final OwnerRepository ownerRepository;

    public OwnerNamesakeCountAdvice(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Before("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public void assignNamesakeCount(Owner owner) {
        if (owner.isNew() && owner.getNamesakeCount() == null) {
            owner.setNamesakeCount((int) ownerRepository.findAll().stream()
                .filter(other -> sameName(owner.getFirstName(), other.getFirstName())
                    && sameName(owner.getLastName(), other.getLastName()))
                .count());
        }
    }

    private static boolean sameName(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
