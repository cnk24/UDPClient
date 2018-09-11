package com.cnk24.udpclient;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPClientUtil {

    /**
     * Shared Instance
     */
    private static UDPClientUtil __sharedUDPClient = null;

    /**
     * UDPClientUtil Get Shared Instance
     * @return
     */
    public static UDPClientUtil getInstance() {
        if ( __sharedUDPClient == null ) {
            __sharedUDPClient = new UDPClientUtil();
        }
        return __sharedUDPClient;
    }

    /**
     * UDP 연결 시작
     */
    private boolean mIsStart = false;

    /**
     * UDP Connect / Receiver Thread
     */
    private UDPConnector mUdpConnectorThread = null;

    /**
     * UDP Socket
     */
    DatagramSocket mUDPSocket = null;

    /**
     * Constructor
     */
    private UDPClientUtil() {
        super();
    }

    /**
     * UDP 연결
     * @param port
     */
    public void connectUdpAddressAndPort(int port) {
        if ( mIsStart == false ) {
            mIsStart = true;
            mUdpConnectorThread = new UDPConnector(port);
            Thread connector = new Thread(mUdpConnectorThread);
            connector.start();
        }
    }

    /**
     * 메세지 보내기
     * @param msg
     */
    public void sendMessage(String msg) {
        if ( msg != null ) {
            UDPSendPacket sendPacket = new UDPSendPacket(mUDPSocket, msg.getBytes());
            sendPacket.run();
        }
    }

    /**
     * Stop UDP
     */
    public void stopUdp() {
        if ( mUdpConnectorThread != null ) {
            mUdpConnectorThread.udpStop();
        }
    }

    /**
     * UDP Connect And Receive Packet Thread
     */
    private class UDPConnector extends Thread {
        /**
         * UDP 연결 포트
         */
        private final int mUdpPort;

        /**
         * UDP Thread Stop Flag
         */
        private boolean mThreadStop;

        /**
         * Constructor
         * @param port
         */
        public UDPConnector(int port) {
            mUdpPort = port;
            mThreadStop = false;
        }

        /**
         * UDP Thread Stop
         */
        public void udpStop() {
            mThreadStop = true;
        }

        @Override
        public void run() {

            while (mThreadStop == false) {
                try {
                    // UDP 소켓 Open / Listen Timeout 5초 설정
                    if ( mUDPSocket == null ) {
                        mUDPSocket = new DatagramSocket(mUdpPort);
                        mUDPSocket.setSoTimeout(5000);
                    }

                    // 메세지 버퍼 1024 설정
                    byte[] receiveBuf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(receiveBuf,receiveBuf.length);
                    mUDPSocket.receive(packet);
                    Log.d("UDP", "Receive" + new String(packet.getData()));

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * UDP Send Packet Thread
     */
    private class UDPSendPacket extends Thread {
        /**
         * UDP Socket
         */
        private final DatagramSocket mSocket;

        /**
         * Send Byte
         */
        private final byte[] mSendByte;

        /**
         * Constructor
         * @param socket
         * @param bytes
         */
        public UDPSendPacket(DatagramSocket socket, byte[] bytes) {
            mSocket = socket;
            mSendByte = bytes;
        }

        @Override
        public void run() {

            if ( mSocket != null ) {

                try {
                    // 패킷 전송
                    DatagramPacket sendPacket = new DatagramPacket(mSendByte, mSendByte.length);
                    mUDPSocket.send(sendPacket);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
