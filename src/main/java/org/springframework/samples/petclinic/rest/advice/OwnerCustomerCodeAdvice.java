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
 * Assigns a customerCode '<CITY3>-<LAST3>-<NNNN>' to each newly-created owner just before it is
 * saved: CITY3 and LAST3 are the upper-cased first three letters of the city and last name, and
 * NNNN is a per-city 4-digit sequence equal to one more than the owners already in that city. Kept
 * as its own small aspect so this rule stays a self-contained unit rather than growing the controller
 * or service.
 */
@Aspect
@Component
public class OwnerCustomerCodeAdvice {

    private final OwnerRepository ownerRepository;

    public OwnerCustomerCodeAdvice(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Before("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public void assignCustomerCode(Owner owner) {
        if (owner.isNew() && owner.getCustomerCode() == null) {
            String city3 = (owner.getCity() + "XXX").substring(0, 3).toUpperCase();
            String last3 = (owner.getLastName() + "XXX").substring(0, 3).toUpperCase();
            long inCity = ownerRepository.findAll().stream()
                .filter(o -> owner.getCity().equalsIgnoreCase(o.getCity())).count();
            owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, inCity + 1));
        }
    }
}
