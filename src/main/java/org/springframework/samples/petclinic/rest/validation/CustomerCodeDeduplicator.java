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
 * Ensures a newly generated {@link CustomerCodeGenerator customer code} is unique across existing
 * owners. When the candidate collides with an existing owner's customer code, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that yields an unused code.
 */
@Component
public class CustomerCodeDeduplicator {

    private static final char SEPARATOR = '-';

    private final ClinicService clinicService;

    public CustomerCodeDeduplicator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Returns the candidate customer code, or a de-duplicated variant if it collides with an
     * existing owner's customer code.
     *
     * @param candidate the freshly generated customer code
     * @return a customer code that no existing owner already holds
     */
    public String deduplicate(String candidate) {
        Set<String> existing = this.clinicService.findAllOwners().stream()
            .map(Owner::getCustomerCode)
            .filter(code -> code != null)
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
