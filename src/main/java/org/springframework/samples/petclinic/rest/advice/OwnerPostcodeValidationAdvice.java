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
 * Rejects an owner whose supplied postcode falls outside the range allowed for its city's
 * region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), answering with a 400. The postcode
 * is optional: absent postcodes and cities with no known region are accepted. Kept as its
 * own advice so this rule stays a small, self-contained unit.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerPostcodeValidationAdvice extends RequestBodyAdviceAdapter {

    /** Lowest valid postcode per city, region ranges spanning that value plus 99. */
    private static final Map<String, Integer> REGION_BASE =
        Map.of("Sydney", 2000, "Melbourne", 3000, "Brisbane", 4000);

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return OwnerFieldsDto.class.equals(targetType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        OwnerFieldsDto owner = (OwnerFieldsDto) body;
        String postcode = owner.getPostcode();
        Integer base = REGION_BASE.get(owner.getCity());
        if (postcode != null && base != null && !inRange(postcode, base)) {
            throw new InvalidPostcodeException();
        }
        return body;
    }

    private boolean inRange(String postcode, int base) {
        if (!postcode.matches("[0-9]{4}")) {
            return true; // malformed postcodes are left for @Pattern bean validation to reject
        }
        int value = Integer.parseInt(postcode);
        return value >= base && value <= base + 99;
    }

    @ExceptionHandler(InvalidPostcodeException.class)
    @ResponseBody
    public ResponseEntity<String> handleInvalidPostcode(InvalidPostcodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /** Signals a postcode that is out of range for its city's region. */
    static class InvalidPostcodeException extends RuntimeException {

        InvalidPostcodeException() {
            super("postcode is not valid for the owner's city");
        }
    }
}
