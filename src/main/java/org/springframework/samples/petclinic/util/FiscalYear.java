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

import java.time.LocalDate;

/**
 * Shared fiscal-year rule. Generic calendar logic with no dependency on any particular domain object,
 * kept here so every value that depends on the fiscal-year boundary (the {@code <FY>} segment baked
 * into an owner's {@code memberId} and the owner's membership-tenure calculation) computes it from one
 * definition rather than each copying the boundary and the "year it ends in" convention.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * The fiscal year a date falls in. The fiscal year starts on 1 July and is identified by the
     * calendar year in which it ends: a date in July through December belongs to the fiscal year
     * ending the following calendar year, a date in January through June to the fiscal year ending
     * that same calendar year.
     *
     * @param date the date whose fiscal year is wanted
     * @return the calendar year in which the fiscal year ends
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }
}
