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
 * Detects, on create, whether a new owner duplicates an already-stored one. All duplicate
 * detection is consolidated into the owner's {@link Owner#getIdentityKey() identity key}
 * (telephone, email and household id combined): a candidate is a duplicate only when an
 * existing owner carries the exact same identity key. Because the telephone is part of the
 * key, owners that differ in any one of those parts — including two members of the same
 * household with different telephones — have distinct keys and are not duplicates.
 */
@Component
public class IdentityDuplicateChecker {

    private final ClinicService clinicService;

    public IdentityDuplicateChecker(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Reports whether an already-stored owner shares {@code candidate}'s identity key.
     *
     * @param candidate the owner being created, not yet stored
     * @return {@code true} when another owner already carries the same identity key
     */
    public boolean isDuplicate(Owner candidate) {
        String identityKey = candidate.getIdentityKey();
        return clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> existing.getIdentityKey().equals(identityKey));
    }
}
