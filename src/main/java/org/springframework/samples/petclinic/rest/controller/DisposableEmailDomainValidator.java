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

import org.springframework.samples.petclinic.model.DisposableEmailDomains;
import org.springframework.stereotype.Component;

/**
 * Rejects owner emails whose domain belongs to a known disposable-email provider.
 *
 * <p>Its single responsibility is to decide whether a given address is disposable and reject it;
 * the blocklist itself lives in {@link DisposableEmailDomains}, and email syntax and normalization
 * are handled separately by {@link EmailNormalizer}.
 */
@Component
public class DisposableEmailDomainValidator {

    /**
     * Validates that the address does not use a disposable-email domain.
     *
     * @param email a normalized email address, or {@code null} when none was supplied
     * @throws DisposableEmailDomainException if the domain is on the blocklist
     */
    public void validate(String email) {
        String domain = DisposableEmailDomains.domainOf(email);
        if (DisposableEmailDomains.isDisposable(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}
