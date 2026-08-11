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

/**
 * Thrown when a submitted owner email's domain is on the disposable-domain blocklist
 * (mailinator.com, tempmail.com, guerrillamail.com). Carries the offending (raw) value so it can
 * be reported to the client.
 */
public class DisposableEmailException extends RuntimeException {

    private final String rejectedValue;

    public DisposableEmailException(String rejectedValue) {
        super("Email domain is on the disposable-domain blocklist: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
