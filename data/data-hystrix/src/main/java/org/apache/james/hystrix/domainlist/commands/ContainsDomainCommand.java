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

package org.apache.james.hystrix.domainlist.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.api.DomainListException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainsDomainCommand extends HystrixCommand<Boolean>{

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainsDomainCommand.class);

    private final DomainList wrappedDomainList;
    private final String domain;

    public ContainsDomainCommand(DomainList wrappedDomainList, String domain) {
        super(HystrixCommandGroupKey.Factory.asKey("containsDomains"), HystrixThreadPoolKey.Factory.asKey("dataStorage"));
        this.wrappedDomainList = wrappedDomainList;
        this.domain = domain;
    }

    @Override
    protected Boolean run() throws Exception {
        try {
            return wrappedDomainList.containsDomain(domain);
        } catch(DomainListException e) {
            throw new HystrixBadRequestException("Error while using domain list", e);
        }
    }

    @Override
    protected Boolean getFallback() {
        try {
            LOGGER.warn("Domainlist fallback : serving default domain");
            return wrappedDomainList.getDefaultDomain().equals(domain);
        } catch (DomainListException e) {
            LOGGER.error("Error while getting default domain on fallback", e);
            throw new HystrixBadRequestException("Error in fallback", e);
        }
    }
}
