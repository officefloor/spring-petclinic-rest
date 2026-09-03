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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Makes {@code POST /api/owners} idempotent per {@code Idempotency-Key} header. The first create
 * carrying a given key has its successful (201) response body remembered; a later create repeating
 * that key replays the originally created owner with 200 instead of creating a duplicate.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OwnerIdempotencyFilter extends OncePerRequestFilter {

    private final Map<String, byte[]> responses = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader("Idempotency-Key");
        if (key == null || !isCreateOwner(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        byte[] cached = responses.get(key);
        if (cached != null) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getOutputStream().write(cached);
            return;
        }
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);
        if (wrapper.getStatus() == HttpStatus.CREATED.value()) {
            responses.put(key, wrapper.getContentAsByteArray());
        }
        wrapper.copyBodyToResponse();
    }

    private boolean isCreateOwner(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
            && request.getRequestURI().endsWith("/api/owners");
    }
}
