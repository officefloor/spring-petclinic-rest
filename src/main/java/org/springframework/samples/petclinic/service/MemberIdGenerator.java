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

import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.RegistrationDatePolicy;
import org.springframework.stereotype.Component;

/**
 * Produces the member id assigned to a new {@link Owner}.
 *
 * <p>The id is the owner's {@link MemberId} identity {@code '<REGION><FY><HASH8><CHK>'}: the region
 * derived from the owner's postcode (via {@link Locality}, so it disambiguates cities that share a
 * name and falls back to the city table), the two-digit fiscal year of the owner's (effective)
 * registration date, a hash of the owner's normalized telephone and last name, and a Luhn check
 * digit (e.g. {@code 'NSW273F2A9C1D5'}).
 *
 * <p>Two owners that share the same region, fiscal year, normalized telephone and last name would
 * otherwise be assigned the same identity, so when the computed id collides with an existing
 * owner's member id it is made unique by appending {@code '-<n>'} with the smallest {@code n} of 2
 * or more that is still free.
 */
@Component
public class MemberIdGenerator {

    /** Smallest suffix used to disambiguate a colliding member id. */
    private static final int FIRST_SUFFIX = 2;

    private final OwnerRepository ownerRepository;

    public MemberIdGenerator(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Generate a unique member id for the given owner from its region, fiscal year and identity fields.
     *
     * @param owner the owner being registered
     * @return the {@code '<REGION><FY><HASH8><CHK>'} member id, de-duplicated with a {@code '-<n>'}
     * suffix when it would collide with an existing owner's id
     */
    public String generate(Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        int fiscalYearSegment = FiscalYear.yearSegment(RegistrationDatePolicy.effectiveDate(owner.getRegistrationDate()));
        String memberId = MemberId.of(region, fiscalYearSegment, owner.getTelephone(), owner.getLastName());
        return deduplicate(memberId);
    }

    /**
     * Return {@code memberId} unchanged when no owner holds it, otherwise the first
     * {@code '<memberId>-<n>'} variant (starting at {@code n = 2}) that no owner holds yet.
     */
    private String deduplicate(String memberId) {
        if (!ownerRepository.existsByMemberId(memberId)) {
            return memberId;
        }
        for (int n = FIRST_SUFFIX; ; n++) {
            String candidate = memberId + "-" + n;
            if (!ownerRepository.existsByMemberId(candidate)) {
                return candidate;
            }
        }
    }
}
