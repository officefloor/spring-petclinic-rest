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

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Assigns an owner's {@link Owner#getMemberId() member id} on create.
 * <p>
 * The id is formatted {@code <REGION><FY><HASH8><CHK>} (see {@link MemberId}) from the
 * owner's region, the fiscal year of its registration date and the hash of its normalized
 * telephone and last name. When the computed id collides with an already-stored owner's
 * member id, it is de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2
 * or more that makes it unique.
 */
@Component
public class MemberIdAssigner {

    private final ClinicService clinicService;

    public MemberIdAssigner(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Assigns {@code owner}'s member id from its region, registration date and the hash of
     * its normalized telephone and last name, de-duplicated against already-stored owners.
     * Call this after the telephone has been normalized and the registration date assigned,
     * and before the owner is saved.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        String base = MemberId.of(owner.getRegion(), owner.getRegistrationDate(),
            owner.getTelephone(), owner.getLastName());
        owner.setMemberId(deduplicate(base));
    }

    private String deduplicate(String base) {
        Set<String> existing = clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .collect(Collectors.toSet());
        if (!existing.contains(base)) {
            return base;
        }
        int n = 2;
        while (existing.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }
}
