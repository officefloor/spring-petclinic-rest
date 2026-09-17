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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Decides whether an owner warrants a manual risk review. The owner is flagged when any single
 * risk signal holds: it is a possible duplicate, its email domain is disposable-adjacent, or its
 * city is over its soft capacity. Each underlying signal is owned by its own collaborator; this
 * evaluator only combines them.
 */
@Component
public class OwnerRiskFlagEvaluator {

    private final DisposableEmailDomainClassifier disposableEmailDomainClassifier;

    private final CityCapacityWarningEvaluator cityCapacityWarningEvaluator;

    public OwnerRiskFlagEvaluator(DisposableEmailDomainClassifier disposableEmailDomainClassifier,
                                  CityCapacityWarningEvaluator cityCapacityWarningEvaluator) {
        this.disposableEmailDomainClassifier = disposableEmailDomainClassifier;
        this.cityCapacityWarningEvaluator = cityCapacityWarningEvaluator;
    }

    /**
     * @param owner the owner to assess
     * @return {@code true} when the owner is a possible duplicate, has a disposable-adjacent email
     * domain, or lives in a city that is over its soft capacity; {@code false} otherwise
     */
    public boolean isAtRisk(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || this.disposableEmailDomainClassifier.isDisposableAdjacent(owner.getEmail())
            || this.cityCapacityWarningEvaluator.isOverSoftCapacity(owner.getCity());
    }
}
