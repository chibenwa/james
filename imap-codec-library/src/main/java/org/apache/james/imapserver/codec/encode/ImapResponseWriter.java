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

package org.apache.james.imapserver.codec.encode;

import java.io.IOException;

/**
 * <p>Writes IMAP response.</p>
 * <p>Factors out basic IMAP reponse writing operations 
 * from higher level ones.</p>
 */
public interface ImapResponseWriter {

    /**
     * Starts an untagged response.
     */
    void untagged() throws IOException;

    /**
     * Starts a tagged response.
     * @param tag the tag, not null
     */
    void tag(String tag) throws IOException;

    /**
     * Writes a command name.
     * @param commandName the command name, not null
     */
    void commandName( String commandName ) throws IOException;

    /**
     * Writes a message.
     * @param message the message, not null
     */
    void message( String message ) throws IOException;

    void message( long number ) throws IOException;
 
    /**
     * Writes a response code.
     * @param responseCode the response code, not null
     */
    void responseCode( String responseCode ) throws IOException;

    /**
     * Writes a quoted message.
     * @param message message, not null
     */
    void quote(String message) throws IOException;
    
    /**
     * Opens a parenthesis - writes a <code>(</code>.
     */
    void openParen() throws IOException;
    
    /**
     * Closes a parenthesis - writes a <code>)</code>.
     */
    void closeParen() throws IOException;

    /**
     * Ends a response.
     *
     */
    void end() throws IOException;
}
