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

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Guards owner creation against duplicate households: two owners are considered to share a
 * household when their last name and address match once compared case-insensitively and with
 * runs of whitespace collapsed to a single space.
 */
@Component
public class HouseholdDuplicateValidator {

    private final ClinicService clinicService;

    public HouseholdDuplicateValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Reject {@code candidate} when an existing owner already occupies the same household.
     *
     * @param candidate the owner about to be created
     * @throws DuplicateHouseholdException if another owner shares the candidate's last name and
     * address
     */
    public void validate(Owner candidate) {
        String lastNameKey = normalize(candidate.getLastName());
        String addressKey = normalize(candidate.getAddress());
        boolean duplicate = clinicService.findOwnerByLastNameIgnoreCase(candidate.getLastName()).stream()
            .anyMatch(existing -> lastNameKey.equals(normalize(existing.getLastName()))
                && addressKey.equals(normalize(existing.getAddress())));
        if (duplicate) {
            throw new DuplicateHouseholdException(candidate.getLastName(), candidate.getAddress());
        }
    }

    /**
     * Canonicalize a value for household comparison: {@code null} becomes empty, surrounding
     * whitespace is trimmed, internal whitespace runs collapse to a single space, and the result
     * is lower-cased.
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
