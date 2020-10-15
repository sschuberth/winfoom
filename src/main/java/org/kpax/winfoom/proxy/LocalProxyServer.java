/*
 *  Copyright (c) 2020. Eugen Covaci
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 */

package org.kpax.winfoom.proxy;

import org.kpax.winfoom.annotation.ThreadSafe;
import org.kpax.winfoom.config.ProxyConfig;
import org.kpax.winfoom.config.SystemConfig;
import org.kpax.winfoom.util.HttpUtils;
import org.kpax.winfoom.util.InputOutputs;
import org.kpax.winfoom.util.functional.Resetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * The local proxy server.
 * <p>Note: We rely on the Spring context to close this instance!
 *
 * @author Eugen Covaci
 */
@ThreadSafe
@Order(0)
@Component
class LocalProxyServer implements Resetable {

    private final Logger logger = LoggerFactory.getLogger(LocalProxyServer.class);

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private ProxyConfig proxyConfig;

    @Autowired
    private ProxyExecutorService executorService;

    @Autowired
    private ClientConnectionHandler clientConnectionHandler;

    private ServerSocket serverSocket;

    /**
     * Start the local proxy server.
     * <p>This means:
     * <ul>
     * <li>Opens a {@link ServerSocket} on a local port, then listen for connections</li>
     * <li>When a connection arrives, it delegates the handling to the {@link ClientConnectionHandler}, on a new
     * thread.</li>
     * </ul>
     *
     * @throws Exception
     */
    synchronized void start() throws Exception {
        logger.info("Start local proxy server with userConfig {}", proxyConfig);
        try {
            serverSocket = new ServerSocket(proxyConfig.getLocalPort(),
                    systemConfig.getServerSocketBacklog());
            executorService.submit(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        systemConfig.configureSocket(socket);
                        executorService.submit(() -> {
                            try {
                                clientConnectionHandler.handleConnection(socket);
                            } catch (Exception e) {
                                logger.debug("Error on handling connection", e);
                            } finally {
                                InputOutputs.close(socket);
                            }
                        });
                    } catch (SocketException e) {

                        // The ServerSocket has been closed, exit the while loop
                        if (HttpUtils.isSocketClosed(e)) {
                            break;
                        }

                        // Get connection interrupted error whenever stop the server socket,
                        // there is no reason to debug it
                        if (!HttpUtils.isConnectionInterrupted(e)) {
                            logger.debug("Socket error on getting connection", e);
                        }
                    } catch (Exception e) {
                        logger.debug("Generic error on getting connection", e);
                    }
                }
            });
            logger.info("Server started, listening on port: " + proxyConfig.getLocalPort());
        } catch (Exception e) {
            // Cleanup on exception
            close();
            throw e;
        }
    }

    @Override
    public synchronized void close() {
        logger.info("Close the local proxy server");
        try {
            InputOutputs.close(serverSocket);
        } catch (Exception e) {
            logger.warn("Error on closing server socket", e);
        }
    }

}
