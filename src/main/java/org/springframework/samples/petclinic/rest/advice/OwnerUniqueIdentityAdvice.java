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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Rejects creating an owner whose derived {@code identityKey} exactly matches an existing
 * owner's, answering 409. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId} and is the single
 * source of duplicate detection: telephone, email and household all contribute to it, so only
 * a whole-key match is a duplicate (household members with different telephones differ and are
 * both allowed). Kept as its own small advice so the rule stays a self-contained unit.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerUniqueIdentityAdvice extends RequestBodyAdviceAdapter {

    private final ClinicService clinicService;

    public OwnerUniqueIdentityAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = methodParameter.getMethod();
        return OwnerFieldsDto.class.equals(targetType)
            && method != null && "addOwner".equals(method.getName());
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        OwnerFieldsDto dto = (OwnerFieldsDto) body;
        String contact = OwnerIdentityKey.telephone(dto.getTelephone()) + '|' + OwnerIdentityKey.email(dto.getEmail());
        boolean duplicate = clinicService.findAllOwners().stream()
            .map(o -> OwnerIdentityKey.telephone(o.getTelephone()) + '|' + OwnerIdentityKey.email(o.getEmail()))
            .anyMatch(contact::equals);
        if (duplicate) {
            throw new DuplicateIdentityException();
        }
        return body;
    }

    @ExceptionHandler(DuplicateIdentityException.class)
    @ResponseBody
    public ResponseEntity<String> handleDuplicateIdentity(DuplicateIdentityException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    /** Signals an owner whose whole identityKey is already used by another owner. */
    static class DuplicateIdentityException extends RuntimeException {

        DuplicateIdentityException() {
            super("owner with the same identity already exists");
        }
    }
}
