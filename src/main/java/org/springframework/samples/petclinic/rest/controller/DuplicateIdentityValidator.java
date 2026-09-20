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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Guards owner creation against re-registration of the same identity. Two owners are the same
 * identity when their {@link OwnerIdentity identity key} matches (same normalized telephone, email
 * and last-name sound); a candidate that collides with an existing, non-deleted owner is rejected.
 * Owners differing in any of those fields — including household members with different telephones —
 * are distinct identities and are left to {@link PossibleDuplicateDetector} to flag softly.
 */
@Component
public class DuplicateIdentityValidator {

    private final ClinicService clinicService;

    private final OwnerIdentity ownerIdentity;

    public DuplicateIdentityValidator(ClinicService clinicService, OwnerIdentity ownerIdentity) {
        this.clinicService = clinicService;
        this.ownerIdentity = ownerIdentity;
    }

    /**
     * Reject {@code candidate} when an existing, non-deleted owner already carries the same identity
     * key.
     *
     * @param candidate the owner about to be created, with its fields already normalized
     * @throws DuplicateIdentityException if a non-deleted owner shares the candidate's identity key
     */
    public void validate(Owner candidate) {
        String identityKey = ownerIdentity.key(candidate);
        boolean duplicate = clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> identityKey.equals(ownerIdentity.key(existing)));
        if (duplicate) {
            throw new DuplicateIdentityException(identityKey);
        }
    }
}
