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
package org.springframework.samples.petclinic.service;

/**
 * Thrown when an {@link org.springframework.samples.petclinic.model.Owner} is supplied with an
 * email whose domain is on the disposable-domain blocklist (mailinator.com, tempmail.com,
 * guerrillamail.com). Signals invalid request input, mapped to HTTP 400.
 */
public class DisposableEmailDomainException extends RuntimeException {

    public DisposableEmailDomainException(String domain) {
        super("Email domain '" + domain + "' is not allowed (disposable-domain blocklist)");
    }
}
