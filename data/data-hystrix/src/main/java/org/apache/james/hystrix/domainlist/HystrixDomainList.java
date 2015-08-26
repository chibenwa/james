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

package org.apache.james.hystrix.domainlist;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.api.DomainListException;
import org.apache.james.hystrix.domainlist.commands.AddDomainCommand;
import org.apache.james.hystrix.domainlist.commands.ContainsDomainCommand;
import org.apache.james.hystrix.domainlist.commands.GetDomainsCommand;
import org.apache.james.hystrix.domainlist.commands.RemoveDomainCommand;

/**
 * Fallback policy is based on the default domain
 */
public class HystrixDomainList implements DomainList {

    private final DomainList wrappedDomainList;

    public HystrixDomainList(DomainList wrappedDomainList) {
        this.wrappedDomainList = wrappedDomainList;
    }

    @Override
    public String[] getDomains() throws DomainListException {
        try {
            return new GetDomainsCommand(wrappedDomainList).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public boolean containsDomain(String domain) throws DomainListException {
        try {
            return new ContainsDomainCommand(wrappedDomainList, domain).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void addDomain(String domain) throws DomainListException {
        try {
            new AddDomainCommand(wrappedDomainList, domain).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void removeDomain(String domain) throws DomainListException {
        try {
            new RemoveDomainCommand(wrappedDomainList, domain).execute();
        } catch(HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public String getDefaultDomain() throws DomainListException {
        return wrappedDomainList.getDefaultDomain();
    }

    private DomainListException unboxException(HystrixBadRequestException e) {
        if (e.getCause() instanceof DomainListException) {
            return  (DomainListException) e.getCause();
        }
        throw new RuntimeException("HystrixBadRequestException wasn't containing MailboxException", e);
    }
}
