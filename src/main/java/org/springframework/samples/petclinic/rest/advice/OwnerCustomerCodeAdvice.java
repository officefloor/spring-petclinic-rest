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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Assigns the unified {@code memberId} '<REGION><FY><HASH8><CHK>' to each newly-created owner just
 * before it is saved, delegating the derivation to {@link OwnerMemberId} and the collision handling
 * to the deduplicator. Kept as its own small aspect so this rule stays a self-contained unit rather
 * than growing the controller or service.
 */
@Aspect
@Component
public class OwnerCustomerCodeAdvice {

    private final OwnerCustomerCodeDeduplicator deduplicator;

    public OwnerCustomerCodeAdvice(OwnerCustomerCodeDeduplicator deduplicator) {
        this.deduplicator = deduplicator;
    }

    @Before("execution(* org.springframework.samples.petclinic.service.ClinicService.saveOwner(..)) && args(owner)")
    public void assignCustomerCode(Owner owner) {
        if (owner.isNew() && owner.getCustomerCode() == null) {
            owner.setCustomerCode(deduplicator.unique(OwnerMemberId.base(owner)));
        }
    }
}
