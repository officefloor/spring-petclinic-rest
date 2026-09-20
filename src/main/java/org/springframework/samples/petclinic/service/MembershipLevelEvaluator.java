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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Derives an owner's numeric membership level. The level starts at 1 and gains a
 * point for each qualifying attribute:
 * <ul>
 *   <li>+1 when the owner has an email address on file;</li>
 *   <li>+1 when the owner has no namesakes ({@code namesakeCount} is 0);</li>
 *   <li>+1 when the owner's tenure exceeds {@value #TENURE_THRESHOLD_DAYS} days.</li>
 * </ul>
 * The result is capped at {@value #MAX_LEVEL}. Because a brand-new owner has zero
 * tenure, only the first two attributes can apply on creation, so a new owner never
 * exceeds level 3; level 4 is attainable only once tenure passes the threshold.
 */
@Component
public class MembershipLevelEvaluator {

    /** The level every owner starts at before any qualifying attribute is counted. */
    static final int BASE_LEVEL = 1;

    /** Highest level attainable. */
    static final int MAX_LEVEL = 4;

    /** Tenure, in days, that must be exceeded for the tenure attribute to apply. */
    static final long TENURE_THRESHOLD_DAYS = 365;

    private final Clock clock;

    public MembershipLevelEvaluator() {
        this(Clock.systemDefaultZone());
    }

    MembershipLevelEvaluator(Clock clock) {
        this.clock = clock;
    }

    /**
     * @param owner the owner whose level is evaluated
     * @return the owner's membership level, or {@code null} when the owner is not known
     */
    public Integer levelFor(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = BASE_LEVEL;
        if (owner.hasEmail()) {
            level++;
        }
        if (hasNoNamesakes(owner)) {
            level++;
        }
        if (hasQualifyingTenure(owner)) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }

    private boolean hasNoNamesakes(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }

    private boolean hasQualifyingTenure(Owner owner) {
        return owner.tenureInDays(LocalDate.now(clock)) > TENURE_THRESHOLD_DAYS;
    }
}
