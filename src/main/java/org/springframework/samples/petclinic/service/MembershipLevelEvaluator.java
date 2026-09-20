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

package org.springframework.samples.petclinic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.OptionalInt;

/**
 * Scores an owner's membership and maps that score to a numeric level. Points start at 0
 * and accrue for each qualifying attribute:
 * <ul>
 *   <li>+{@value #EMAIL_POINTS} when the owner has an email address on file;</li>
 *   <li>+{@value #NO_NAMESAKE_POINTS} when the owner has no namesakes ({@code namesakeCount} is 0);</li>
 *   <li>+{@value #LARGE_HOUSEHOLD_POINTS} when the owner's household has {@value #LARGE_HOUSEHOLD_SIZE}
 *       or more members;</li>
 *   <li>+{@value #TENURE_POINTS} when the owner's tenure exceeds {@value #TENURE_THRESHOLD_FISCAL_YEARS}
 *       fiscal year(s).</li>
 * </ul>
 * The point total maps to a level as 1 (0-1 points), 2 (2-3), 3 (4-5) and 4 (6 or more).
 */
@Component
public class MembershipLevelEvaluator {

    /** Points gained when an email address is on file. */
    static final int EMAIL_POINTS = 2;

    /** Points gained when the owner has no namesakes. */
    static final int NO_NAMESAKE_POINTS = 1;

    /** Points gained when the owner belongs to a large household. */
    static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Points gained when tenure exceeds {@value #TENURE_THRESHOLD_FISCAL_YEARS} fiscal year(s). */
    static final int TENURE_POINTS = 3;

    /** Household size at or above which {@link #LARGE_HOUSEHOLD_POINTS} apply. */
    static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Tenure, in whole fiscal years, that must be exceeded for the tenure points to apply. */
    static final long TENURE_THRESHOLD_FISCAL_YEARS = 1;

    private final ClinicService clinicService;

    private final Clock clock;

    @Autowired
    public MembershipLevelEvaluator(ClinicService clinicService) {
        this(clinicService, Clock.systemDefaultZone());
    }

    MembershipLevelEvaluator(ClinicService clinicService, Clock clock) {
        this.clinicService = clinicService;
        this.clock = clock;
    }

    /**
     * @param owner the owner whose membership points are evaluated
     * @return the owner's membership points, or {@code null} when the owner is not known
     */
    public Integer pointsFor(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        if (owner.hasEmail()) {
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

    /**
     * @param owner the owner whose level is evaluated
     * @return the owner's membership level capped at one above the highest level among the
     *     owner's existing household members, or {@code null} when the owner is not known
     */
    public Integer levelFor(Owner owner) {
        Integer points = pointsFor(owner);
        return points == null ? null : capToHousehold(owner, levelForPoints(points));
    }

    /**
     * Caps {@code level} at one above the highest level among the owner's existing household
     * members. Member levels are evaluated uncapped to avoid recursion; the owner itself and
     * soft-deleted owners are excluded. With no household members no cap applies.
     */
    private int capToHousehold(Owner owner, int level) {
        if (owner.getHouseholdId() == null) {
            return level;
        }
        OptionalInt maxMemberLevel = clinicService.findOwnersInHousehold(owner.getHouseholdId()).stream()
            .filter(member -> !member.isDeleted())
            .filter(member -> !isSameOwner(member, owner))
            .mapToInt(member -> levelForPoints(pointsFor(member)))
            .max();
        return maxMemberLevel.isPresent() ? Math.min(level, maxMemberLevel.getAsInt() + 1) : level;
    }

    private boolean isSameOwner(Owner member, Owner owner) {
        return owner.getId() != null && owner.getId().equals(member.getId());
    }

    /**
     * Maps a point total to a membership level: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more).
     */
    private int levelForPoints(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    private boolean hasNoNamesakes(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }

    private boolean hasLargeHousehold(Owner owner) {
        return owner.getHouseholdId() != null
            && clinicService.countOwnersInHousehold(owner.getHouseholdId()) >= LARGE_HOUSEHOLD_SIZE;
    }

    private boolean hasQualifyingTenure(Owner owner) {
        return owner.tenureInFiscalYears(LocalDate.now(clock)) > TENURE_THRESHOLD_FISCAL_YEARS;
    }
}
