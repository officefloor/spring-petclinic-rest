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

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single source of truth for extracting an owner's region from its primary identifier.
 * Today the primary identifier is the customerCode, formatted '&lt;REGION&gt;-&lt;HASH8&gt;', so the
 * region is the prefix before the first '-'. Every consumer (segment, timezone, locality)
 * reads the region through here so the extraction lives in exactly one place.
 */
public final class RegionCode {

    private RegionCode() {
    }

    public static String of(Owner owner) {
        String code = owner.getCustomerCode();
        return code.substring(0, code.indexOf('-'));
    }
}
