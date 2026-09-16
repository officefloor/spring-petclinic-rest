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

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Ensures a newly generated {@link MemberIdGenerator member id} is unique across existing owners.
 * When the candidate collides with an existing owner's member id, {@code -<n>} is appended with the
 * smallest {@code n} of 2 or more that yields an unused id.
 */
@Component
public class MemberIdDeduplicator {

    private static final char SEPARATOR = '-';

    private final ClinicService clinicService;

    public MemberIdDeduplicator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Returns the candidate member id, or a de-duplicated variant if it collides with an existing
     * owner's member id.
     *
     * @param candidate the freshly generated member id
     * @return a member id that no existing owner already holds
     */
    public String deduplicate(String candidate) {
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getMemberId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        if (!existing.contains(candidate)) {
            return candidate;
        }
        int suffix = 2;
        while (existing.contains(candidate + SEPARATOR + suffix)) {
            suffix++;
        }
        return candidate + SEPARATOR + suffix;
    }
}
