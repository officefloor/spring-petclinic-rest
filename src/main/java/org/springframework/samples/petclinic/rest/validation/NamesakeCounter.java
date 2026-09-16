/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Counts how many existing owners share a given owner's name, i.e. have the same first name and
 * last name compared case-insensitively. The comparison is done in memory because the stored names
 * are not kept in a normalized form, for the same reason as the possible-duplicate matching in
 * {@link PossibleDuplicateOwnerDetector}.
 */
@Component
public class NamesakeCounter {

    private final ClinicService clinicService;

    public NamesakeCounter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Counts the existing owners whose first and last name match the given ones, case-insensitively.
     *
     * @param firstName the first name of the owner being created
     * @param lastName  the last name of the owner being created
     * @return the number of existing owners sharing that first and last name
     */
    public int count(String firstName, String lastName) {
        return (int) this.clinicService.findAllOwners().stream()
            .filter(existing -> equalsIgnoreCase(existing.getFirstName(), firstName)
                && equalsIgnoreCase(existing.getLastName(), lastName))
            .count();
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
