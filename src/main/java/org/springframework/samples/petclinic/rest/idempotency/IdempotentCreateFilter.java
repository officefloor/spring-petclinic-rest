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

package org.springframework.samples.petclinic.rest.idempotency;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Makes owner creation idempotent: a {@code POST /api/owners} that repeats an already-seen
 * {@code Idempotency-Key} replays the first response (as {@code 200 OK}) instead of creating
 * a duplicate.
 */
@Component
public class IdempotentCreateFilter extends OncePerRequestFilter {

    private final Map<String, StoredResponse> responses = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = idempotencyKey(request);
        if (key == null) {
            chain.doFilter(request, response);
            return;
        }
        StoredResponse stored = responses.get(key);
        if (stored != null) {
            stored.replayTo(response);
            return;
        }
        ContentCachingResponseWrapper capture = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, capture);
        if (isSuccessful(capture.getStatus())) {
            responses.put(key, StoredResponse.capturedFrom(capture));
        }
        capture.copyBodyToResponse();
    }

    /** The create key when this is a {@code POST /api/owners} carrying one, else {@code null}. */
    private static String idempotencyKey(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())
                || !"/api/owners".equals(request.getRequestURI())) {
            return null;
        }
        return request.getHeader("Idempotency-Key");
    }

    private static boolean isSuccessful(int status) {
        return status >= 200 && status < 300;
    }
}
