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
 * Assigns, on create, the owner's membership points and the numeric membership level they
 * band into.
 * <p>
 * Points start at zero and accrue from the owner's standing: {@link #EMAIL_POINTS} when the
 * owner has an email address, {@link #NO_NAMESAKE_POINTS} when the owner has no namesakes
 * ({@code namesakeCount} is 0), {@link #LARGE_HOUSEHOLD_POINTS} for a household of
 * {@link #LARGE_HOUSEHOLD_MIN_SIZE} or more, and {@link #TENURE_POINTS} for tenure of more
 * than {@link #QUALIFYING_TENURE_FISCAL_YEARS} fiscal years. The total bands into a level of 1 (0-1 points),
 * 2 (2-3), 3 (4-5) or 4 (6 or more). A newly created owner has not yet accrued any tenure, so
 * on create the tenure points are never earned. As it reads the owner's namesake count and
 * household size, this must be assigned after {@link NamesakeCounter} and
 * {@link HouseholdSizeAssigner}, and before the owner is saved.
 */
@Component
public class MembershipLevelAssigner {

    /** Points awarded when the owner has an email address. */
    static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has no namesakes ({@code namesakeCount} is 0). */
    static final int NO_NAMESAKE_POINTS = 1;

    /** Points awarded for a household of {@link #LARGE_HOUSEHOLD_MIN_SIZE} or more. */
    static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Points awarded for tenure of more than {@link #QUALIFYING_TENURE_FISCAL_YEARS} fiscal years. */
    static final int TENURE_POINTS = 3;

    /** Household size, in members, at or above which {@link #LARGE_HOUSEHOLD_POINTS} apply. */
    static final int LARGE_HOUSEHOLD_MIN_SIZE = 3;

    /** Tenure, in elapsed fiscal years, an owner must exceed to earn the {@link #TENURE_POINTS}. */
    static final int QUALIFYING_TENURE_FISCAL_YEARS = 1;

    /**
     * Assigns {@code owner}'s membership points and the level they band into. Call this after
     * the namesake count and household size have been assigned and before the owner is saved.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        int points = points(owner);
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelForPoints(points));
    }

    /** The owner's total membership points, tallied from its standing. */
    private int points(Owner owner) {
        int points = 0;
        if (hasEmail(owner)) {
            points += EMAIL_POINTS;
        }
        if (hasNoNamesakes(owner)) {
            points += NO_NAMESAKE_POINTS;
        }
        if (hasLargeHousehold(owner)) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (hasQualifyingTenure(owner)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /** Bands a point total into a membership level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
    private int levelForPoints(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    private boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    private boolean hasNoNamesakes(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }

    private boolean hasLargeHousehold(Owner owner) {
        return owner.getHouseholdSize() != null && owner.getHouseholdSize() >= LARGE_HOUSEHOLD_MIN_SIZE;
    }

    /**
     * Whether the owner's tenure exceeds the {@link #QUALIFYING_TENURE_FISCAL_YEARS} the tenure
     * points require. A newly created owner has not yet accrued any tenure, so on create this is
     * always {@code false} and a new owner never earns the tenure points.
     */
    private boolean hasQualifyingTenure(Owner owner) {
        return tenureFiscalYears(owner) > QUALIFYING_TENURE_FISCAL_YEARS;
    }

    /** The whole fiscal years the owner has been a member; zero for an owner being created. */
    private long tenureFiscalYears(Owner owner) {
        return 0L;
    }
}
