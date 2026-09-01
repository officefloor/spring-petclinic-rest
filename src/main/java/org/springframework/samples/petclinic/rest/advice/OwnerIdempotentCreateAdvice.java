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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Makes owner creation idempotent per {@code Idempotency-Key} header. The first create that
 * carries a key records key -> assigned owner id (response side); a later create repeating a
 * seen key is answered with the originally created owner and 200, short-circuiting before the
 * body is even read so none of the other create rules (including duplicate-identity 409) run.
 * Kept as its own small advice so the rule stays a self-contained unit.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerIdempotentCreateAdvice extends RequestBodyAdviceAdapter
        implements ResponseBodyAdvice<Object> {

    private static final String KEY_HEADER = "Idempotency-Key";

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    public OwnerIdempotentCreateAdvice(ClinicService clinicService, OwnerMapper ownerMapper) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
    }

    private static boolean isAddOwner(Method method) {
        return method != null && "addOwner".equals(method.getName());
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return OwnerFieldsDto.class.equals(targetType) && isAddOwner(methodParameter.getMethod());
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        String key = inputMessage.getHeaders().getFirst(KEY_HEADER);
        Integer ownerId = key == null ? null : keyToOwnerId.get(key);
        if (ownerId != null) {
            throw new IdempotentReplayException(ownerId);
        }
        return inputMessage;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return isAddOwner(returnType.getMethod());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String key = request.getHeaders().getFirst(KEY_HEADER);
        if (key != null && body instanceof OwnerDto dto && dto.getId() != null) {
            keyToOwnerId.putIfAbsent(key, dto.getId());
        }
        return body;
    }

    @ExceptionHandler(IdempotentReplayException.class)
    @ResponseBody
    public ResponseEntity<OwnerDto> handleReplay(IdempotentReplayException e) {
        Owner owner = clinicService.findOwnerById(e.ownerId);
        if (owner == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
    }

    /** Signals that a create repeats an already-seen key and must replay owner {@code ownerId}. */
    static class IdempotentReplayException extends RuntimeException {

        final Integer ownerId;

        IdempotentReplayException(Integer ownerId) {
            this.ownerId = ownerId;
        }
    }
}
