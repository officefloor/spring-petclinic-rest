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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.util.Households;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Rejects a create-owner request whose whole identityKey
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}) equals an
 * existing owner's, responding 409 Conflict. This single derived key subsumes the former separate
 * telephone, email and household duplicate checks: because the telephone is part of the key, two
 * members of the same household with different telephones have different keys and are both allowed;
 * only an exact full-key match is a duplicate. The householdId is now derived deterministically
 * from (normalizedLastName, postcode), so owners sharing both are the same household; a second such
 * owner is rejected here as a household duplicate unless it sets {@code sharesHousehold}, which only
 * bypasses that block. The body is buffered and re-supplied downstream with the householdId filled in.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerUniqueIdentityAdvice implements RequestBodyAdvice {

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final ObjectMapper objectMapper;

    public OwnerUniqueIdentityAdvice(ClinicService clinicService, OwnerMapper ownerMapper, ObjectMapper objectMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = methodParameter.getMethod();
        return OwnerFieldsDto.class.equals(targetType) && method != null
            && "addOwner".equals(method.getName());
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {
        byte[] bytes = inputMessage.getBody().readAllBytes();
        JsonNode body = objectMapper.readTree(bytes);
        String householdId = Households.id(body.path("lastName").asString(""), body.path("postcode").asString(""));
        ((ObjectNode) body).put("householdId", householdId);
        if (!body.path("sharesHousehold").asBoolean(false)
            && Households.isDuplicate(clinicService.findAllOwners(), householdId)) {
            throw new DuplicateIdentityException();
        }
        return buffered(objectMapper.writeValueAsBytes(body), inputMessage.getHeaders());
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        String identityKey = ownerMapper.identityKey(ownerMapper.toOwner((OwnerFieldsDto) body));
        for (Owner existing : clinicService.findAllOwners()) {
            if (identityKey.equals(ownerMapper.identityKey(existing))) {
                throw new DuplicateIdentityException();
            }
        }
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    private HttpInputMessage buffered(byte[] bytes, HttpHeaders headers) {
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }

    @ExceptionHandler(DuplicateIdentityException.class)
    @ResponseBody
    public ResponseEntity<Void> handleDuplicateIdentity(DuplicateIdentityException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    /** Signals that an owner with the same whole identityKey already exists. */
    static class DuplicateIdentityException extends RuntimeException {
    }
}
