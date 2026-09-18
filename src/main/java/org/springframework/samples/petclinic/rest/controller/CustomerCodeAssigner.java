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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Assigns an owner's customer code on create.
 * <p>
 * The code is formatted {@code <LAST3>-<NNNN>} where {@code LAST3} is the upper-cased
 * first three letters of the owner's last name and {@code NNNN} is a global 4-digit
 * zero-padded sequence equal to one more than the current number of stored owners
 * (e.g. {@code "SMI-0007"}).
 */
@Component
public class CustomerCodeAssigner {

    private final ClinicService clinicService;

    public CustomerCodeAssigner(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Assigns {@code owner}'s customer code based on its last name and the current owner
     * count. Call this before the owner is saved so the sequence reflects the number of
     * owners already stored.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        String lastName = owner.getLastName();
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        long sequence = clinicService.countOwners() + 1;
        owner.setCustomerCode(String.format("%s-%04d", prefix, sequence));
    }
}
