package com.cnk24.udpclient;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientUtil {

    private static int __PORT = 15000;
    private static UDPClientUtil __sharedUDPClient = null;

    private boolean mIsStart = false;
    private UDPConnector mUdpConnectorThread = null;
    private DatagramSocket mUDPSocket = null;

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
     * Constructor
     */
    private UDPClientUtil() {
        super();
    }

    /**
     * UDP 연결
     */
    public void connectUdpAddressAndPort() {
        if ( mIsStart == false ) {
            mIsStart = true;
            mUdpConnectorThread = new UDPConnector(__PORT);
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

        private final int mUdpPort;
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

        private static int TYPE_MESSAGE = 1;
        private static int TYPE_FILE = 2;

        private final DatagramSocket mSocket;
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

        private void UDPSendFile() {
            File f = new File("./img/2.png");
            if (!f.exists()) {
                System.exit(0);
            }

            try {
                InetAddress ia = InetAddress.getByName("127.0.0.1");

                // 데이터 전송 시작 신호 전송
                String str = "start";
                // Constructs a datagram packet for sending packets of length length
                // to the
                // specified port number on the specified host.
                DatagramPacket dp = new DatagramPacket(str.getBytes(), str.getBytes().length, ia, __PORT);

                // 데이터 전송
                mSocket.send(dp);
                // 파일이름 전송
                String data = "2.png";
                dp = new DatagramPacket(data.getBytes(), data.getBytes().length, ia, __PORT);
                mSocket.send(dp);

                // 파일을 읽을때는 바이트단위!!
                // 파일을 바이트단위로 읽고, 너무많이 접근하니까이걸가지고 다시 버퍼에 넣는다
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
                FileInputStream fi = new FileInputStream(f);

                System.out.printf(dis.toString());

                // 한번에 다읽으면 부담되기 때문에 적절한크기의 버퍼로 나눔
                byte[] by = new byte[512];
                int count = 0;
                while (true) {
                    // dis 를 읽어 by 배열에 0부터 length만큼 저장
                    // 읽은 갯수 리턴.
                    int x = dis.read(by, 0, by.length);
                    if (x == -1)
                        break;
                    // 읽은 갯수까지 'x'
                    dp = new DatagramPacket(by, x, ia, __PORT); // *
                    mSocket.send(dp);
                    System.out.println(x);

                    count++;

                }
                System.out.println(count);
                // 전송신호전송
                str = "end";
                dp = new DatagramPacket(str.getBytes(), str.getBytes().length, ia, __PORT);
                mSocket.send(dp);

                dis.close();
                mSocket.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        @Override
        public void run() {

            if ( mSocket != null ) {
                try {
                    DatagramPacket sendPacket = new DatagramPacket(mSendByte, mSendByte.length);
                    mSocket.send(sendPacket);
                    mSocket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
