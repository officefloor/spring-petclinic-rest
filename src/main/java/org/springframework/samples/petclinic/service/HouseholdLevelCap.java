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
import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Caps a new owner's {@link MembershipLevel} at one above the current maximum level among
 * the other (non-deleted) members of their household. With no existing household member
 * the owner's own level applies unchanged.
 */
public final class HouseholdLevelCap {

    private HouseholdLevelCap() {
    }

    public static int cap(Collection<Owner> owners, Owner owner) {
        String householdId = HouseholdMatcher.householdId(owner);
        OptionalInt max = owners.stream()
            .filter(o -> !Boolean.TRUE.equals(o.getDeleted()))
            .filter(o -> householdId.equals(HouseholdMatcher.householdId(o)))
            .mapToInt(MembershipLevel::of)
            .max();
        int level = MembershipLevel.of(owner);
        return max.isPresent() ? Math.min(level, max.getAsInt() + 1) : level;
    }
}
