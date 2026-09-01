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
import java.util.Locale;

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
 * Rejects creating an owner whose city already contains 50 or more owners, answering 409
 * (cities are compared case-insensitively with surrounding whitespace trimmed). Kept as its
 * own advice so this rule stays a small, self-contained unit rather than growing the
 * controller or another handler.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerCityCapacityAdvice extends RequestBodyAdviceAdapter {

    private static final int CITY_CAPACITY = 50;

    private final ClinicService clinicService;

    public OwnerCityCapacityAdvice(ClinicService clinicService) {
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
        String city = normalize(((OwnerFieldsDto) body).getCity());
        long count = clinicService.findAllOwners().stream()
            .map(Owner::getCity)
            .filter(existing -> normalize(existing).equals(city))
            .count();
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException();
        }
        return body;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @ExceptionHandler(CityAtCapacityException.class)
    @ResponseBody
    public ResponseEntity<String> handleCityAtCapacity(CityAtCapacityException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    /** Signals a city that already holds the maximum number of owners. */
    static class CityAtCapacityException extends RuntimeException {

        CityAtCapacityException() {
            super("city already has the maximum number of owners");
        }
    }
}
