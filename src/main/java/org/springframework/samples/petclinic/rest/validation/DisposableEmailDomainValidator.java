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

import org.springframework.samples.petclinic.rest.error.DisposableEmailDomainException;
import org.springframework.stereotype.Component;

/**
 * Rejects an owner whose email address belongs to a known disposable-email provider. Expects the
 * already-normalized (trimmed, lower-cased) email produced by {@link EmailNormalizer}; an absent
 * email passes untouched. The disposable-domain taxonomy lives in
 * {@link DisposableEmailDomainClassifier}.
 */
@Component
public class DisposableEmailDomainValidator {

    private final DisposableEmailDomainClassifier classifier;

    public DisposableEmailDomainValidator(DisposableEmailDomainClassifier classifier) {
        this.classifier = classifier;
    }

    /**
     * @param email the normalized email address, or {@code null} when none was supplied
     * @throws DisposableEmailDomainException if the email's domain is on the blocklist
     */
    public void validate(String email) {
        if (this.classifier.isBlocked(email)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
