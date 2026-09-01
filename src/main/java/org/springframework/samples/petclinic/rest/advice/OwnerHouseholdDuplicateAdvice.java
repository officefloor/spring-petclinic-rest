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

import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Rejects creating an owner whose derived {@code householdId} (normalized lastName + postcode)
 * already belongs to an existing owner, answering 409: owners sharing last name and postcode are
 * the same household. Setting {@code sharesHousehold} bypasses this block, declaring the owner an
 * intentional household member instead. Kept as its own small advice so the rule stays a
 * self-contained unit.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerHouseholdDuplicateAdvice extends RequestBodyAdviceAdapter {

    private final ClinicService clinicService;

    public OwnerHouseholdDuplicateAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return OwnerFieldsDto.class.equals(targetType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        OwnerFieldsDto dto = (OwnerFieldsDto) body;
        if (!Boolean.TRUE.equals(dto.getSharesHousehold())) {
            String household = OwnerIdentityKey.householdId(dto.getLastName(), dto.getPostcode());
            boolean exists = clinicService.findAllOwners().stream()
                .anyMatch(o -> household.equals(OwnerIdentityKey.householdId(o.getLastName(), o.getPostcode())));
            if (exists) {
                throw new DuplicateHouseholdException();
            }
        }
        return body;
    }

    @ExceptionHandler(DuplicateHouseholdException.class)
    @ResponseBody
    public ResponseEntity<String> handleDuplicateHousehold(DuplicateHouseholdException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    /** Signals an owner whose household (last name + postcode) already exists and is not declared shared. */
    static class DuplicateHouseholdException extends RuntimeException {

        DuplicateHouseholdException() {
            super("owner belongs to an existing household");
        }
    }
}
