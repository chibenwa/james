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

import com.google.common.collect.Maps;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.rrt.api.RecipientRewriteTable;
import org.apache.james.rrt.api.RecipientRewriteTableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class GetAllMappingsCommand extends HystrixCommand<Map<String, Collection<String>>>{

    private final static Logger LOGGER = LoggerFactory.getLogger(GetAllMappingsCommand.class);

    private final RecipientRewriteTable wrappedRecipientRewriteTable;

    public GetAllMappingsCommand(RecipientRewriteTable wrappedRecipientRewriteTable) {
        super(HystrixCommandGroupKey.Factory.asKey("getAllMappings"), HystrixThreadPoolKey.Factory.asKey("dataStorage"));
        this.wrappedRecipientRewriteTable = wrappedRecipientRewriteTable;
    }

    @Override
    protected Map<String, Collection<String>> run() throws Exception {
        try {
            return wrappedRecipientRewriteTable.getAllMappings();
        } catch(RecipientRewriteTableException e) {
            throw new HystrixBadRequestException("Error while using domain list", e);
        }
    }

    @Override
    protected Map<String, Collection<String>> getFallback() {
        LOGGER.warn("Failed to load All mappings from backend. Serving empty mappings instead.");
        return Maps.newHashMap();
    }
}
