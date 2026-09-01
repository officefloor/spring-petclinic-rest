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
import java.util.Locale;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Normalizes an owner's address before the request reaches the controller: whitespace is
 * trimmed and collapsed, the text is upper-cased and common street-type abbreviations are
 * expanded (ST-&gt;STREET, RD-&gt;ROAD, AVE-&gt;AVENUE). The normalized form is what gets
 * stored and returned, and (via {@link #normalize}) what address comparisons use. A blank
 * result is left for {@link OwnerFieldsValidationAdvice} to reject as a missing field. Kept
 * as its own advice so this rule stays a small, self-contained unit.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerAddressNormalizationAdvice extends RequestBodyAdviceAdapter {

    private static final Map<String, String> ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return OwnerFieldsDto.class.equals(targetType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        OwnerFieldsDto owner = (OwnerFieldsDto) body;
        String line1 = owner.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String normalizedLine1 = normalize(line1);
            owner.setAddressLine1(normalizedLine1);
            String line2 = owner.getAddressLine2();
            boolean hasLine2 = line2 != null && !line2.isBlank();
            String normalizedLine2 = hasLine2 ? normalize(line2) : null;
            owner.setAddressLine2(normalizedLine2);
            owner.setAddress(hasLine2 ? normalizedLine1 + " " + normalizedLine2 : normalizedLine1);
        }
        else {
            owner.setAddress(normalize(owner.getAddress()));
        }
        return body;
    }

    /** Canonical address form: trimmed, whitespace-collapsed, upper-cased, abbreviations expanded. */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String upper = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(upper.length());
        for (String token : upper.split(" ")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }
}
