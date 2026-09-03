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

import java.util.Locale;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Flags every owner response with {@code capacityWarning}: true when the owner's city (compared
 * case-insensitively with surrounding whitespace trimmed) already holds between 40 and 49 owners,
 * approaching the 50-owner hard limit enforced by {@link OwnerCityCapacityAdvice}; otherwise false.
 * Purely informational, so it never rejects a request.
 */
@ControllerAdvice
public class OwnerCapacityWarningAdvice implements ResponseBodyAdvice<Object> {

    private static final int WARN_FROM = 40;

    private static final int WARN_TO = 49;

    private final ClinicService clinicService;

    public OwnerCapacityWarningAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof OwnerDto owner) {
            int count = cityCount(normalize(owner.getCity()));
            owner.setCapacityWarning(count >= WARN_FROM && count <= WARN_TO);
        }
        return body;
    }

    private int cityCount(String city) {
        if (city.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        return count;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
