/*
 * Copyright (c) 2020. Eugen Covaci
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.kpax.winfoom.proxy;

import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.kpax.winfoom.annotation.*;
import org.kpax.winfoom.config.*;
import org.kpax.winfoom.exception.*;
import org.kpax.winfoom.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;

/**
 * Responsible with proxy config validation.
 */
@ThreadSafe
@Component
public class ProxyValidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProxyConfig proxyConfig;

    /**
     * Test the proxy settings by issuing a request through the proxy facade.
     *
     * @throws IOException
     * @throws InvalidProxySettingsException
     */
    public void testProxy() throws IOException, InvalidProxySettingsException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHost target = HttpHost.create(proxyConfig.getProxyTestUrl());
            HttpHost proxy = new HttpHost("localhost", proxyConfig.getLocalPort());
            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            HttpGet request = new HttpGet("/");
            request.setConfig(config);
            logger.info("Executing request " + request.getRequestLine() + " to " + target + " via " + proxy);
            try (CloseableHttpResponse response = httpClient.execute(target, request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                logger.info("statusCode={}", statusCode);
                if (statusCode > HttpUtils.MAX_HTTP_SUCCESS_CODE) {
                    if (statusCode == HttpStatus.SC_BAD_GATEWAY) {
                        throw new InvalidProxySettingsException("Wrong proxy host/port");
                    } else if (statusCode == HttpStatus.SC_NOT_FOUND || statusCode == HttpStatus.SC_GATEWAY_TIMEOUT) {
                        throw new InvalidProxySettingsException("Cannot connect to the provided test URL");
                    } else if (statusCode == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
                        throw new InvalidProxySettingsException("Wrong user/password");
                    } else {
                        throw new InvalidProxySettingsException("Validation failed with statusCode: " + statusCode +
                                " reason: " + response.getStatusLine().getReasonPhrase());
                    }
                }
            }
        }
    }

}
