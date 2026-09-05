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

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Maps an owner's region (the customerCode prefix, e.g. NSW) to its IANA timezone using a
 * fixed table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane),
 * returning {@code null} for any region not in the table.
 */
public final class RegionTimezone {

    private static final Map<String, String> REGION_TIMEZONE =
        Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    public static String of(Owner owner) {
        String code = owner.getCustomerCode();
        return REGION_TIMEZONE.get(code.substring(0, code.indexOf('-')));
    }
}
