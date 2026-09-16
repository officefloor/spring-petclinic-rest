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

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Enforces that no two owners share a household, i.e. have the same last name and the same address.
 * Both fields are compared case-insensitively and with runs of whitespace collapsed to a single
 * space (and surrounding whitespace trimmed), so values that differ only in letter case or spacing
 * are still treated as the same. The comparison is done in memory because the stored address is not
 * kept in a normalized form, so it cannot be matched reliably with a database query.
 */
@Component
public class OwnerHouseholdUniquenessValidator {

    private final ClinicService clinicService;

    public OwnerHouseholdUniquenessValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Rejects an owner whose last name and address already belong to an existing owner.
     *
     * @param lastName the last name of the owner being created
     * @param address  the address of the owner being created
     * @throws DuplicateOwnerHouseholdException if another owner already has this last name and
     *                                          address
     */
    public void validateUnique(String lastName, String address) {
        String normalizedLastName = normalize(lastName);
        String normalizedAddress = normalize(address);
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .anyMatch(existing -> normalize(existing.getLastName()).equals(normalizedLastName)
                && normalize(existing.getAddress()).equals(normalizedAddress));
        if (duplicate) {
            throw new DuplicateOwnerHouseholdException(lastName, address);
        }
    }

    /**
     * Reduces a value to its comparison form: trimmed, with internal whitespace runs collapsed to a
     * single space and lower-cased.
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
