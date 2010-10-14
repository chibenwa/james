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
package org.apache.james.util.inetnetwork.model;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.james.util.inetnetwork.InetNetworkBuilder;

/**
 * 
 * 
 */
public class Inet4Network implements InetNetwork {

    /**
     * The IP address on which a subnet mask is applied.
     */
    private InetAddress network;

    /**
     * The subnet mask to apply on the IP address.
     */
    private InetAddress netmask;
    
    /**
     * You need a IP address and an subnetmask to construct an Inet4Network.<br/>
     * Both constructor parameters are passed via a InetAddress.
     * 
     * @param ip the InetAddress to init the class
     * @param netmask the InetAddress represent the netmask to init the class
     */
    public Inet4Network(InetAddress ip, InetAddress netmask) {
        network = maskIP(ip, netmask);
        this.netmask = netmask;
    }

    /* (non-Javadoc)
     * @see org.apache.james.api.dnsservice.model.InetNetwork#contains(java.net.InetAddress)
     */
    public boolean contains(final InetAddress ip) {
        if (InetNetworkBuilder.isV6(ip.getHostAddress())) {
            return false;
        }
        try {
            return network.equals(maskIP(ip, netmask));
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return network.getHostAddress() + "/" + netmask.getHostAddress();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return maskIP(network, netmask).hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return (obj != null)
                && (obj instanceof InetNetwork)
                && ((((Inet4Network) obj).network.equals(network)) && (((Inet4Network) obj).netmask.equals(netmask)));
    }
    /**
     * @see #maskIP(byte[], byte[])
     */
    private static InetAddress maskIP(final InetAddress ip, final InetAddress mask) {
        return maskIP(ip.getAddress(), mask.getAddress());
    }

    /**
     * Return InetAddress generated of the passed arguments. 
     * Return Null if any error occurs
     * 
     * @param ip the byte[] represent the ip
     * @param mask the byte[] represent the netmask
     * @return inetAddress the InetAddress generated of the passed arguments.
     */
    private static InetAddress maskIP(final byte[] ip, final byte[] mask) {
        if (ip.length != mask.length) {
            throw new IllegalArgumentException("IP address and mask must be of the same length.");
        }
        if (ip.length != 4) {
            throw new IllegalArgumentException("IP address and mask length must be equal to 4.");
        }
        try {
            byte[] maskedIp = new byte[ip.length];
            for (int i=0; i < ip.length; i++) {
                maskedIp[i] = (byte) (ip[i] & mask[i]);
            }
            return getByAddress(maskedIp);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Return InetAddress which represent the given byte[]
     * 
     * @param ip the byte[] represent the ip
     * @return ip the InetAddress generated of the given byte[]
     * @throws java.net.UnknownHostException
     */
    private static InetAddress getByAddress(byte[] ip) throws UnknownHostException {

        InetAddress addr = null;
        
        addr = Inet4Address.getByAddress(ip);

        if (addr == null) {
            addr = InetAddress.getByName(
                    Integer.toString(ip[0] & 0xFF, 10) + "." 
                    + Integer.toString(ip[1] & 0xFF, 10) + "."
                    + Integer.toString(ip[2] & 0xFF, 10) + "."
                    + Integer.toString(ip[3] & 0xFF, 10));
        }
        
        return addr;
    
    }

}
