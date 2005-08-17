/***********************************************************************
 * Copyright (c) 1999-2005 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.james.smtpserver;


import org.apache.mailet.Mail;
import org.apache.james.core.MailImpl;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import org.apache.james.util.watchdog.Watchdog;
import org.apache.avalon.framework.logger.Logger;

/**
 * All the handlers access this interface to communicate with
 * SMTPHandler object
 */

public interface SMTPSession {

    /**
     * Writes response string to the client
     *
     * @param respString String that needs to send to the client
     */
    void writeResponse(String respString);

    /**
     * Returns the next command string sent by the client
     *
     * @return reads the characters of the commandline
     */
    String readCommandLine() throws IOException;

    /**
     * Returns Logger object
     *
     * @return Logger object
     */
    Logger getLogger();

    /**
     * Returns ResponseBuffer, this optimizes the unecessary creation of resources
     * by each handler object
     *
     * @return responseBuffer
     */
    StringBuffer getResponseBuffer();

    /**
     * Returns response string and clears the response buffer
     *
     * @return response string
     */
    String clearResponseBuffer();

    /**
     * Returns Inputstream for handling messages and commands
     *
     * @return InputStream object
     */
    InputStream getInputStream();

    /**
     * Returns currently process command name
     *
     * @return current command name
     */
    String getCommandName();

    /**
     * Returns currently process command argument
     *
     * @return current command argument
     */
    String getCommandArgument();

    /**
     * Returns Mail object for message handlers to process
     *
     * @return Mail object
     */
    Mail getMail();

    /**
     * Sets the MailImpl object for further processing
     *
     * @param mail MailImpl object
     */
    void setMail(MailImpl mail);

    /**
     * Returns host name of the client
     *
     * @return hostname of the client
     */
    String getRemoteHost();

    /**
     * Returns host ip address of the client
     *
     * @return host ip address of the client
     */
    String getRemoteIPAddress();

    /**
     * this makes the message to be dropped inprotocol
     *
     */
    void abortMessage();

    /**
     * this makes the session to close
     *
     */
    void endSession();

    /**
     * Returns the session status
     *
     * @return if the session is open or closed
     */
    boolean isSessionEnded();

    /**
     * Returns Map that consists of the state of the SMTPSession
     *
     * @return map of the current SMTPSession state
     */
    HashMap getState();

    /**
     * Resets the state
     *
     */
    void resetState();

    /**
     * Returns SMTPHandler service wide configuration
     *
     * @return SMTPHandlerConfigurationData
     */
    SMTPHandlerConfigurationData getConfigurationData();

    /**
     * Sets the blocklisted value
     *
     * @param blocklisted
     */
    void setBlockListed(boolean blocklisted);

    /**
     * Returns the blocklisted status
     *
     * @return blocklisted
     */
    boolean isBlockListed();

    /**
     * Returns whether Relaying is allowed or not
     *
     * @return the relaying status
     */
    boolean isRelayingAllowed();

    /**
     * Returns whether Authentication is required or not
     *
     * @return authentication required or not
     */
    boolean isAuthRequired();

    /**
     * Returns currently authenticated user
     *
     * @return the authenticated user name
     */
    String getUser();

    /**
     * set the user name after successful authentication
     *
     * @param the authenticated user name
     */
    void setUser(String user);

    /**
     * Returns Watchdog object used for handling timeout
     *
     * @return Watchdog object
     */
    Watchdog getWatchdog();

    /**
     * Returns the SMTP session id
     *
     * @return SMTP session id
     */
    String getSessionID();

}

