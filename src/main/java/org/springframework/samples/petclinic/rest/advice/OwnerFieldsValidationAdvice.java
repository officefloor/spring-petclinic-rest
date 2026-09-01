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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Rejects an owner whose required text fields are missing or blank before the request
 * reaches the controller, answering with a 400 and an {@code errors} array naming each
 * offending field. Kept separate from the general {@link ExceptionControllerAdvice} so
 * neither this rule nor the existing handlers grow to accommodate the other.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerFieldsValidationAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return OwnerFieldsDto.class.equals(targetType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        List<String> missing = missingFields((OwnerFieldsDto) body);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        return body;
    }

    private List<String> missingFields(OwnerFieldsDto owner) {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", owner.getFirstName());
        addIfBlank(missing, "lastName", owner.getLastName());
        addIfBlank(missing, "address", owner.getAddress());
        addIfBlank(missing, "city", owner.getCity());
        addIfBlank(missing, "telephone", owner.getTelephone());
        return missing;
    }

    private void addIfBlank(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }

    @ExceptionHandler(MissingOwnerFieldsException.class)
    @ResponseBody
    public ResponseEntity<Map<String, List<String>>> handleMissingOwnerFields(MissingOwnerFieldsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", e.getMissingFields()));
    }

    /** Carries the names of the owner fields that were missing or blank. */
    static class MissingOwnerFieldsException extends RuntimeException {

        private final transient List<String> missingFields;

        MissingOwnerFieldsException(List<String> missingFields) {
            this.missingFields = missingFields;
        }

        List<String> getMissingFields() {
            return this.missingFields;
        }
    }
}
