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

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.stereotype.Component;

/**
 * Detects whether a would-be owner duplicates an existing one. Duplication is decided by a
 * single derived {@link IdentityKey}: a candidate is a duplicate exactly when its whole
 * identity key equals that of some existing owner. This consolidates the former separate
 * telephone, email and household checks into one rule.
 */
@Component
public class IdentityDuplicateValidator {

    /**
     * @param candidate the owner about to be created, with its telephone, email and household id
     *                  already in their canonical (stored) form
     * @param existingOwners owners already stored to compare against (a full-key match requires an
     *                       equal telephone, so callers typically pre-filter by telephone)
     * @return {@code true} when some existing owner shares the candidate's whole identity key
     */
    public boolean isDuplicate(Owner candidate, Collection<Owner> existingOwners) {
        String key = IdentityKey.of(candidate);
        return existingOwners.stream().anyMatch(existing -> IdentityKey.of(existing).equals(key));
    }
}
