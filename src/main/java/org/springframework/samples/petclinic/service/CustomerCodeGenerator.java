/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Produces the customer code assigned to a new {@link Owner}.
 *
 * <p>The code is the owner's {@link CustomerCode} identity {@code '<REGION>-<HASH8>'}: the region
 * derived from the owner's postcode (via {@link Locality}, so it disambiguates cities that share a
 * name and falls back to the city table) joined to a hash of the owner's normalized telephone and
 * last name (e.g. {@code 'NSW-3F2A9C1D'}). It is derived purely from owner fields — there are no
 * sequence numbers — so it needs no persistence lookups.
 */
@Component
public class CustomerCodeGenerator {

    /**
     * Generate the customer code for the given owner from its region and identity fields.
     *
     * @param owner the owner being registered
     * @return the {@code '<REGION>-<HASH8>'} customer code
     */
    public String generate(Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        return CustomerCode.of(region, owner.getTelephone(), owner.getLastName());
    }
}
