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
 * Derives an owner's {@code membershipTier}: {@code SILVER} for a unique namesake
 * ({@code namesakeCount} of 0) that has an email present, otherwise {@code BRONZE}.
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    /**
     * @param namesakeCount number of pre-existing namesakes when the owner was created
     * @param email         the owner's email, if any
     * @return {@code "SILVER"} when {@code namesakeCount} is 0 and {@code email} is present,
     *         otherwise {@code "BRONZE"}
     */
    public static String of(Integer namesakeCount, String email) {
        boolean silver = Integer.valueOf(0).equals(namesakeCount) && email != null && !email.isBlank();
        return silver ? "SILVER" : "BRONZE";
    }
}
