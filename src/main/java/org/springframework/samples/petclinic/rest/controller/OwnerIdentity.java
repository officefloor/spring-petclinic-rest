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
import org.springframework.stereotype.Component;

/**
 * Owner identity: the single derived key that consolidates telephone, email and household
 * into one value used for duplicate detection. Two owners are the same identity only when
 * their whole key matches; because the (normalized) telephone is part of the key, two members
 * of one household with different telephones have distinct identities.
 * <p>
 * The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, built
 * from the owner's already-canonical fields (telephone in E.164 form, email trimmed and
 * lower-cased). A {@code null} field contributes an empty segment so distinct fields cannot
 * collide across the {@code '|'} separators.
 */
@Component
public class OwnerIdentity {

    /**
     * The identity key for {@code owner}, derived from its normalized telephone, email and
     * household identifier.
     */
    public String key(Owner owner) {
        return segment(owner.getTelephone())
            + "|" + segment(owner.getEmail())
            + "|" + segment(owner.getHouseholdId());
    }

    private String segment(String value) {
        return value == null ? "" : value;
    }
}
