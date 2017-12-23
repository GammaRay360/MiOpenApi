package com.github.gammaray360.MiOpenApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by junk_ on 06/11/2017.
 */
class MiSocket {
    private static final int MAX_PACKET_SIZE = 65535; // bytes
    private final int mPort;

    private final InetAddress mIp;
    private final DatagramSocket mDatagramSocket;
    private final Object lockReceive = new Object();
    private final Object lockSend = new Object();
    private final DatagramPacket mIncomingDatagramPacket; // to use for recycle
    private final DatagramPacket mOutgoingDatagramPacket; // to use for recycle
    private final DatagramPacket mHelloDatagramPacket; // to use for recycle
    public MiSocket(InetAddress ip, int port) throws SocketException {
        mPort = port;
        mIp = ip;
        mDatagramSocket = new DatagramSocket();
        mIncomingDatagramPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE],MAX_PACKET_SIZE,mIp,mPort);
        mOutgoingDatagramPacket =  new DatagramPacket(new byte[MAX_PACKET_SIZE],MAX_PACKET_SIZE,mIp,mPort);
        mHelloDatagramPacket = new DatagramPacket(new byte[MiPacket.HEADER_SIZE],MiPacket.HEADER_SIZE,mIp,mPort);
        mHelloDatagramPacket.setData(MiPacket.HELLO_PACKET_BYTES);
    }
    public void receive(MiPacket packet) throws IOException {
        synchronized (lockReceive) {
            mDatagramSocket.receive(mIncomingDatagramPacket);
            packet.setData(mIncomingDatagramPacket.getData(), mIncomingDatagramPacket.getLength());
        }
    }
    public void send(MiPacket packet) throws IOException {
        synchronized (lockSend) {
            mOutgoingDatagramPacket.setData(packet.getData());
            mDatagramSocket.send(mOutgoingDatagramPacket);
        }
    }
    public void sync() throws IOException {
        mDatagramSocket.send(mHelloDatagramPacket);
    }

    public void close(){
        mDatagramSocket.close();
    }
}
