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

import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Luhn;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Regions;
import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code memberId}, formatted {@code '<REGION><FY><HASH8><CHK>'}: REGION is the
 * version-2 identity region &mdash; the {@link IdentityVersion#TAG version tag} followed by the plain
 * region derived from the postcode (see {@link Regions#regionOfPostcode(String)}), e.g. {@code 'V2NSW'}
 * &mdash; so it differs from the plain user-facing region; FY is the two-digit fiscal year of the
 * business-day-adjusted registration date, HASH8 is the first 8 upper-case hex characters of SHA-256
 * over the normalized telephone concatenated with the last name (the same HASH8 as the
 * region-and-hash identity), and CHK is a single Luhn check digit computed over the digits of
 * {@code <REGION><FY><HASH8>} (e.g. {@code 'V2NSW271A2B3C4D5'}).
 *
 * <p>When the formatted member id collides with an existing owner's {@code memberId}, {@code '-<n>'}
 * is appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners
 * always receive distinct member ids.
 */
@Component
public class MemberIdGenerator {

    private static final int HASH_LENGTH = 8;

    private final ClinicService clinicService;

    public MemberIdGenerator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param owner the owner whose member id is being assigned; its postcode fixes the REGION, its
     * effective registration date the FY, and its telephone and last name the HASH8
     * @return the formatted member id, de-duplicated against existing owners
     */
    public String generate(Owner owner) {
        String region = IdentityVersion.TAG + Regions.regionOfPostcode(owner.getPostcode());
        String fiscalYear = FiscalYear.twoDigit(owner.effectiveRegistrationDate());
        String hash8 = Sha256.hex(owner.getTelephone() + owner.getLastName())
            .substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        String base = region + fiscalYear + hash8;
        return deduplicate(base + Luhn.checkDigit(base));
    }

    /**
     * Return {@code memberId} if no existing owner already uses it; otherwise append {@code '-<n>'}
     * with the smallest {@code n >= 2} that is not yet taken.
     */
    private String deduplicate(String memberId) {
        Set<String> existing = existingMemberIds();
        if (!existing.contains(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    private Set<String> existingMemberIds() {
        Set<String> ids = new HashSet<>();
        for (Owner owner : this.clinicService.findAllOwners()) {
            String id = owner.getMemberId();
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
