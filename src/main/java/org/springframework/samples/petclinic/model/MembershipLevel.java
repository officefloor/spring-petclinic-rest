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
 * The mapping from an owner's membership points to their membership level. Owns the
 * level boundaries so they live in one place rather than being scattered across callers:
 * 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * The membership level (1 to 4) earned by the given number of membership points.
     */
    public static int forPoints(int points) {
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
