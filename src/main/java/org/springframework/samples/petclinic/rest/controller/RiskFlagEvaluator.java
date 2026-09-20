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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Aggregates an owner's soft risk signals into a single flag: an owner is risky when it is a
 * possible duplicate, or its email domain is disposable-adjacent, or its city is over its soft
 * capacity. Each underlying signal is owned by its own component; this evaluator only combines
 * them.
 */
@Component
public class RiskFlagEvaluator {

    private final DisposableEmailDomainEvaluator disposableEmailDomainEvaluator;

    private final CityCapacityWarningEvaluator cityCapacityWarningEvaluator;

    public RiskFlagEvaluator(DisposableEmailDomainEvaluator disposableEmailDomainEvaluator,
            CityCapacityWarningEvaluator cityCapacityWarningEvaluator) {
        this.disposableEmailDomainEvaluator = disposableEmailDomainEvaluator;
        this.cityCapacityWarningEvaluator = cityCapacityWarningEvaluator;
    }

    /**
     * @param owner the owner to assess
     * @return {@code true} when any risk signal holds for {@code owner}, {@code false} otherwise
     *         (including for a {@code null} owner)
     */
    public boolean isRisk(Owner owner) {
        if (owner == null) {
            return false;
        }
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || disposableEmailDomainEvaluator.isDisposableAdjacent(owner)
            || cityCapacityWarningEvaluator.isWarranted(owner);
    }
}
