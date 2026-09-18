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
 * Records, on create, how many already-stored owners share the new owner's name.
 * <p>
 * Two owners are namesakes when their first name and last name are equal ignoring case.
 * The count reflects the owners stored before this create, so it must be assigned before
 * the owner is saved.
 */
@Component
public class NamesakeCounter {

    private final ClinicService clinicService;

    public NamesakeCounter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Assigns {@code owner}'s namesake count: the number of already-stored owners sharing
     * its first name and last name, compared case-insensitively. Call this before the
     * owner is saved so it does not count itself.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        long count = clinicService.findAllOwners().stream()
            .filter(existing -> isNamesake(existing, owner))
            .count();
        owner.setNamesakeCount(Math.toIntExact(count));
    }

    private boolean isNamesake(Owner existing, Owner candidate) {
        return equalsIgnoreCase(existing.getFirstName(), candidate.getFirstName())
            && equalsIgnoreCase(existing.getLastName(), candidate.getLastName());
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
