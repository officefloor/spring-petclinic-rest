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
 * The code is formatted {@code <CITY3>-<LAST3>-<NNNN>} where {@code CITY3} is the
 * upper-cased first three letters of the owner's city, {@code LAST3} the upper-cased
 * first three letters of the owner's last name, and {@code NNNN} is a per-city 4-digit
 * zero-padded sequence equal to one more than the number of owners already stored in
 * that city (e.g. {@code "SYD-SMI-0007"}).
 */
@Component
public class CustomerCodeAssigner {

    private final ClinicService clinicService;

    public CustomerCodeAssigner(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Assigns {@code owner}'s customer code based on its city, last name and the current
     * per-city owner count. Call this before the owner is saved so the sequence reflects
     * the number of owners already stored in that city.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        String cityPrefix = prefix(owner.getCity());
        String lastNamePrefix = prefix(owner.getLastName());
        long sequence = clinicService.countOwnersByCity(owner.getCity()) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", cityPrefix, lastNamePrefix, sequence));
    }

    /**
     * The upper-cased first three letters of {@code value} (fewer if it is shorter).
     */
    private String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
