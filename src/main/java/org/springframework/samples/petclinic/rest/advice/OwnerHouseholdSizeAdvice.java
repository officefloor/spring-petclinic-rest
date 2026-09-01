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

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Records, on each newly-created owner just before it is saved, how many owners will share its
 * household once this one is stored — the existing members plus this owner. Household membership is
 * the same key the mapper derives {@code householdId} from (normalized lastName + address), so a
 * size of 3+ is exactly what the mapper turns into the GOLD membership tier. Kept as its own small
 * aspect so this rule stays a self-contained unit.
 */
@Aspect
@Component
public class OwnerHouseholdSizeAdvice {

    private final OwnerRepository ownerRepository;

    public OwnerHouseholdSizeAdvice(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Before("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public void assignHouseholdSize(Owner owner) {
        if (owner.isNew() && owner.getHouseholdSize() == null) {
            String household = householdKey(owner);
            long existing = ownerRepository.findAll().stream()
                .filter(other -> household.equals(householdKey(other)))
                .count();
            owner.setHouseholdSize((int) (existing + 1));
        }
    }

    private static String householdKey(Owner owner) {
        String normalized = (owner.getLastName() + "|" + owner.getAddress())
            .trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return UUID.nameUUIDFromBytes(normalized.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
