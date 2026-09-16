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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Rejects an owner that duplicates one already on file. Two owners collide only when their
 * {@link Owner#getIdentityKey() identity keys} match — the same normalized telephone, email and
 * surname sound — so members of the same household who differ in telephone keep distinct keys and a
 * genuinely new household member is allowed (and merely flagged as a
 * {@link PossibleDuplicateOwnerDetector possible duplicate}). Only an owner whose identity key
 * matches an existing one — the same person submitted again — is a true duplicate and rejected. The
 * caller bypasses this block when the new owner explicitly opts in via {@code sharesHousehold}.
 */
@Component
public class HouseholdDuplicateValidator {

    private final ClinicService clinicService;

    public HouseholdDuplicateValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Rejects an owner whose {@link Owner#getIdentityKey() identity key} already belongs to an
     * existing owner. The owner's telephone, email and last name must already be assigned when this
     * runs, since the identity key is derived from them.
     *
     * @param owner the owner being created
     * @throws DuplicateHouseholdException if another owner shares this owner's identity key
     */
    public void rejectIfDuplicate(Owner owner) {
        String identityKey = owner.getIdentityKey();
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(existing.getIdentityKey()));
        if (duplicate) {
            throw new DuplicateHouseholdException(owner.getHouseholdId());
        }
    }
}
