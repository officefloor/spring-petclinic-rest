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

import org.springframework.stereotype.Component;

/**
 * Rejects an owner's email when its domain belongs to a known disposable-email provider.
 * An absent (null) email is accepted; the domain is compared case-insensitively. The set of
 * blocked providers lives in {@link DisposableEmailDomains}.
 */
@Component
public class DisposableEmailDomainValidator {

    /**
     * @param email the owner's email (may be {@code null}); expected already normalized
     * @throws DisposableEmailDomainException if the email's domain is on the blocklist
     */
    public void validate(String email) {
        if (DisposableEmailDomains.isBlocked(email)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
