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
 * The membership scoring rule: an owner earns points for the loyalty factors they satisfy, and the
 * accumulated points map onto a membership level. Points start at 0 and add up as follows: 2 for
 * having an email, 1 for being uniquely named, 2 for belonging to a household of three or more, and
 * 3 for tenure of more than a year. The level is a coarser banding of that score: 1 for 0-1 points,
 * 2 for 2-3, 3 for 4-5 and 4 for 6 or more.
 */
public final class MembershipPoints {

    private static final int EMAIL_POINTS = 2;
    private static final int UNIQUE_NAME_POINTS = 1;
    private static final int LARGE_HOUSEHOLD_POINTS = 2;
    private static final int LONG_TENURE_POINTS = 3;

    private MembershipPoints() {
    }

    /**
     * The membership points earned for the given loyalty factors.
     *
     * @param hasEmail        whether the owner has an email
     * @param uniquelyNamed   whether the owner is uniquely named (no namesakes)
     * @param largeHousehold  whether the owner belongs to a household of three or more
     * @param longTenure      whether the owner's tenure exceeds a year
     * @return the total points, starting at 0
     */
    public static int of(boolean hasEmail, boolean uniquelyNamed, boolean largeHousehold, boolean longTenure) {
        int points = 0;
        if (hasEmail) {
            points += EMAIL_POINTS;
        }
        if (uniquelyNamed) {
            points += UNIQUE_NAME_POINTS;
        }
        if (largeHousehold) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (longTenure) {
            points += LONG_TENURE_POINTS;
        }
        return points;
    }

    /**
     * The membership level the given points fall into: 1 for 0-1, 2 for 2-3, 3 for 4-5, 4 for 6+.
     *
     * @param points the membership points
     * @return the membership level, from 1 to 4
     */
    public static int levelFor(int points) {
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
}
