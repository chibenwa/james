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

package org.apache.james.hystrix.rrt.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.rrt.api.RecipientRewriteTable;
import org.apache.james.rrt.api.RecipientRewriteTableException;

public class AddAliasDomainMappingCommand extends HystrixCommand<Void>{

    private final RecipientRewriteTable wrappedRecipientRewriteTable;
    private final String aliasDomain;
    private final String realDomain;

    public AddAliasDomainMappingCommand(RecipientRewriteTable wrappedRecipientRewriteTable,
                                        String aliasDomain,
                                        String realDomain) {
        super(HystrixCommandGroupKey.Factory.asKey("addAliasMappings"), HystrixThreadPoolKey.Factory.asKey("dataStorage"));
        this.wrappedRecipientRewriteTable = wrappedRecipientRewriteTable;
        this.aliasDomain = aliasDomain;
        this.realDomain = realDomain;
    }

    @Override
    protected Void run() throws Exception {
        try {
            wrappedRecipientRewriteTable.addAliasDomainMapping(aliasDomain, realDomain);
            return null;
        } catch(RecipientRewriteTableException e) {
            throw new HystrixBadRequestException("Error while using domain list", e);
        }
    }
}
