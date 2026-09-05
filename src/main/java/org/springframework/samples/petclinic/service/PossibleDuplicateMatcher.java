/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.service;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects a soft duplicate: an owner that is not a hard duplicate (its identity key differs)
 * but shares an existing owner's postcode and last-name soundex. Returns the id of the first
 * such existing owner, or {@code null} when there is none.
 */
public final class PossibleDuplicateMatcher {

    private PossibleDuplicateMatcher() {
    }

    public static Integer matchId(Collection<Owner> existing, Owner owner) {
        if (owner.getPostcode() == null || owner.getLastName() == null) {
            return null;
        }
        String soundex = Soundex.of(owner.getLastName());
        String key = IdentityKey.of(owner);
        return existing.stream()
            .filter(o -> soundex.equals(Soundex.of(o.getLastName()))
                && owner.getPostcode().equals(o.getPostcode())
                && !key.equals(IdentityKey.of(o)))
            .map(Owner::getId)
            .findFirst()
            .orElse(null);
    }
}
