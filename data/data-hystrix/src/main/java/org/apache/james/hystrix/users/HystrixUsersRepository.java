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

package org.apache.james.hystrix.users;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.james.hystrix.users.commands.*;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.api.UsersRepositoryException;
import org.apache.james.user.api.model.User;

import java.util.Iterator;

public class HystrixUsersRepository implements UsersRepository {

    private final UsersRepository wrappedUsersRepository;

    public HystrixUsersRepository(UsersRepository wrappedUsersRepository) {
        this.wrappedUsersRepository = wrappedUsersRepository;
    }

    @Override
    public void addUser(String username, String password) throws UsersRepositoryException {
        try {
            new AddUserCommand(wrappedUsersRepository, username, password).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public User getUserByName(String name) throws UsersRepositoryException {
        try {
            return new GetUserByNameCommand(wrappedUsersRepository, name).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void updateUser(User user) throws UsersRepositoryException {
        try {
            new UpdateUserCommand(wrappedUsersRepository, user).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public void removeUser(String name) throws UsersRepositoryException {
        try {
            new RemoveUserCommand(wrappedUsersRepository, name).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public boolean contains(String name) throws UsersRepositoryException {
        try {
            return new ContainsCommand(wrappedUsersRepository, name).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public boolean test(String name, String password) throws UsersRepositoryException {
        try {
            return new TestCommand(wrappedUsersRepository, name, password).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public int countUsers() throws UsersRepositoryException {
        try {
            return new CountUsersCommand(wrappedUsersRepository).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public Iterator<String> list() throws UsersRepositoryException {
        try {
            return new ListCommand(wrappedUsersRepository).execute();
        } catch (HystrixBadRequestException e) {
            throw unboxException(e);
        }
    }

    @Override
    public boolean supportVirtualHosting() throws UsersRepositoryException {
        return wrappedUsersRepository.supportVirtualHosting();
    }

    private UsersRepositoryException unboxException(HystrixBadRequestException e) {
        if (e.getCause() instanceof UsersRepositoryException) {
            return  (UsersRepositoryException) e.getCause();
        }
        throw new RuntimeException("HystrixBadRequestException wasn't containing MailboxException", e);
    }
}
