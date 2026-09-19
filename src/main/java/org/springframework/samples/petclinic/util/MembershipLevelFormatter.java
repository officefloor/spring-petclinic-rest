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
 * Derives an owner's {@code membershipLevel} from their namesake count and email address.
 */
public final class MembershipLevelFormatter {

    /** The level every owner starts at. */
    private static final int BASE_LEVEL = 1;

    /** Highest level reachable from email and namesake status; level 4 is reserved for tenure. */
    private static final int MAX_LEVEL = 3;

    private MembershipLevelFormatter() {
    }

    /**
     * Return the membership level: starting at {@value #BASE_LEVEL}, add 1 when an email address is
     * present and add 1 when the owner has no namesakes ({@code namesakeCount} is {@code 0}), capped
     * at {@value #MAX_LEVEL}.
     *
     * @param namesakeCount the number of owners sharing this owner's name, may be {@code null}
     * @param email         the owner's email address, may be {@code null}
     * @return the membership level, between {@value #BASE_LEVEL} and {@value #MAX_LEVEL}
     */
    public static int format(Integer namesakeCount, String email) {
        int level = BASE_LEVEL;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }
}
