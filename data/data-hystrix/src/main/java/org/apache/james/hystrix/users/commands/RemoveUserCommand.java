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

package org.apache.james.hystrix.users.commands;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.api.UsersRepositoryException;

public class RemoveUserCommand extends HystrixCommand<Void>{

    private final UsersRepository wrappedUsersRepository;
    private final String user;

    public RemoveUserCommand(UsersRepository usersRepository, String user) {
        super(HystrixCommandGroupKey.Factory.asKey("addUser"), HystrixThreadPoolKey.Factory.asKey("dataStorage"));
        this.user = user;
        this.wrappedUsersRepository = usersRepository;
    }

    @Override
    protected Void run() throws Exception {
        try {
            wrappedUsersRepository.removeUser(user);
            return null;
        } catch (UsersRepositoryException e) {
            throw new HystrixBadRequestException("Error while running UsersRepository", e);
        }
    }
}
