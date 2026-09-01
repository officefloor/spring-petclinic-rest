/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.advice;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code timezone} as the IANA name pinned to their locality/region via a fixed
 * table (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane), and null when the
 * region is absent from it. Kept as a small standalone unit so the response mapper can expose the
 * value without growing.
 */
public final class OwnerTimezone {

    private OwnerTimezone() {
    }

    /** Region code -> IANA timezone. */
    private static final Map<String, String> TIMEZONE =
        Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    public static String of(Owner owner) {
        String code = owner.getCustomerCode();
        int dash = code == null ? -1 : code.indexOf('-');
        return dash < 0 ? null : TIMEZONE.get(code.substring(0, dash));
    }
}
