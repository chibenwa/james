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

package org.apache.james.hystrix.rrt;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.hystrix.rrt.commands.*;
import org.apache.james.rrt.api.RecipientRewriteTable;
import org.apache.james.rrt.api.RecipientRewriteTableException;

import java.util.Collection;
import java.util.Map;

public class HystrixRecipientRewriteTable implements RecipientRewriteTable {

    private final RecipientRewriteTable recipientRewriteTable;

    public HystrixRecipientRewriteTable(RecipientRewriteTable recipientRewriteTable) {
        this.recipientRewriteTable = recipientRewriteTable;
    }

    @Override
    public Collection<String> getMappings(String user, String domain) throws ErrorMappingException, RecipientRewriteTableException {
        try {
            return new GetMappingsCommand(recipientRewriteTable, user, domain).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void addRegexMapping(String user, String domain, String regex) throws RecipientRewriteTableException {
        try {
            new AddRegexMappingCommand(recipientRewriteTable, user, domain, regex).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void removeRegexMapping(String user, String domain, String regex) throws RecipientRewriteTableException {
        try {
            new RemoveRegexMappingCommand(recipientRewriteTable, user, domain, regex).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void addAddressMapping(String user, String domain, String address) throws RecipientRewriteTableException {
        try {
            new AddAddressMappingCommand(recipientRewriteTable, user, domain, address).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void removeAddressMapping(String user, String domain, String address) throws RecipientRewriteTableException {
        try {
            new RemoveAddressMappingCommand(recipientRewriteTable, user, domain, address).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void addErrorMapping(String user, String domain, String error) throws RecipientRewriteTableException {
        try {
            new AddErrorMappingCommand(recipientRewriteTable, user, domain, error).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void removeErrorMapping(String user, String domain, String error) throws RecipientRewriteTableException {
        try {
            new RemoveErrorMappingCommand(recipientRewriteTable, user, domain, error).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public Collection<String> getUserDomainMappings(String user, String domain) throws RecipientRewriteTableException {
        try {
            return new GetUserDomainMappingsCommand(recipientRewriteTable, user, domain).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void addMapping(String user, String domain, String mapping) throws RecipientRewriteTableException {
        try {
            new AddMappingCommand(recipientRewriteTable, user, domain, mapping).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void removeMapping(String user, String domain, String mapping) throws RecipientRewriteTableException {
        try {
            new RemoveMappingComand(recipientRewriteTable, user, domain, mapping).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public Map<String, Collection<String>> getAllMappings() throws RecipientRewriteTableException {
        try {
            return new GetAllMappingsCommand(recipientRewriteTable).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void addAliasDomainMapping(String aliasDomain, String realDomain) throws RecipientRewriteTableException {
        try {
            new AddAliasDomainMappingCommand(recipientRewriteTable, aliasDomain, realDomain).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void removeAliasDomainMapping(String aliasDomain, String realDomain) throws RecipientRewriteTableException {
        try {
            new RemoveAliasMappingCommand(recipientRewriteTable, aliasDomain, realDomain).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    private RecipientRewriteTableException unboxException(HystrixBadRequestException e) {
        if (e.getCause() instanceof RecipientRewriteTableException) {
            return  (RecipientRewriteTableException) e.getCause();
        }
        throw new RuntimeException("HystrixBadRequestException wasn't containing MailboxException", e);
    }
}
