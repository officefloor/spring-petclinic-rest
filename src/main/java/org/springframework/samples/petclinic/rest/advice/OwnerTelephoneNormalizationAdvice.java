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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Normalizes an owner's telephone before the request reaches the controller: every
 * non-digit character is stripped, and the result must be exactly ten digits or the
 * request is rejected with a 400. Kept as its own advice so this rule stays a small,
 * self-contained unit rather than growing the controller or another handler.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerTelephoneNormalizationAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return OwnerFieldsDto.class.equals(targetType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        OwnerFieldsDto owner = (OwnerFieldsDto) body;
        owner.setTelephone(normalize(owner.getTelephone()));
        return body;
    }

    private String normalize(String telephone) {
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException();
        }
        return digits;
    }

    @ExceptionHandler(InvalidTelephoneException.class)
    @ResponseBody
    public ResponseEntity<String> handleInvalidTelephone(InvalidTelephoneException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /** Signals a telephone that is not exactly ten digits once non-digits are stripped. */
    static class InvalidTelephoneException extends RuntimeException {

        InvalidTelephoneException() {
            super("telephone must be exactly 10 digits");
        }
    }
}
