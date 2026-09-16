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
 * Counts how many existing owners live in a given city, compared case-insensitively. The
 * comparison is done in memory because the stored cities are not kept in a normalized form,
 * mirroring {@link NamesakeCounter}.
 */
@Component
public class CityOwnerCounter {

    private final ClinicService clinicService;

    public CityOwnerCounter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Counts the existing owners whose city matches the given one, case-insensitively.
     *
     * @param city the city of the owner being created
     * @return the number of existing owners in that city
     */
    public long count(String city) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> existing.getCity() != null && existing.getCity().equalsIgnoreCase(city))
            .count();
    }
}
