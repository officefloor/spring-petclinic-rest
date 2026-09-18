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
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Produces the customer code assigned to a new {@link Owner}.
 *
 * <p>The code is the owner's {@link CustomerCode} identity {@code '<REGION>-<HASH8>'}: the region
 * derived from the owner's postcode (via {@link Locality}, so it disambiguates cities that share a
 * name and falls back to the city table) joined to a hash of the owner's normalized telephone and
 * last name (e.g. {@code 'NSW-3F2A9C1D'}).
 *
 * <p>Two owners that share the same normalized telephone and last name would otherwise be assigned
 * the same identity, so when the computed code collides with an existing owner's customer code it is
 * made unique by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that is still
 * free.
 */
@Component
public class CustomerCodeGenerator {

    /** Smallest suffix used to disambiguate a colliding customer code. */
    private static final int FIRST_SUFFIX = 2;

    private final OwnerRepository ownerRepository;

    public CustomerCodeGenerator(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Generate a unique customer code for the given owner from its region and identity fields.
     *
     * @param owner the owner being registered
     * @return the {@code '<REGION>-<HASH8>'} customer code, de-duplicated with a {@code '-<n>'} suffix
     * when it would collide with an existing owner's code
     */
    public String generate(Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String code = CustomerCode.of(region, owner.getTelephone(), owner.getLastName());
        return deduplicate(code);
    }

    /**
     * Return {@code code} unchanged when no owner holds it, otherwise the first {@code '<code>-<n>'}
     * variant (starting at {@code n = 2}) that no owner holds yet.
     */
    private String deduplicate(String code) {
        if (!ownerRepository.existsByCustomerCode(code)) {
            return code;
        }
        for (int n = FIRST_SUFFIX; ; n++) {
            String candidate = code + "-" + n;
            if (!ownerRepository.existsByCustomerCode(candidate)) {
                return candidate;
            }
        }
    }
}
