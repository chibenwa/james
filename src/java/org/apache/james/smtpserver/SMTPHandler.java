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

import org.apache.avalon.cornerstone.services.connection.ConnectionHandler;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.james.Constants;
import org.apache.james.core.MailImpl;
import org.apache.james.util.CRLFTerminatedReader;
import org.apache.james.util.InternetPrintWriter;
import org.apache.james.util.watchdog.Watchdog;
import org.apache.james.util.watchdog.WatchdogTarget;
import org.apache.james.util.mail.dsn.DSNStatus;
import org.apache.mailet.Mail;
import org.apache.mailet.dates.RFC822DateFormat;

import javax.mail.MessagingException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Provides SMTP functionality by carrying out the server side of the SMTP
 * interaction.
 *
 * @version CVS $Revision$ $Date$
 */
public class SMTPHandler
    extends AbstractLogEnabled
    implements ConnectionHandler, Poolable {

    /**
     * The constants to indicate the current processing mode of the session
     */
    private final static byte COMMAND_MODE = 1;
    private final static byte RESPONSE_MODE = 2;
    private final static byte MESSAGE_RECEIVED_MODE = 3;
    private final static byte MESSAGE_ABORT_MODE = 4;

    /**
     * SMTP Server identification string used in SMTP headers
     */
    private final static String SOFTWARE_TYPE = "JAMES SMTP Server "
                                                 + Constants.SOFTWARE_VERSION;

    // Keys used to store/lookup data in the internal state hash map
    private final static String SENDER = "SENDER_ADDRESS";     // Sender's email address
    private final static String MESG_FAILED = "MESG_FAILED";   // Message failed flag
    private final static String RCPT_LIST = "RCPT_LIST";   // The message recipients

    /**
     * Static Random instance used to generate SMTP ids
     */
    private final static Random random = new Random();

    /**
     * Static RFC822DateFormat used to generate date headers
     */
    private final static RFC822DateFormat rfc822DateFormat = new RFC822DateFormat();

    /**
     * The name of the currently parsed command
     */
    String curCommandName =  null;
    
    /**
     * The value of the currently parsed command
     */
    String curCommandArgument =  null;
    
    /**
     * The SMTPHandlerChain object set by SMTPServer
     */
    SMTPHandlerChain handlerChain = null;
    
    /**
     * The per SMTPHandler Session object
     */
    private SMTPSession session = new SMTPSessionImpl();
    
    /**
     * The mode of the current session
     */
    private byte mode;
    
    /**
     * The MailImpl object set by the DATA command
     */
    private MailImpl mail = null;
    
    /**
     * The session termination status
     */
    private boolean sessionEnded = false;

    /**
     * The thread executing this handler
     */
    private Thread handlerThread;

    /**
     * The TCP/IP socket over which the SMTP
     * dialogue is occurring.
     */
    private Socket socket;

    /**
     * The incoming stream of bytes coming from the socket.
     */
    private InputStream in;

    /**
     * The writer to which outgoing messages are written.
     */
    private PrintWriter out;

    /**
     * A Reader wrapper for the incoming stream of bytes coming from the socket.
     */
    private CRLFTerminatedReader inReader;

    /**
     * The remote host name obtained by lookup on the socket.
     */
    private String remoteHost;

    /**
     * The remote IP address of the socket.
     */
    private String remoteIP;

    /**
     * The user name of the authenticated user associated with this SMTP transaction.
     */
    private String authenticatedUser;

    /**
     * whether or not authorization is required for this connection
     */
    private boolean authRequired;

    /**
     * whether or not this connection can relay without authentication
     */
    private boolean relayingAllowed;

    /**
     * TEMPORARY: is the sending address blocklisted
     */
    private boolean blocklisted;

    /**
     * The id associated with this particular SMTP interaction.
     */
    private String smtpID;

    /**
     * The per-service configuration data that applies to all handlers
     */
    private SMTPHandlerConfigurationData theConfigData;

    /**
     * The hash map that holds variables for the SMTP message transfer in progress.
     *
     * This hash map should only be used to store variable set in a particular
     * set of sequential MAIL-RCPT-DATA commands, as described in RFC 2821.  Per
     * connection values should be stored as member variables in this class.
     */
    private HashMap state = new HashMap();

    /**
     * The watchdog being used by this handler to deal with idle timeouts.
     */
    private Watchdog theWatchdog;

    /**
     * The watchdog target that idles out this handler.
     */
    private WatchdogTarget theWatchdogTarget = new SMTPWatchdogTarget();

    /**
     * The per-handler response buffer used to marshal responses.
     */
    private StringBuffer responseBuffer = new StringBuffer(256);

    /**
     * Set the configuration data for the handler
     *
     * @param theData the per-service configuration data for this handler
     */
    void setConfigurationData(SMTPHandlerConfigurationData theData) {
        theConfigData = theData;
    }

    /**
     * Set the Watchdog for use by this handler.
     *
     * @param theWatchdog the watchdog
     */
    void setWatchdog(Watchdog theWatchdog) {
        this.theWatchdog = theWatchdog;
    }

    /**
     * Gets the Watchdog Target that should be used by Watchdogs managing
     * this connection.
     *
     * @return the WatchdogTarget
     */
    WatchdogTarget getWatchdogTarget() {
        return theWatchdogTarget;
    }

    /**
     * Idle out this connection
     */
    void idleClose() {
        if (getLogger() != null) {
            getLogger().error("SMTP Connection has idled out.");
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            // ignored
        }

        synchronized (this) {
            // Interrupt the thread to recover from internal hangs
            if (handlerThread != null) {
                handlerThread.interrupt();
            }
        }
    }

    /**
     * @see org.apache.avalon.cornerstone.services.connection.ConnectionHandler#handleConnection(Socket)
     */
    public void handleConnection(Socket connection) throws IOException {

        try {
            this.socket = connection;
            synchronized (this) {
                handlerThread = Thread.currentThread();
            }
            in = new BufferedInputStream(socket.getInputStream(), 1024);
            // An ASCII encoding can be used because all transmissions other
            // that those in the DATA command are guaranteed
            // to be ASCII
            // inReader = new BufferedReader(new InputStreamReader(in, "ASCII"), 512);
            inReader = new CRLFTerminatedReader(in, "ASCII");
            remoteIP = socket.getInetAddress().getHostAddress();
            remoteHost = socket.getInetAddress().getHostName();
            smtpID = random.nextInt(1024) + "";
            relayingAllowed = theConfigData.isRelayingAllowed(remoteIP);
            authRequired = theConfigData.isAuthRequired(remoteIP);
            resetState();
        } catch (Exception e) {
            StringBuffer exceptionBuffer =
                new StringBuffer(256)
                    .append("Cannot open connection from ")
                    .append(remoteHost)
                    .append(" (")
                    .append(remoteIP)
                    .append("): ")
                    .append(e.getMessage());
            String exceptionString = exceptionBuffer.toString();
            getLogger().error(exceptionString, e );
            throw new RuntimeException(exceptionString);
        }

        if (getLogger().isInfoEnabled()) {
            StringBuffer infoBuffer =
                new StringBuffer(128)
                        .append("Connection from ")
                        .append(remoteHost)
                        .append(" (")
                        .append(remoteIP)
                        .append(")");
            getLogger().info(infoBuffer.toString());
        }

        try {

            out = new InternetPrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()), 1024), false);

            // Initially greet the connector
            // Format is:  Sat, 24 Jan 1998 13:16:09 -0500

            responseBuffer.append("220 ")
                          .append(theConfigData.getHelloName())
                          .append(" SMTP Server (")
                          .append(SOFTWARE_TYPE)
                          .append(") ready ")
                          .append(rfc822DateFormat.format(new Date()));
            String responseString = clearResponseBuffer();
            writeLoggedFlushedResponse(responseString);
            
            //the core in protocol handling logic
            //run all the connection handlers, if it fast fails, end the session
            //parse the command command, look up for the list command handlers
            //execute each of the command handlers. If any command handlers writes
            //response then, end the command handler processing and start parsing new command
            //Once the message is received, run all the message handlers
            //the message handlers can either terminate message or terminate session


            //Session started - RUN all connect handlers
            List connectHandlers = handlerChain.getConnectHandlers();
            if(connectHandlers != null) {
                int count = connectHandlers.size();
                for(int i = 0; i < count; i++) {
                    ((ConnectHandler)connectHandlers.get(i)).onConnect(session);
                    if(sessionEnded) {
                        break;
                    }
                }
            }

            theWatchdog.start();
            while(!sessionEnded) {
              //Reset the current command values
              curCommandName = null;
              curCommandArgument = null;
              mode = COMMAND_MODE;

              //parse the command
              String cmdString =  readCommandLine();
              if (cmdString == null) {
                  break;
              }
              int spaceIndex = cmdString.indexOf(" ");
              if (spaceIndex > 0) {
                  curCommandName = cmdString.substring(0, spaceIndex);
                  curCommandArgument = cmdString.substring(spaceIndex + 1);
              } else {
                  curCommandName = cmdString;
              }
              curCommandName = curCommandName.toUpperCase(Locale.US);

              //fetch the command handlers registered to the command
              List commandHandlers = handlerChain.getCommandHandlers(curCommandName);
              if(commandHandlers == null) {
                  //end the session
                  break;
              } else {
                  int count = commandHandlers.size();
                  for(int i = 0; i < count; i++) {
                      ((CommandHandler)commandHandlers.get(i)).onCommand(session);
                      theWatchdog.reset();
                      //if the response is received, stop processing of command handlers
                      if(mode != COMMAND_MODE) {
                          break;
                      }
                  }

              }

              //handle messages
              if(mode == MESSAGE_RECEIVED_MODE) {
                  getLogger().info("executing message handlers");
                  List messageHandlers = handlerChain.getMessageHandlers();
                  int count = messageHandlers.size();
                  for(int i =0; i < count; i++) {
                      ((MessageHandler)messageHandlers.get(i)).onMessage(session);
                      //if the response is received, stop processing of command handlers
                      if(mode == MESSAGE_ABORT_MODE) {
                          break;
                      }
                  }
              }

              //if the message is not aborted, then send Mail
              if(mode == MESSAGE_RECEIVED_MODE) {
                  getLogger().info("sending mail");
                  sendMail(mail);
              }

              //do the clean up
              if(mail != null) {
                  mail.dispose();
                  mail = null;
                  resetState();
              }

            }
            theWatchdog.stop();
            getLogger().debug("Closing socket.");
        } catch (SocketException se) {
            if (getLogger().isErrorEnabled()) {
                StringBuffer errorBuffer =
                    new StringBuffer(64)
                        .append("Socket to ")
                        .append(remoteHost)
                        .append(" (")
                        .append(remoteIP)
                        .append(") closed remotely.");
                getLogger().error(errorBuffer.toString(), se );
            }
        } catch ( InterruptedIOException iioe ) {
            if (getLogger().isErrorEnabled()) {
                StringBuffer errorBuffer =
                    new StringBuffer(64)
                        .append("Socket to ")
                        .append(remoteHost)
                        .append(" (")
                        .append(remoteIP)
                        .append(") timeout.");
                getLogger().error( errorBuffer.toString(), iioe );
            }
        } catch ( IOException ioe ) {
            if (getLogger().isErrorEnabled()) {
                StringBuffer errorBuffer =
                    new StringBuffer(256)
                            .append("Exception handling socket to ")
                            .append(remoteHost)
                            .append(" (")
                            .append(remoteIP)
                            .append(") : ")
                            .append(ioe.getMessage());
                getLogger().error( errorBuffer.toString(), ioe );
            }
        } catch (Exception e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error( "Exception opening socket: "
                                   + e.getMessage(), e );
            }
        } finally {
            resetHandler();
        }
    }

    /**
     * Resets the handler data to a basic state.
     */
    private void resetHandler() {
        resetState();

        clearResponseBuffer();
        in = null;
        inReader = null;
        out = null;
        remoteHost = null;
        remoteIP = null;
        authenticatedUser = null;
        smtpID = null;

        if (theWatchdog != null) {
            ContainerUtil.dispose(theWatchdog);
            theWatchdog = null;
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("Exception closing socket: "
                                  + e.getMessage());
            }
        } finally {
            socket = null;
        }

        synchronized (this) {
            handlerThread = null;
        }

    }

    /**
     * Clears the response buffer, returning the String of characters in the buffer.
     *
     * @return the data in the response buffer
     */
    private String clearResponseBuffer() {
        String responseString = responseBuffer.toString();
        responseBuffer.delete(0,responseBuffer.length());
        return responseString;
    }

    /**
     * This method logs at a "DEBUG" level the response string that
     * was sent to the SMTP client.  The method is provided largely
     * as syntactic sugar to neaten up the code base.  It is declared
     * private and final to encourage compiler inlining.
     *
     * @param responseString the response string sent to the client
     */
    private final void logResponseString(String responseString) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Sent: " + responseString);
        }
    }

    /**
     * Write and flush a response string.  The response is also logged.
     * Should be used for the last line of a multi-line response or
     * for a single line response.
     *
     * @param responseString the response string sent to the client
     */
    final void writeLoggedFlushedResponse(String responseString) {
        out.println(responseString);
        out.flush();
        logResponseString(responseString);
    }

    /**
     * Write a response string.  The response is also logged.
     * Used for multi-line responses.
     *
     * @param responseString the response string sent to the client
     */
    final void writeLoggedResponse(String responseString) {
        out.println(responseString);
        logResponseString(responseString);
    }

    /**
     * Reads a line of characters off the command line.
     *
     * @return the trimmed input line
     * @throws IOException if an exception is generated reading in the input characters
     */
    final String readCommandLine() throws IOException {
        for (;;) try {
            String commandLine = inReader.readLine();
            if (commandLine != null) {
                commandLine = commandLine.trim();
            }
            return commandLine;
        } catch (CRLFTerminatedReader.TerminationException te) {
            writeLoggedFlushedResponse("501 Syntax error at character position " + te.position() + ". CR and LF must be CRLF paired.  See RFC 2821 #2.7.1.");
        }
    }

    /**
     * Sets the user name associated with this SMTP interaction.
     *
     * @param userID the user name
     */
    private void setUser(String userID) {
        authenticatedUser = userID;
    }

    /**
     * Returns the user name associated with this SMTP interaction.
     *
     * @return the user name
     */
    private String getUser() {
        return authenticatedUser;
    }

    /**
     * Resets message-specific, but not authenticated user, state.
     *
     */
    private void resetState() {
        ArrayList recipients = (ArrayList)state.get(RCPT_LIST);
        if (recipients != null) {
            recipients.clear();
        }
        state.clear();
    }

    /**
     * A private inner class which serves as an adaptor
     * between the WatchdogTarget interface and this
     * handler class.
     */
    private class SMTPWatchdogTarget
        implements WatchdogTarget {

        /**
         * @see org.apache.james.util.watchdog.WatchdogTarget#execute()
         */
        public void execute() {
            SMTPHandler.this.idleClose();
        }
    }

   /**
     * Sets the SMTPHandlerChain
     *
     * @param handlerChain SMTPHandler object
     */
    public void setHandlerChain(SMTPHandlerChain handlerChain) {
        this.handlerChain = handlerChain;
    }

     /**
      * delivers the mail to the spool.
      *
      * @param Mail the mail object
      */
      public void sendMail(MailImpl mail) {
         String responseString = null;
         try {
             session.setMail(mail);
             theConfigData.getMailServer().sendMail(mail);
             Collection theRecipients = mail.getRecipients();
             String recipientString = "";
             if (theRecipients != null) {
                 recipientString = theRecipients.toString();
             }
             if (getLogger().isInfoEnabled()) {
                 StringBuffer infoBuffer =
                     new StringBuffer(256)
                         .append("Successfully spooled mail ")
                         .append(mail.getName())
                         .append(" from ")
                         .append(mail.getSender())
                         .append(" on ")
                         .append(remoteIP)
                         .append(" for ")
                         .append(recipientString);
                 getLogger().info(infoBuffer.toString());
             }
         } catch (MessagingException me) {
             // Grab any exception attached to this one.
             Exception e = me.getNextException();
             // If there was an attached exception, and it's a
             // MessageSizeException
             if (e != null && e instanceof MessageSizeException) {
                 // Add an item to the state to suppress
                 // logging of extra lines of data
                 // that are sent after the size limit has
                 // been hit.
                 state.put(MESG_FAILED, Boolean.TRUE);
                 // then let the client know that the size
                 // limit has been hit.
                 responseString = "552 "+DSNStatus.getStatus(DSNStatus.PERMANENT,DSNStatus.SYSTEM_MSG_TOO_BIG)+" Error processing message: "
                             + e.getMessage();
                 StringBuffer errorBuffer =
                     new StringBuffer(256)
                         .append("Rejected message from ")
                         .append(state.get(SENDER).toString())
                         .append(" from host ")
                         .append(remoteHost)
                         .append(" (")
                         .append(remoteIP)
                         .append(") exceeding system maximum message size of ")
                         .append(theConfigData.getMaxMessageSize());
                 getLogger().error(errorBuffer.toString());
             } else {
                 responseString = "451 "+DSNStatus.getStatus(DSNStatus.TRANSIENT,DSNStatus.UNDEFINED_STATUS)+" Error processing message: "
                             + me.getMessage();
                 getLogger().error("Unknown error occurred while processing DATA.", me);
             }
             session.writeResponse(responseString);
             return;
         }
         responseString = "250 "+DSNStatus.getStatus(DSNStatus.SUCCESS,DSNStatus.CONTENT_OTHER)+" Message received";
         session.writeResponse(responseString);
     }
  

    /**
     * The SMTPSession implementation data to be passed to each handler
     */
    private class SMTPSessionImpl implements SMTPSession {

        /**
         * @see org.apache.james.smtpserver.SMTPSession#writeResponse(String)
         */
        public void writeResponse(String respString) {
            SMTPHandler.this.writeLoggedFlushedResponse(respString);
            //TODO Explain this well
            if(SMTPHandler.this.mode == COMMAND_MODE) {
                SMTPHandler.this.mode = SMTPHandler.RESPONSE_MODE;
            }
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getCommandName()
         */
        public String getCommandName() {
            return SMTPHandler.this.curCommandName;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getCommandArgument()
         */
        public String getCommandArgument() {
            return SMTPHandler.this.curCommandArgument;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getMail()
         */
        public Mail getMail() {
            return SMTPHandler.this.mail;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#setMail(MailImpl)
         */
        public void setMail(MailImpl mail) {
            SMTPHandler.this.mail = mail;
            SMTPHandler.this.mode = SMTPHandler.MESSAGE_RECEIVED_MODE;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getRemoteHost()
         */
        public String getRemoteHost() {
            return SMTPHandler.this.remoteHost;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getRemoteIPAddress()
         */
        public String getRemoteIPAddress() {
            return SMTPHandler.this.remoteIP;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#endSession()
         */
        public void endSession() {
            SMTPHandler.this.sessionEnded = true;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#isSessionEnded()
         */
        public boolean isSessionEnded() {
            return SMTPHandler.this.sessionEnded;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#resetState()
         */
        public void resetState() {
            SMTPHandler.this.resetState();
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getState()
         */
        public HashMap getState() {
            return SMTPHandler.this.state;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getLogger()
         */
        public Logger getLogger() {
            return SMTPHandler.this.getLogger();
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getConfigurationData()
         */
        public SMTPHandlerConfigurationData getConfigurationData() {
            return SMTPHandler.this.theConfigData;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#isBlockListed()
         */
        public boolean isBlockListed() {
            return SMTPHandler.this.blocklisted;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#setBlockListed(boolean)
         */
        public void setBlockListed(boolean blocklisted ) {
            SMTPHandler.this.blocklisted = blocklisted;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#isRelayingAllowed()
         */
        public boolean isRelayingAllowed() {
            return SMTPHandler.this.relayingAllowed;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#isAuthRequired()
         */
        public boolean isAuthRequired() {
            return SMTPHandler.this.authRequired;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getUser()
         */
        public String getUser() {
            return SMTPHandler.this.getUser();
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#setUser()
         */
        public void setUser(String user) {
            SMTPHandler.this.setUser(user);
        }
  
        /**
         * @see org.apache.james.smtpserver.SMTPSession#getResponseBuffer()
         */
        public StringBuffer getResponseBuffer() {
            return SMTPHandler.this.responseBuffer;
        }
  
        /**
         * @see org.apache.james.smtpserver.SMTPSession#clearResponseBuffer()
         */
        public String clearResponseBuffer() {
            return SMTPHandler.this.clearResponseBuffer();
        }


        /**
         * @see org.apache.james.smtpserver.SMTPSession#readCommandLine()
         */
        public String readCommandLine() throws IOException {
            return SMTPHandler.this.readCommandLine();
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getWatchdog()
         */
        public Watchdog getWatchdog() {
            return SMTPHandler.this.theWatchdog;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getInputStream()
         */
        public InputStream getInputStream() {
            return SMTPHandler.this.in;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#getSessionID()
         */
        public String getSessionID() {
            return SMTPHandler.this.smtpID;
        }

        /**
         * @see org.apache.james.smtpserver.SMTPSession#abortMessage()
         */
        public void abortMessage() {
            SMTPHandler.this.mode = SMTPHandler.MESSAGE_ABORT_MODE;
        }

    }

}
