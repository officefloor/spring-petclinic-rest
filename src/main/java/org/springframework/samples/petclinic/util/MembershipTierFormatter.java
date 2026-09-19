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
 * Derives an owner's {@code membershipTier} from their household size, namesake count and email
 * address.
 */
public final class MembershipTierFormatter {

    private static final String GOLD = "GOLD";

    private static final String SILVER = "SILVER";

    private static final String BRONZE = "BRONZE";

    /**
     * Household size (members sharing the same household) at or above which an owner is {@code GOLD}.
     */
    private static final int GOLD_HOUSEHOLD_SIZE = 3;

    private MembershipTierFormatter() {
    }

    /**
     * Return the membership tier: {@code "GOLD"} when the owner's household has
     * {@value #GOLD_HOUSEHOLD_SIZE} or more members; otherwise {@code "SILVER"} when the owner has
     * no namesakes ({@code namesakeCount} is {@code 0}) and an email address is present, otherwise
     * {@code "BRONZE"}.
     *
     * @param householdSize the number of owners sharing this owner's household, may be {@code null}
     * @param namesakeCount the number of owners sharing this owner's name, may be {@code null}
     * @param email         the owner's email address, may be {@code null}
     * @return {@code "GOLD"}, {@code "SILVER"} or {@code "BRONZE"}
     */
    public static String format(Integer householdSize, Integer namesakeCount, String email) {
        if (householdSize != null && householdSize >= GOLD_HOUSEHOLD_SIZE) {
            return GOLD;
        }
        boolean unique = namesakeCount != null && namesakeCount == 0;
        boolean hasEmail = email != null && !email.isBlank();
        return unique && hasEmail ? SILVER : BRONZE;
    }
}
