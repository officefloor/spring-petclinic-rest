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
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.Localities;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

/**
 * Rejects a create-owner request whose 4-digit postcode falls outside the range pinned for
 * its city's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), responding 400 Bad Request.
 * A city with no known region accepts any 4-digit postcode, and an absent postcode is left
 * untouched. Malformed postcodes are left to the generic pattern validation. Applies only to
 * the create endpoint.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerPostcodeAdvice implements RequestBodyAdvice {

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGE =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

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
        OwnerFieldsDto owner = (OwnerFieldsDto) body;
        if (isOutOfRange(owner.getPostcode(), owner.getCity())) {
            throw new InvalidPostcodeException();
        }
        return body;
    }

    private boolean isOutOfRange(String postcode, String city) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return false;
        }
        int[] range = REGION_RANGE.get(Localities.regionFor(city));
        if (range == null) {
            return false;
        }
        int value = Integer.parseInt(postcode);
        return value < range[0] || value > range[1];
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

    @ExceptionHandler(InvalidPostcodeException.class)
    @ResponseBody
    public ResponseEntity<Void> handleInvalidPostcode(InvalidPostcodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /** Signals that the postcode is out of range for the city's region. */
    static class InvalidPostcodeException extends RuntimeException {
    }
}
