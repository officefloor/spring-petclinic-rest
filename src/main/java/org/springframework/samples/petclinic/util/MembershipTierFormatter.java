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
 * Derives an owner's {@code membershipTier} from their namesake count and email address.
 */
public final class MembershipTierFormatter {

    private static final String SILVER = "SILVER";

    private static final String BRONZE = "BRONZE";

    private MembershipTierFormatter() {
    }

    /**
     * Return the membership tier: {@code "SILVER"} when the owner has no namesakes
     * ({@code namesakeCount} is {@code 0}) and an email address is present, otherwise
     * {@code "BRONZE"}.
     *
     * @param namesakeCount the number of owners sharing this owner's name, may be {@code null}
     * @param email         the owner's email address, may be {@code null}
     * @return {@code "SILVER"} or {@code "BRONZE"}
     */
    public static String format(Integer namesakeCount, String email) {
        boolean unique = namesakeCount != null && namesakeCount == 0;
        boolean hasEmail = email != null && !email.isBlank();
        return unique && hasEmail ? SILVER : BRONZE;
    }
}
