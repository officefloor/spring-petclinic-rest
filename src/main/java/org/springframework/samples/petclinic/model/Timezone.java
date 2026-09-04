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

import java.util.Map;

/**
 * Derives an owner's {@code timezone} from its region using a fixed
 * region-to-timezone table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
 * QLD-&gt;Australia/Brisbane).
 */
public final class Timezone {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /**
     * @param region the owner's region
     * @return the IANA timezone name for {@code region}, or {@code null} when the
     *         region is not in the table
     */
    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
