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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.domainlist.jpa.JPADomainList;
import org.apache.james.rrt.api.RecipientRewriteTable;
import org.apache.james.rrt.jpa.JPARecipientRewriteTable;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.jpa.JPAUsersRepository;

public class JpaDataServerModule extends AbstractModule {

    @Override
    protected void configure() {
        createRrt();
        createDomainList();
        createUserRepository();
    }

    private void createRrt() {
        RecipientRewriteTable recipientRewriteTable = new JPARecipientRewriteTable();
        bind(RecipientRewriteTable.class).toInstance(recipientRewriteTable);
        bind(RecipientRewriteTable.class).annotatedWith(Names.named("recipientrewritetable")).toInstance(recipientRewriteTable);
    }

    private void createDomainList() {
        JPADomainList jpaDomainList = new JPADomainList();
        bind(DomainList.class).toInstance(jpaDomainList);
        bind(DomainList.class).annotatedWith(Names.named("domainlist")).toInstance(jpaDomainList);
    }

    private void createUserRepository() {
        JPAUsersRepository jpaUsersRepository = new JPAUsersRepository();
        bind(UsersRepository.class).toInstance(jpaUsersRepository);
        bind(UsersRepository.class).annotatedWith(Names.named("usersrepository")).toInstance(jpaUsersRepository);
    }
}
