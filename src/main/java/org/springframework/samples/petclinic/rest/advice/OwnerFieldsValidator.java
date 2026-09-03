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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Rejects an {@link OwnerFieldsDto} whose required identity fields are missing or blank
 * (null, empty or whitespace-only). This complements the schema's bean-validation
 * constraints, which do not treat a whitespace-only address or city as invalid.
 */
public class OwnerFieldsValidator implements Validator {

    private static final List<String> REQUIRED_FIELDS =
        List.of("firstName", "lastName", "address", "city", "telephone");

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        owner.setAddress(composeAddress(owner));
        for (String field : REQUIRED_FIELDS) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, "required", "must not be blank");
        }
        String telephone = owner.getTelephone();
        String raw = telephone == null ? "" : telephone.replaceAll("[\\s\\-()]", "");
        String e164 = raw.startsWith("+") ? raw
            : "+61" + (raw.startsWith("0") ? raw.substring(1) : raw);
        if (E164Telephone.isValid(e164)) {
            owner.setTelephone(e164);
        } else {
            errors.rejectValue("telephone", "telephone", "must be a valid E.164 telephone number");
        }
        String email = owner.getEmail();
        if (email != null) {
            owner.setEmail(email.toLowerCase(Locale.ROOT));
        }
        DisposableEmailDomains.reject(owner.getEmail(), errors);
        validatePostcode(owner, errors);
        validateRegistrationDate(owner, errors);
    }

    /** The effective, normalized address: the composed structured lines when a non-blank
     * addressLine1 is supplied (addressLine2 appended after a single space when present),
     * otherwise the normalized flat address for backward compatibility. */
    private String composeAddress(OwnerFieldsDto owner) {
        String line1 = AddressNormalizer.normalize(owner.getAddressLine1());
        if (line1.isEmpty()) {
            return AddressNormalizer.normalize(owner.getAddress());
        }
        String line2 = AddressNormalizer.normalize(owner.getAddressLine2());
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }

    /** Rejects a supplied registrationDate later than the current server date. */
    private void validateRegistrationDate(OwnerFieldsDto owner, Errors errors) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            errors.rejectValue("registrationDate", "registrationDate", "must not be in the future");
        }
    }

    /** Rejects a supplied 4-digit postcode that falls outside the range for the city's
     * region. Absent postcodes and cities with no known region are accepted. */
    private void validatePostcode(OwnerFieldsDto owner, Errors errors) {
        String postcode = owner.getPostcode();
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return;
        }
        int[] range = switch (owner.getCity() == null ? "" : owner.getCity()) {
            case "Sydney" -> new int[] {2000, 2099};
            case "Melbourne" -> new int[] {3000, 3099};
            case "Brisbane" -> new int[] {4000, 4099};
            default -> null;
        };
        int value = Integer.parseInt(postcode);
        if (range != null && (value < range[0] || value > range[1])) {
            errors.rejectValue("postcode", "postcode", "must be valid for the city's region");
        }
    }
}
