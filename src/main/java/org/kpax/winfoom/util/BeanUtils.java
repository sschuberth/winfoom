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

package org.kpax.winfoom.util;

import org.apache.commons.beanutils.*;
import org.slf4j.*;

import java.lang.reflect.*;
import java.util.*;

public class BeanUtils {

    private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    public static void copyNonNullProperties(Object src, Object dest)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Map<String, Object> objectMap = PropertyUtils.describe(src);
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            if (!entry.getKey().equals("class") && entry.getValue() != null) {
                PropertyUtils.setProperty(dest, entry.getKey(), entry.getValue());
            }
        }
    }

}