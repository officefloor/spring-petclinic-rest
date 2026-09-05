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
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.samples.petclinic.model.BusinessDay;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Flags an unusually high signup volume: the warning trips once more than
 * {@value #THRESHOLD} owners already share today's registration date.
 */
public final class BulkSignupWarning {

    public static final int THRESHOLD = 80;

    private BulkSignupWarning() {
    }

    public static boolean isTriggered(Collection<Owner> existing) {
        LocalDate today = BusinessDay.adjust(LocalDate.now());
        long createdToday = existing.stream().filter(o -> today.equals(o.getRegistrationDate())).count();
        return createdToday > THRESHOLD;
    }
}
