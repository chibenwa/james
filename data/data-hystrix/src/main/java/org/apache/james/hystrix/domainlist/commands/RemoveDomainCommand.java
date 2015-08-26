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

public class RemoveDomainCommand extends HystrixCommand<Void> {

    private final DomainList wrappedDomainList;
    private final String domain;

    public RemoveDomainCommand(DomainList wrappedDomainList, String domain) {
        super(HystrixCommandGroupKey.Factory.asKey("removeDomains"), HystrixThreadPoolKey.Factory.asKey("dataStorage"));
        this.wrappedDomainList = wrappedDomainList;
        this.domain = domain;
    }

    @Override
    protected Void run() throws Exception {
        try {
            wrappedDomainList.removeDomain(domain);
            return null;
        } catch(DomainListException e) {
            throw new HystrixBadRequestException("Error while using domain list", e);
        }
    }
}
