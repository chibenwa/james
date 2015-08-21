/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.modules.server;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.james.container.spring.lifecycle.ConfigurationProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ClassPathConfigurationProvider implements ConfigurationProvider {

    private static final String CONFIGURATION_FILE_SUFFIX = ".xml";

    private final Map<String, HierarchicalConfiguration> configurations;

    public ClassPathConfigurationProvider() {
        this.configurations = new HashMap<>();
    }

    @Override
    public void registerConfiguration(String beanName, HierarchicalConfiguration conf) {
        configurations.put(beanName, conf);
    }

    @Override
    public HierarchicalConfiguration getConfiguration(String beanName) throws ConfigurationException {
        HierarchicalConfiguration conf = configurations.get(beanName);
        if (conf != null) {
            return conf;
        }
        conf = loadConfiguration(beanName);
        registerConfiguration(beanName, conf);
        return conf;
    }

    private HierarchicalConfiguration loadConfiguration(String beanName) throws ConfigurationException {
        int delimiterPosition = beanName.indexOf(".");
        try {
            HierarchicalConfiguration config = getConfig(retrieveConfigInputStream(beanName, delimiterPosition));
            return selectHierarchicalConfigPart(beanName, delimiterPosition, config);
        } catch (Exception e) {
            throw new ConfigurationException("Unable to load configuration for component " + beanName, e);
        }
    }

    private InputStream retrieveConfigInputStream(String beanName, int delimiterPosition) throws ConfigurationException {
        InputStream configStream = ClassLoader.getSystemResourceAsStream(computeResourceName(beanName, delimiterPosition) + CONFIGURATION_FILE_SUFFIX);
        if (configStream == null) {
            throw new ConfigurationException("Unable to locate configuration file " + computeResourceName(beanName, delimiterPosition) + CONFIGURATION_FILE_SUFFIX + " for component " + beanName);
        }
        return configStream;
    }

    private XMLConfiguration getConfig(InputStream configStream) throws ConfigurationException, IOException {
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.setAttributeSplittingDisabled(true);
        config.load(configStream);
        return config;
    }


    private HierarchicalConfiguration selectHierarchicalConfigPart(String beanName, int i, HierarchicalConfiguration config) {
        String configPart = computeConfigPart(beanName, i);
        if (configPart != null) {
            return config.configurationAt(configPart);
        } else {
            return config;
        }
    }


    private String computeConfigPart(String beanName, int i) {
        if (i > -1) {
            return beanName.substring(i + 1);
        }
        return null;
    }

    private String computeResourceName(String beanName, int i) {
        if (i >= 0) {
            return beanName.substring(0, i);
        } else {
            return beanName;
        }
    }
}
