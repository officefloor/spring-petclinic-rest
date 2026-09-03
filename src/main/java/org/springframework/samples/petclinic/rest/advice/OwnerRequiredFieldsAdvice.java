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
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

/**
 * Rejects a create/update owner request whose required text fields (firstName, lastName,
 * address, city, telephone) are missing or blank, responding 400 with a JSON body whose
 * {@code errors} array names each offending field. The check runs before Bean Validation
 * so blank (whitespace-only) values are caught in addition to absent ones.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerRequiredFieldsAdvice implements RequestBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return OwnerFieldsDto.class.equals(targetType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        OwnerFieldsDto owner = (OwnerFieldsDto) body;
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", owner.getFirstName());
        addIfBlank(missing, "lastName", owner.getLastName());
        if (isBlank(owner.getAddress()) && isBlank(owner.getAddressLine1())) {
            missing.add("address");
        }
        addIfBlank(missing, "city", owner.getCity());
        addIfBlank(missing, "telephone", owner.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        return body;
    }

    private void addIfBlank(List<String> missing, String field, String value) {
        if (isBlank(value)) {
            missing.add(field);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @ExceptionHandler(MissingOwnerFieldsException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleMissingOwnerFields(MissingOwnerFieldsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", e.getFields()));
    }

    /** Signals that one or more required owner fields were missing or blank. */
    static class MissingOwnerFieldsException extends RuntimeException {

        private final List<String> fields;

        MissingOwnerFieldsException(List<String> fields) {
            this.fields = fields;
        }

        List<String> getFields() {
            return this.fields;
        }
    }
}
