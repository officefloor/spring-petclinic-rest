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
 * Normalizes an owner's telephone to E.164 before the request reaches the controller: a
 * leading '+' keeps its country code, otherwise country code '+61' is assumed and a single
 * leading '0' dropped; spaces, dashes and brackets are stripped and 8 to 15 digits must
 * remain after the '+' or the request is rejected with a 400. Kept as its own advice so
 * this rule stays a small, self-contained unit rather than growing the controller or another
 * handler.
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
        owner.setTelephone(toE164(owner.getTelephone()));
        checkNationalLength(owner.getTelephone());
        return body;
    }

    /** National-number digit count required per E.164 country code. */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of("61", 9, "1", 10);

    /**
     * Rejects a telephone whose national-number length is wrong for its country code:
     * '+61' requires 9 national digits and '+1' requires 10. Country codes not listed
     * here keep only the generic E.164 length rule applied in {@link #toE164}.
     */
    private static void checkNationalLength(String e164) {
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> country : NATIONAL_DIGITS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code) && digits.length() - code.length() != country.getValue()) {
                throw new InvalidTelephoneException();
            }
        }
    }

    /** Converts a telephone to its E.164 string, or throws when no valid form exists. */
    static String toE164(String telephone) {
        String trimmed = telephone == null ? "" : telephone.trim();
        String digits = trimmed.replaceAll("\\D", "");
        if (!trimmed.startsWith("+")) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new InvalidTelephoneException();
        }
        return "+" + digits;
    }

    @ExceptionHandler(InvalidTelephoneException.class)
    @ResponseBody
    public ResponseEntity<String> handleInvalidTelephone(InvalidTelephoneException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /** Signals a telephone that cannot form a valid E.164 number. */
    static class InvalidTelephoneException extends RuntimeException {

        InvalidTelephoneException() {
            super("telephone must form a valid E.164 number");
        }
    }
}
