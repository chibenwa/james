/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.mailrepository;

import org.apache.avalon.cornerstone.services.store.StreamRepository;
import org.apache.james.core.MimeMessageSource;
import org.apache.james.util.JDBCUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class points to a specific message in a repository.  This will return an
 * InputStream to the JDBC field/record, possibly sequenced with the file stream.
 */
public class MimeMessageJDBCSource extends MimeMessageSource {
    private static final boolean DEEP_DEBUG = false;

    //Define how to get to the data
    JDBCMailRepository repository = null;
    String key = null;
    StreamRepository sr = null;

    String retrieveMessageBodySQL = null;
    String retrieveMessageBodySizeSQL = null;

    // The JDBCUtil helper class
    private static final JDBCUtil theJDBCUtil =
            new JDBCUtil() {
                protected void delegatedLog(String logString) {
                    // No logging available at this point in the code.
                    // Therefore this is a noop method.
                }
            };

    /**
     * Construct a MimeMessageSource based on a JDBC repository, a key, and a
     * stream repository (where we might store the message body)
     */
    public MimeMessageJDBCSource(JDBCMailRepository repository,
            String key, StreamRepository sr) throws IOException {
        if (repository == null) {
            throw new IOException("Repository is null");
        }
        if (key == null) {
            throw new IOException("Message name (key) was not defined");
        }
        this.repository = repository;
        this.key = key;
        this.sr = sr;

        retrieveMessageBodySQL =
            repository.sqlQueries.getSqlString("retrieveMessageBodySQL", true);
        // this is optional
        retrieveMessageBodySizeSQL =
            repository.sqlQueries.getSqlString("retrieveMessageBodySizeSQL");
    }

    public String getSourceId() {
        StringBuffer sourceIdBuffer =
            new StringBuffer(128)
                    .append(repository.repositoryName)
                    .append("/")
                    .append(key);
        return sourceIdBuffer.toString();
    }

    /**
     * Return the input stream to the database field and then the file stream.  This should
     * be smart enough to work even if the file does not exist.  This is to support
     * a repository with the entire message in the database, which is how James 1.2 worked.
     */
    public synchronized InputStream getInputStream() throws IOException {
        Connection conn = null;
        PreparedStatement retrieveMessageStream = null;
        ResultSet rsRetrieveMessageStream = null;
        try {
            conn = repository.getConnection();

            byte[] headers = null;

            long start = 0;
            if (DEEP_DEBUG) {
                start = System.currentTimeMillis();
                System.out.println("starting");
            }
            retrieveMessageStream = conn.prepareStatement(retrieveMessageBodySQL);
            retrieveMessageStream.setString(1, key);
            retrieveMessageStream.setString(2, repository.repositoryName);
            rsRetrieveMessageStream = retrieveMessageStream.executeQuery();

            if (!rsRetrieveMessageStream.next()) {
                throw new IOException("Could not find message");
            }

            headers = rsRetrieveMessageStream.getBytes(1);
            if (DEEP_DEBUG) {
                System.err.println("stopping");
                System.err.println(System.currentTimeMillis() - start);
            }

            InputStream in = new ByteArrayInputStream(headers);
            try {
                if (sr != null) {
                    in = new SequenceInputStream(in, sr.get(key));
                }
            } catch (Exception e) {
                //ignore this... either sr is null, or the file does not exist
                // or something else
            }
            return in;
        } catch (SQLException sqle) {
            throw new IOException(sqle.toString());
        } finally {
            theJDBCUtil.closeJDBCResultSet(rsRetrieveMessageStream);
            theJDBCUtil.closeJDBCStatement(retrieveMessageStream);
            theJDBCUtil.closeJDBCConnection(conn);
        }
    }

    /**
     * Runs a custom SQL statement to check the size of the message body
     */
    public synchronized long getMessageSize() throws IOException {
        if (retrieveMessageBodySizeSQL == null) {
            //There was no SQL statement for this repository... figure it out the hard way
            System.err.println("no SQL statement to find size");
            return super.getMessageSize();
        }
        Connection conn = null;
        PreparedStatement retrieveMessageSize = null;
        ResultSet rsRetrieveMessageSize = null;
        try {
            conn = repository.getConnection();

            retrieveMessageSize = conn.prepareStatement(retrieveMessageBodySizeSQL);
            retrieveMessageSize.setString(1, key);
            retrieveMessageSize.setString(2, repository.repositoryName);
            rsRetrieveMessageSize = retrieveMessageSize.executeQuery();

            if (!rsRetrieveMessageSize.next()) {
                throw new IOException("Could not find message");
            }

            long size = rsRetrieveMessageSize.getLong(1);

            InputStream in = null;
            try {
                if (sr != null) {
                    in = sr.get(key);
                    int len = 0;
                    byte[] block = new byte[1024];
                    while ((len = in.read(block)) > -1) {
                        size += len;
                    }
                }
            } catch (Exception e) {
                //ignore this... either sr is null, or the file does not exist
                // or something else
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ioe) {
                    // Ignored - no access to logger at this point in the code
                }
            }
            
            return size;
        } catch (SQLException sqle) {
            throw new IOException(sqle.toString());
        } finally {
            theJDBCUtil.closeJDBCResultSet(rsRetrieveMessageSize);
            theJDBCUtil.closeJDBCStatement(retrieveMessageSize);
            theJDBCUtil.closeJDBCConnection(conn);
        }
    }

    /**
     * Check to see whether this is the same repository and the same key
     */
    public boolean equals(Object obj) {
        if (obj instanceof MimeMessageJDBCSource) {
            // TODO: Figure out whether other instance variables should be part of
            // the equals equation
            MimeMessageJDBCSource source = (MimeMessageJDBCSource)obj;
            return ((source.key == key) || ((source.key != null) && source.key.equals(key))) &&
                   ((source.repository == repository) || ((source.repository != null) && source.repository.equals(repository)));
        }
        return false;
    }

    /**
     * Provide a hash code that is consistent with equals for this class
     *
     * @return the hash code
     */
     public int hashCode() {
        int result = 17;
        if (key != null) {
            result = 37 * key.hashCode();
        }
        if (repository != null) {
            result = 37 * repository.hashCode();
        }
        return result;
     }
}
