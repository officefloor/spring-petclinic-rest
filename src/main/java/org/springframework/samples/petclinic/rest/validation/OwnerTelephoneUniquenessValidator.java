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

import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Enforces that an owner's normalized telephone is unique across all owners. Telephones are
 * compared in their canonical storage form (see {@link TelephoneNormalizer}), so two owners whose
 * submitted telephones differ only in formatting are still treated as duplicates.
 */
@Component
public class OwnerTelephoneUniquenessValidator {

    private final ClinicService clinicService;

    public OwnerTelephoneUniquenessValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Rejects a normalized telephone that is already used by any existing owner.
     *
     * @param normalizedTelephone the normalized telephone of the owner being created
     * @throws DuplicateOwnerTelephoneException if another owner already uses this telephone
     */
    public void validateUnique(String normalizedTelephone) {
        if (!this.clinicService.findOwnerByTelephone(normalizedTelephone).isEmpty()) {
            throw new DuplicateOwnerTelephoneException(normalizedTelephone);
        }
    }
}
