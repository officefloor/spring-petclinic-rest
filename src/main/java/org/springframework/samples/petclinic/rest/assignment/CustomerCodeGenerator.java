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

package org.springframework.samples.petclinic.rest.assignment;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Regions;
import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where REGION is the
 * region derived from the postcode (see {@link Regions#regionOfPostcode(String)}) and HASH8 is the
 * first 8 upper-case hex characters of SHA-256 over the normalized telephone concatenated with the
 * last name (e.g. {@code 'NSW-1A2B3C4D'}).
 *
 * <p>When the formatted code collides with an existing owner's {@code customerCode}, {@code '-<n>'}
 * is appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners
 * always receive distinct customer codes.
 */
@Component
public class CustomerCodeGenerator {

    private static final int HASH_LENGTH = 8;

    private final ClinicService clinicService;

    public CustomerCodeGenerator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param postcode  the owner's postcode, used to derive the REGION prefix
     * @param telephone the owner's normalized telephone, hashed with the last name
     * @param lastName  the owner's last name, hashed with the telephone
     * @return the formatted customer code, de-duplicated against existing owners
     */
    public String generate(String postcode, String telephone, String lastName) {
        String region = Regions.regionOfPostcode(postcode);
        String hash8 = Sha256.hex(telephone + lastName).substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        return deduplicate(region + "-" + hash8);
    }

    /**
     * Return {@code base} if no existing owner already uses it; otherwise append {@code '-<n>'}
     * with the smallest {@code n >= 2} that is not yet taken.
     */
    private String deduplicate(String base) {
        Set<String> existing = existingCustomerCodes();
        if (!existing.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    private Set<String> existingCustomerCodes() {
        Set<String> codes = new HashSet<>();
        for (Owner owner : this.clinicService.findAllOwners()) {
            String code = owner.getCustomerCode();
            if (code != null) {
                codes.add(code);
            }
        }
        return codes;
    }
}
