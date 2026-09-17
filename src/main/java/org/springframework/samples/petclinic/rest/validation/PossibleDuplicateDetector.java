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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Comparator;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Detects a soft match between a newly created owner and an existing one: an owner that is not a
 * hard {@link DuplicateOwnerValidator duplicate} but whose last name (compared phonetically by
 * Soundex) and postcode match an existing owner while their {@link Owner#getIdentityKey() identity
 * keys} differ. Unlike the duplicate validator this never rejects the create; it merely reports the
 * existing owner so the caller can flag the new one as a possible duplicate.
 */
@Component
public class PossibleDuplicateDetector {

    private final ClinicService clinicService;

    public PossibleDuplicateDetector(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param owner the owner being created, with its telephone and email already normalized so its
     * {@link Owner#getIdentityKey() identityKey} is final
     * @return the id of the lowest-id existing owner whose last name (by Soundex) and postcode match
     * this owner's but whose identity key differs, or {@code null} when there is no such match
     */
    public Integer findPossibleDuplicate(Owner owner) {
        if (!StringUtils.hasText(owner.getPostcode())) {
            return null;
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .filter(existing -> soundex.equals(Soundex.of(existing.getLastName())))
            .filter(existing -> owner.getPostcode().equals(existing.getPostcode()))
            .filter(existing -> !identityKey.equals(existing.getIdentityKey()))
            .map(Owner::getId)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElse(null);
    }
}
