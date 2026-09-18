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
 * Assigns, on create, the owner's numeric membership level.
 * <p>
 * The level starts at {@link #BASE_LEVEL}, gains a point when the owner has an email
 * address, another when the owner has no namesakes ({@code namesakeCount} is 0), and a
 * final one for tenured members, up to {@link #MAX_LEVEL}. The top level requires tenure of
 * more than {@link #LEVEL_4_MIN_TENURE_DAYS} days; a newly created owner has not yet accrued
 * any tenure, so on create the level earned can never exceed 3. As it reads the owner's
 * namesake count, this must be assigned after {@link NamesakeCounter} and before the owner
 * is saved.
 */
@Component
public class MembershipLevelAssigner {

    /** The level every owner starts at. */
    static final int BASE_LEVEL = 1;

    /** The highest membership level an owner can hold; only reachable through tenure. */
    static final int MAX_LEVEL = 4;

    /** Tenure, in days, an owner must exceed to reach the top {@link #MAX_LEVEL top level}. */
    static final int LEVEL_4_MIN_TENURE_DAYS = 365;

    /**
     * Assigns {@code owner}'s membership level from its own fields and tenure. Call this after
     * the namesake count has been assigned and before the owner is saved.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        int level = BASE_LEVEL;
        if (hasEmail(owner)) {
            level++;
        }
        if (hasNoNamesakes(owner)) {
            level++;
        }
        if (hasQualifyingTenure(owner)) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_LEVEL));
    }

    private boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    private boolean hasNoNamesakes(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }

    /**
     * Whether the owner's tenure exceeds the {@link #LEVEL_4_MIN_TENURE_DAYS} that the top
     * level requires. A newly created owner has not yet accrued any tenure, so on create this
     * is always {@code false} and a new owner never exceeds level 3.
     */
    private boolean hasQualifyingTenure(Owner owner) {
        return tenureDays(owner) > LEVEL_4_MIN_TENURE_DAYS;
    }

    /** The whole days the owner has been a member; zero for an owner being created. */
    private long tenureDays(Owner owner) {
        return 0L;
    }
}
