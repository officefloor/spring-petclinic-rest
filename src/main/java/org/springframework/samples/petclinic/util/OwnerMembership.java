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

package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

import java.util.Collection;
import java.util.OptionalInt;

/**
 * Derives an owner's {@code membershipLevel} from their scoring fields and applies the
 * household level ceiling.
 */
public final class OwnerMembership {

    private OwnerMembership() {
    }

    /**
     * Return the owner's natural (uncapped) membership level, derived purely from their own scoring
     * fields via {@link MembershipPointsFormatter} and {@link MembershipLevelFormatter}.
     *
     * @param owner the owner to score
     * @return the membership level, between 1 and 4
     */
    public static int naturalLevel(Owner owner) {
        int points = MembershipPointsFormatter.format(owner.getNamesakeCount(), owner.getEmail(),
            owner.getHouseholdSize(), owner.getRegistrationDate());
        return MembershipLevelFormatter.format(points);
    }

    /**
     * Return the owner's effective membership level as reported to clients: the capped value stored
     * at creation time when present, otherwise the {@link #naturalLevel(Owner) natural level}.
     *
     * @param owner the owner to score
     * @return the membership level, between 1 and 4
     */
    public static int level(Owner owner) {
        return owner.getMembershipLevel() != null ? owner.getMembershipLevel() : naturalLevel(owner);
    }

    /**
     * Return the owner's membership level capped by their household: it may not exceed one above the
     * current maximum level among the given existing household members. With no existing member no
     * cap applies and the {@link #naturalLevel(Owner) natural level} is returned.
     *
     * @param owner            the owner being created
     * @param householdMembers the existing members of the owner's household (excluding the owner)
     * @return the capped membership level, between 1 and 4
     */
    public static int cappedLevel(Owner owner, Collection<Owner> householdMembers) {
        int natural = naturalLevel(owner);
        OptionalInt maxMemberLevel = householdMembers.stream()
            .mapToInt(OwnerMembership::level)
            .max();
        return maxMemberLevel.isPresent() ? Math.min(natural, maxMemberLevel.getAsInt() + 1) : natural;
    }
}
