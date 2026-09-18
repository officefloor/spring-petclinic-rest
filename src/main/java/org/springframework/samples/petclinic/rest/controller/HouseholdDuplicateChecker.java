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
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Detects, on create, whether an owner would duplicate an existing household.
 * <p>
 * Two owners share a household when they carry the same last name and the same address.
 * Both fields are compared leniently: surrounding whitespace is trimmed, internal runs of
 * whitespace are collapsed to a single space and letters are compared without regard to
 * case, so values that differ only in spacing or capitalisation are treated as equal.
 */
@Component
public class HouseholdDuplicateChecker {

    /** Runs of whitespace collapsed to a single space before comparison. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final ClinicService clinicService;

    public HouseholdDuplicateChecker(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Reports whether an already-stored owner shares {@code candidate}'s household, i.e.
     * has the same last name and address under the lenient comparison described above.
     *
     * @param candidate the owner being created
     * @return {@code true} when another owner already occupies the same household
     */
    public boolean isDuplicate(Owner candidate) {
        String lastName = normalize(candidate.getLastName());
        String address = normalize(candidate.getAddress());
        return clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalize(existing.getLastName()).equals(lastName)
                && normalize(existing.getAddress()).equals(address));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return WHITESPACE.matcher(value.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }
}
