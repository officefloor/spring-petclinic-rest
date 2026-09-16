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

import java.util.Comparator;
import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Detects "possible duplicate" owners: a new owner that is not a hard duplicate (see
 * {@link HouseholdDuplicateValidator}) but still sounds like an existing owner living at the same
 * postcode — their last names share a {@link Soundex#encode(String) Soundex code} and their
 * postcodes match — while resolving to a different {@link Owner#getIdentityKey() identity key}. The
 * comparison is done in memory because the stored last names are not kept in a normalized form, for
 * the same reason as the name matching in {@link NamesakeCounter}.
 */
@Component
public class PossibleDuplicateOwnerDetector {

    private final ClinicService clinicService;

    public PossibleDuplicateOwnerDetector(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Finds the existing owner the given (not-yet-saved) owner is a possible duplicate of: the
     * earliest owner whose last name shares its {@link Soundex#encode(String) Soundex code} and
     * whose postcode matches, yet whose {@link Owner#getIdentityKey() identity key} differs (so the
     * two are not the same person). The owner's telephone and email are expected to already be in
     * their normalized form.
     *
     * @param owner the owner being created
     * @return the id of the earliest matching owner, or empty when the owner is not a possible
     *         duplicate of any existing owner
     */
    public Optional<Integer> findPossibleDuplicateOf(Owner owner) {
        if (owner.getPostcode() == null) {
            return Optional.empty();
        }
        String soundex = Soundex.encode(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> soundex.equals(Soundex.encode(existing.getLastName())))
            .filter(existing -> owner.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> !identityKey.equals(existing.getIdentityKey()))
            .min(Comparator.comparing(Owner::getId))
            .map(Owner::getId);
    }
}
