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

package org.springframework.samples.petclinic.rest.advice;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDate;

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
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

/**
 * Rejects a create-owner request once 100 or more owners have already been created today
 * (by registrationDate), responding 429 Too Many Requests. Applies only to the create
 * endpoint so updates are unaffected.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerDailyLimitAdvice implements RequestBodyAdvice {

    private static final int DAILY_LIMIT = 100;

    private final ClinicService clinicService;

    public OwnerDailyLimitAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = methodParameter.getMethod();
        return OwnerFieldsDto.class.equals(targetType) && method != null
            && "addOwner".equals(method.getName());
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (createdTodayCount() >= DAILY_LIMIT) {
            throw new DailyLimitReachedException();
        }
        return body;
    }

    private int createdTodayCount() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
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

    @ExceptionHandler(DailyLimitReachedException.class)
    @ResponseBody
    public ResponseEntity<Void> handleDailyLimitReached(DailyLimitReachedException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    /** Signals that {@value #DAILY_LIMIT} or more owners were already created today. */
    static class DailyLimitReachedException extends RuntimeException {
    }
}
