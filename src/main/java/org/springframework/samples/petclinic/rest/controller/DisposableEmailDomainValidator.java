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

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Rejects owner emails whose domain belongs to a known disposable-email provider.
 * <p>
 * Validation runs only when a syntactically usable email is present; its shape is enforced
 * declaratively by the DTO's {@code @Email} constraint. Registered on the owner request binder
 * so a disposable domain surfaces through the same {@code BindingResult} as the other
 * owner-field checks, yielding a 400 response.
 */
@Component
public class DisposableEmailDomainValidator implements Validator {

    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        String domain = domainOf(owner.getEmail());
        if (domain != null && DISPOSABLE_DOMAINS.contains(domain)) {
            errors.rejectValue("email", "email.disposableDomain",
                "email domain is not accepted");
        }
    }

    /**
     * Extract the lower-cased domain from an email address, or {@code null} when none is present.
     */
    private String domainOf(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
    }
}
