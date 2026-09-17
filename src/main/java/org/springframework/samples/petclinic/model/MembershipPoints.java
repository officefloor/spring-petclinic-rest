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
package org.springframework.samples.petclinic.model;

/**
 * Scores an owner's membership from the owner's own fields.
 *
 * <p>The score starts at {@code 0} and accumulates: a present email adds
 * {@value #EMAIL_POINTS}; having no namesakes (namesakeCount is 0) adds
 * {@value #NO_NAMESAKE_POINTS}; a household of {@value #LARGE_HOUSEHOLD_SIZE} or more
 * adds {@value #LARGE_HOUSEHOLD_POINTS}; and tenure of more than
 * {@value #TENURE_FISCAL_YEARS} elapsed fiscal year adds {@value #TENURE_POINTS}. The
 * resulting points are mapped to a level by {@link MembershipLevel}.
 */
public final class MembershipPoints {

    /** Points added when the owner has an email address. */
    public static final int EMAIL_POINTS = 2;

    /** Points added when the owner has no namesakes (namesakeCount is 0). */
    public static final int NO_NAMESAKE_POINTS = 1;

    /** Household size at or above which the large-household points apply. */
    public static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Points added for a household of {@value #LARGE_HOUSEHOLD_SIZE} or more. */
    public static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Elapsed fiscal years that an owner's tenure must exceed to earn the tenure points. */
    public static final int TENURE_FISCAL_YEARS = 1;

    /** Points added when tenure exceeds {@value #TENURE_FISCAL_YEARS} elapsed fiscal year. */
    public static final int TENURE_POINTS = 3;

    private MembershipPoints() {
    }

    /**
     * Score the given owner's membership points.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the total points, starting at {@code 0}.
     */
    public static int of(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += NO_NAMESAKE_POINTS;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (Tenure.inFiscalYears(owner) > TENURE_FISCAL_YEARS) {
            points += TENURE_POINTS;
        }
        return points;
    }
}
