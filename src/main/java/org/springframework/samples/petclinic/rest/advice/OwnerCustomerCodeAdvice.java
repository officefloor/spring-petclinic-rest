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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Assigns a customerCode '<REGION>-<HASH8>' to each newly-created owner just before it is saved:
 * REGION is the region code derived from the postcode (its 4-digit range, else "UNKNOWN") and HASH8
 * is the first 8 upper-case hex characters of SHA-256 over (normalizedTelephone + lastName). Kept as
 * its own small aspect so this rule stays a self-contained unit rather than growing the controller
 * or service.
 */
@Aspect
@Component
public class OwnerCustomerCodeAdvice {

    /** Postcode band (4-digit / 100) -> region code. */
    private static final Map<Integer, String> REGION = Map.of(20, "NSW", 30, "VIC", 40, "QLD");

    private final OwnerCustomerCodeDeduplicator deduplicator;

    public OwnerCustomerCodeAdvice(OwnerCustomerCodeDeduplicator deduplicator) {
        this.deduplicator = deduplicator;
    }

    @Before("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public void assignCustomerCode(Owner owner) {
        if (owner.isNew() && owner.getCustomerCode() == null) {
            String pc = owner.getPostcode();
            int band = pc != null && pc.matches("[0-9]{4}") ? Integer.parseInt(pc) / 100 : -1;
            try {
                byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest((owner.getTelephone() + owner.getLastName()).getBytes(StandardCharsets.UTF_8));
                String hash8 = String.format("%02X%02X%02X%02X", d[0], d[1], d[2], d[3]);
                owner.setCustomerCode(deduplicator.unique(REGION.getOrDefault(band, "UNKNOWN") + "-" + hash8));
            }
            catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
