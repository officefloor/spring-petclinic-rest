/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Decides whether an owner warrants a manual risk review. The "risk" flag is raised when any of
 * three independent signals hold: the owner is a {@link Owner#getPossibleDuplicate() possible
 * duplicate}, the owner's email domain is
 * {@link DisposableEmailDomainValidator#isDisposableAdjacent(String) disposable-adjacent}, or the
 * owner's city is over its soft capacity (the {@link CityCapacityWarningEvaluator capacity warning}
 * applies). Like the capacity warning it reuses, the flag is evaluated per response rather than
 * stored.
 */
@Component
public class RiskFlagEvaluator {

    private final CityCapacityWarningEvaluator cityCapacityWarningEvaluator;

    public RiskFlagEvaluator(CityCapacityWarningEvaluator cityCapacityWarningEvaluator) {
        this.cityCapacityWarningEvaluator = cityCapacityWarningEvaluator;
    }

    /**
     * @param owner the owner to evaluate
     * @return {@code true} when any risk signal holds for the owner
     */
    public boolean isFlagged(Owner owner) {
        return owner.getPossibleDuplicate()
            || DisposableEmailDomainValidator.isDisposableAdjacent(owner.getEmail())
            || this.cityCapacityWarningEvaluator.isWarranted(owner.getCity());
    }
}
