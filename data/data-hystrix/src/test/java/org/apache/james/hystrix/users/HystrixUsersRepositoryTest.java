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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.api.UsersRepositoryException;
import org.apache.james.user.api.model.User;
import org.junit.Before;
import org.junit.Test;

public class HystrixUsersRepositoryTest {

    private static final String PASSWORD = "password";
    private static final String NAME = "name";

    private UsersRepository mockedUsersRepository;
    private UsersRepository testee;

    @Before
    public void setUp() throws Exception {
        mockedUsersRepository = mock(UsersRepository.class);
        testee = new HystrixUsersRepository(mockedUsersRepository);
    }

    @Test
    public void countUsersShouldCallUnderlyingUsersRepository() throws Exception {
        testee.countUsers();
        verify(mockedUsersRepository).countUsers();
    }

    @Test
    public void testShouldCallUnderlyingUsersRepository() throws Exception {
        testee.test(NAME, PASSWORD);
        verify(mockedUsersRepository).test(NAME, PASSWORD);
    }

    @Test
    public void containsShouldCallUnderlyingUsersRepository() throws Exception {
        testee.contains(NAME);
        verify(mockedUsersRepository).contains(NAME);
    }

    @Test
    public void getUserByNameShouldCallUnderlyingUsersRepository() throws Exception {
        testee.getUserByName(NAME);
        verify(mockedUsersRepository).getUserByName(NAME);
    }

    @Test
    public void removeUserShouldCallUnderlyingUsersRepository() throws Exception {
        testee.removeUser(NAME);
        verify(mockedUsersRepository).removeUser(NAME);
    }

    @Test
    public void listShouldCallUnderlyingUsersRepository() throws Exception {
        testee.list();
        verify(mockedUsersRepository).list();
    }

    @Test
    public void updateUserShouldCallUnderlyingUsersRepository() throws Exception {
        User user = new User() {
            @Override
            public String getUserName() {
                return NAME;
            }

            @Override
            public boolean verifyPassword(String pass) {
                return false;
            }

            @Override
            public boolean setPassword(String newPass) {
                return false;
            }
        };
        testee.updateUser(user);
        verify(mockedUsersRepository).updateUser(user);
    }

    @Test
    public void supportVirtualHostingShouldCallUnderlyingUsersRepository() throws Exception {
        testee.supportVirtualHosting();
        verify(mockedUsersRepository).supportVirtualHosting();
    }

    @Test
    public void addUserShouldCallUnderlyingUsersRepository() throws Exception {
        testee.addUser(NAME, PASSWORD);
        verify(mockedUsersRepository).addUser(NAME, PASSWORD);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = UsersRepositoryException.class)
    public void userRepositoryExceptionShouldBePropagated() throws Exception {
        when(mockedUsersRepository.countUsers()).thenThrow(UsersRepositoryException.class);
        testee.countUsers();
    }

}
