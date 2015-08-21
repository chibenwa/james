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

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.james.lifecycle.api.Configurable;
import org.apache.james.lifecycle.api.LogEnabled;
import org.apache.james.mailrepository.api.MailRepository;
import org.apache.james.mailrepository.api.MailRepositoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JavaMailRepositoryStore implements MailRepositoryStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaMailRepositoryStore.class);

    private Map<String, MailRepository> repositories;
    private Map<String, String> classes;
    private Map<String, HierarchicalConfiguration> defaultConfigs;
    private HierarchicalConfiguration configuration;

    public JavaMailRepositoryStore() {
        classes = new ConcurrentHashMap<>();
        defaultConfigs = new ConcurrentHashMap<>();
        repositories = new ConcurrentHashMap<>();
    }

    public List<String> getUrls() {
        return repositories.keySet().stream().collect(Collectors.toList());
    }

    public void configure(HierarchicalConfiguration configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    public void init() throws Exception {
        LOGGER.info("JamesMailStore init... " + this);
        List<HierarchicalConfiguration> registeredClasses = configuration.configurationsAt("mailrepositories.mailrepository");
        for (HierarchicalConfiguration registeredClass : registeredClasses) {
            registerRepository(registeredClass);
        }
    }

    private void registerRepository(HierarchicalConfiguration repositoryConfiguration) throws ConfigurationException {
        String className = repositoryConfiguration.getString("[@class]");
        for (String protocol : repositoryConfiguration.getStringArray("protocols.protocol")) {
            registerClass(className, protocol);
            registerRepositoryDefaultConfiguration(repositoryConfiguration, protocol);
        }
    }

    public synchronized MailRepository select(String destination) throws MailRepositoryStoreException {
        MailRepository repository = repositories.get(destination);
        if (repository != null) {
            return repository;
        }
        String protocol = retrieveProtocol(destination);
        repository = initialiseNewRepository(classes.get(protocol), createRepositoryCombinedConfig(destination, protocol));
        repositories.put(destination, repository);
        return repository;
    }

    private void registerRepositoryDefaultConfiguration(HierarchicalConfiguration repositoryConfiguration, String protocol) {
        HierarchicalConfiguration defConf = null;
        if (repositoryConfiguration.getKeys("config").hasNext()) {
            defConf = repositoryConfiguration.configurationAt("config");
        }
        if (defConf != null) {
            defaultConfigs.put(protocol, defConf);
        }
    }

    private void registerClass(String className, String protocol) throws ConfigurationException {
        if (classes.get(protocol) != null) {
            throw new ConfigurationException("The combination of protocol and type comprise a unique key for repositories. This constraint has been violated. Please check your repository configuration.");
        }
        LOGGER.info("Registering Repository instance of class {} to handle {} protocol request for repositories with key {}", className, protocol, protocol);
        classes.put(protocol, className);
    }

    private CombinedConfiguration createRepositoryCombinedConfig(String destination, String protocol) {
        final CombinedConfiguration config = new CombinedConfiguration();
        HierarchicalConfiguration defaultProtocolConfig = defaultConfigs.get(protocol);
        if (defaultProtocolConfig != null) {
            config.addConfiguration(defaultProtocolConfig);
        }
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        builder.addProperty("[@destinationURL]", destination);
        config.addConfiguration(builder);
        return config;
    }

    private MailRepository initialiseNewRepository(String repositoryClassString, CombinedConfiguration config) throws MailRepositoryStoreException {
        try {
            Class<MailRepository> repositoryClass = (Class<MailRepository>) ClassLoader.getSystemClassLoader().loadClass(repositoryClassString);
            MailRepository repository = repositoryClass.newInstance();
            if (repository instanceof LogEnabled) {
                ((LogEnabled) repository).setLog(LOGGER);
            }
            if (repository instanceof Configurable) {
                ((Configurable) repository).configure(config);
            }
            return repository;
        } catch (Exception e) {
            throw new MailRepositoryStoreException("Cannot find or init repository", e);
        }
    }

    private String retrieveProtocol(String destination) throws MailRepositoryStoreException {
        int protocolSeparatorPosition = destination.indexOf(':');
        if (protocolSeparatorPosition == -1) {
            throw new MailRepositoryStoreException("Destination is malformed. Must be a valid URL: " + destination);
        }
        return destination.substring(0, protocolSeparatorPosition);
    }

}
