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
 * Guards owner creation against duplicate identities: telephone, email and household are
 * consolidated into a single {@link OwnerIdentity#key(Owner) identity key}, and a candidate is
 * rejected only when its whole key equals an existing owner's. Owners that differ in any
 * segment (for example two household members with different telephones) are distinct and both
 * allowed.
 */
@Component
public class IdentityDuplicateValidator {

    private final ClinicService clinicService;

    private final OwnerIdentity ownerIdentity;

    public IdentityDuplicateValidator(ClinicService clinicService, OwnerIdentity ownerIdentity) {
        this.clinicService = clinicService;
        this.ownerIdentity = ownerIdentity;
    }

    /**
     * Reject {@code candidate} when its identity key already belongs to another owner.
     *
     * @param candidate the owner about to be created, with its telephone, email and household
     * identifier already normalized
     * @throws DuplicateIdentityException if another owner shares the candidate's whole identity key
     */
    public void validate(Owner candidate) {
        String key = ownerIdentity.key(candidate);
        boolean taken = clinicService.findAllOwners().stream()
            .map(ownerIdentity::key)
            .anyMatch(key::equals);
        if (taken) {
            throw new DuplicateIdentityException(key);
        }
    }
}
