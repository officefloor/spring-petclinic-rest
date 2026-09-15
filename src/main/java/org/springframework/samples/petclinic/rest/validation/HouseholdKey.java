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

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Computes the canonical key that identifies an owner's household, namely the pair of last
 * name and postcode, joined as {@code normalizedLastName + "|" + postcode}. The last name is
 * compared case-insensitively and with runs of whitespace collapsed to a single space, so
 * cosmetic differences do not split a household; the postcode is taken verbatim. Two owners
 * belong to the same household exactly when their keys are equal, and the key is the input from
 * which {@link HouseholdIdGenerator} derives the stable household identifier.
 */
@Component
public class HouseholdKey {

    /** @return the canonical household key for {@code owner}. */
    public String of(Owner owner) {
        return of(owner.getLastName(), owner.getPostcode());
    }

    /** @return the canonical household key for the given last name and postcode. */
    public String of(String lastName, String postcode) {
        return normalize(lastName) + "|" + (postcode == null ? "" : postcode);
    }

    /** Trim, collapse internal whitespace and lower-case, so equality ignores case and spacing. */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
