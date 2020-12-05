/*
 * Copyright (c) 2020. Eugen Covaci
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.kpax.winfoom.starter;

import org.kpax.winfoom.util.*;
import org.kpax.winfoom.view.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

import java.awt.*;

@Profile("gui")
@Component
public class GuiStarter implements ApplicationListener<ApplicationReadyEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppFrame appFrame;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        appFrame.setLocationRelativeTo(null);
        logger.info("Launch the GUI");
        EventQueue.invokeLater(() -> {
            try {
                appFrame.activate();
            } catch (Exception e) {
                logger.error("GUI error", e);
                SwingUtils.showErrorMessage("Failed to load the graphical interface." +
                        "<br>Please check the application's log file.");
                System.exit(1);
            }
        });
    }
}