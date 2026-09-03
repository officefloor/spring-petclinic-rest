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

import java.util.List;
import java.util.Locale;

import org.springframework.validation.Errors;

/**
 * Rejects an owner whose email address uses a known disposable-email domain, so throwaway
 * mailboxes cannot be used to register.
 */
final class DisposableEmailDomains {

    private static final List<String> BLOCKED =
        List.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    static void reject(String email, Errors errors) {
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        String domain = at < 0 ? "" : email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED.contains(domain)) {
            errors.rejectValue("email", "email", "must not use a disposable-email domain");
        }
    }
}
