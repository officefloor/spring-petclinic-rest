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

package org.springframework.samples.petclinic.rest.assignment;

import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.samples.petclinic.rest.validation.HouseholdKey;
import org.springframework.stereotype.Component;

/**
 * Derives an owner's {@code householdId}: a stable identifier shared by every owner in the same
 * household. Because it is a pure function of the {@link HouseholdKey} over (last name, postcode),
 * owners with the same last name and postcode always yield the same value: the first 12 hex
 * characters of the SHA-256 of that key.
 */
@Component
public class HouseholdIdGenerator {

    private final HouseholdKey householdKey;

    public HouseholdIdGenerator(HouseholdKey householdKey) {
        this.householdKey = householdKey;
    }

    /**
     * @param lastName the owner's last name
     * @param postcode the owner's postcode
     * @return the stable household identifier for that last name and postcode
     */
    public String generate(String lastName, String postcode) {
        String key = this.householdKey.of(lastName, postcode);
        return Sha256.hex(key).substring(0, 12);
    }
}
