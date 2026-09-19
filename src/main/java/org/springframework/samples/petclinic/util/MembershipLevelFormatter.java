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

/**
 * Maps an owner's {@code membershipPoints} to their {@code membershipLevel}.
 */
public final class MembershipLevelFormatter {

    private MembershipLevelFormatter() {
    }

    /**
     * Return the membership level derived from the given membership points: level 1 for 0-1 points,
     * 2 for 2-3 points, 3 for 4-5 points and 4 for 6 or more points.
     *
     * @param points the owner's membership points, as produced by {@link MembershipPointsFormatter}
     * @return the membership level, between 1 and 4
     */
    public static int format(int points) {
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
}
