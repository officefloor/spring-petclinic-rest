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
 * Derives an owner's numeric {@code membershipLevel} assigned on creation: starts at
 * {@code 1}, adds {@code 1} when an email is present, adds {@code 1} when
 * {@code namesakeCount} is {@code 0}, and is capped at {@code 3} (level {@code 4} is
 * reserved for tenure).
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * @param namesakeCount number of pre-existing namesakes when the owner was created
     * @param email         the owner's email, if any
     * @return the membership level from {@code 1} to {@code 3}
     */
    public static int of(Integer namesakeCount, String email) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (Integer.valueOf(0).equals(namesakeCount)) {
            level++;
        }
        return Math.min(level, 3);
    }
}
