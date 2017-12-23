package com.github.gammaray360.MiOpenApi;

import com.github.gammaray360.MiOpenApi.Exceptions.DeviceNotConnectedException;
import com.github.gammaray360.MiOpenApi.Exceptions.DeviceNotDisconnectedException;
import com.github.gammaray360.MiOpenApi.Exceptions.MissingTokenException;
import com.github.gammaray360.BlockingCollections.BlockingArray;
import com.sun.istack.internal.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by junk_ on 04/11/2017.
 */
public class MiDevice {
    private final static Logger LOGGER = Logger.getLogger(MiDevice.class.getName());

    // region Class members and methods: {...}
    private static final int PORT = 54321; // default port for yeelight devices
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private static final byte[] EMPTY_TOKEN = new byte[16];
    private final static int TIME_BONUS = 10;
    private final static int SYNC_SLEEP_TIME = 2000; // ms
    private final static int MAX_WAITING_SYNC = 50;
    private final static int JOIN_TIME_OUT = 2000; // ms
    private final static int MAX_PAYLOAD_ID = 40;
    private final static String PAYLOAD_ID_MARKER = "%id";
    // endregion

    // region flags {...}
    public final static int OVERRIDE_TOKEN = 0b1;
    public final static int OVERRIDE_DEVICE_ID = 0b10;
    public final static int OVERRIDE_TIME = 0b100;
    public final static int DEFAULT_FLAGS = OVERRIDE_TOKEN | OVERRIDE_DEVICE_ID | OVERRIDE_TIME;
    private int mFlags;
    // endregion

    // region Status: {...}
    public static final int DISCONNECTED = 0b1;
    public static final int CONNECTED = 0b10;
    public static final int SYNCING = 0b100;
    private int mStatus = DISCONNECTED;
    // endregion

    // region Properties: {...}
    private InetAddress mIp;
    private int mPort = PORT;
    private byte[] mToken = new byte[16];
    private byte[] mDeviceId =  new byte[4];
    private long mTimeDifference;
    // endregion

    // region Private Members: {...}
    private MiSocket mSocket;
    private ExecutorService mOutgoingDataThread;
    private IncomingDataThread mIncomingDataThread;
    private SyncingThread mSyncingThread;
    private ExecutorService mResponseDelivererThread;
    private final MiPacket mIncomingMiPacket = new MiPacket(); // to use for recycle
    private final MiPacket mOutgoingMiPacket = new MiPacket(); // to use for recycle
    private final Object lockReceive = new Object();
    private final Object lockSend = new Object();
    private int currentPayloadId = 0;
    private boolean syncFromPacket(MiPacket packet, int flags){
        if(packet.hasPayload()){
            LOGGER.log(Level.WARNING,"trying to sync from packet with payload");
    }
        if((flags & OVERRIDE_TOKEN) != 0){
            if(Arrays.equals(packet.getMd5(), EMPTY_TOKEN) ){
                LOGGER.log(Level.WARNING,"received token=0 when trying to connect");
                throw new MissingTokenException("received token=0");
            }
            mToken = packet.getMd5();
            LOGGER.log(Level.INFO,"set Token= {0}",MiProtocolHelper.toString(mToken));
        }
        if((flags & OVERRIDE_DEVICE_ID) != 0) {
            mDeviceId = packet.getDeviceId();
            LOGGER.log(Level.INFO,"set Device ID= {0}",MiProtocolHelper.toString(mDeviceId));
        }
        if((flags & OVERRIDE_TIME) != 0) {
            mTimeDifference = System.currentTimeMillis()/1000 - MiProtocolHelper.toLong(packet.getTimeStamp());
            LOGGER.log(Level.INFO,"Set time difference= {0}",mTimeDifference);

        }
        LOGGER.log(Level.INFO,"Device synced");
        return true;
    }
    private byte[] getDeviceTimeWithBonus(){
        long deviceTime = System.currentTimeMillis()/1000 - mTimeDifference + TIME_BONUS;
        return MiProtocolHelper.toByteArray(deviceTime,4); //TODO replace this with constant
    }
    private void deliverResponse(String payload){
        mResponseDelivererThread.submit(new DeliverResonanceRubble(payload));
    }
    private String nextPayloadId(){
        return String.valueOf(currentPayloadId = currentPayloadId%MAX_PAYLOAD_ID +1);
    }
    private BlockingArray<String> mBlockingArray = new BlockingArray<>(MAX_PAYLOAD_ID);
    private int embedPayloadId(StringBuilder s){
        int index = s.indexOf(PAYLOAD_ID_MARKER);
        if(index == -1){
            return 0;
        }
        s.replace(index,index+PAYLOAD_ID_MARKER.length(),nextPayloadId());
        return currentPayloadId;
    }
    private int getPayloadId(String s){
        int index;
        try {
            JSONObject json = new JSONObject(s);
            index = json.getInt("id");
        } catch (JSONException e) {
            return 0;
        }
        return index;
    }
    // endregion

    // region Getters and Setters: {...}
    public synchronized InetAddress getIp() {
        return mIp;
    }
    public synchronized MiDevice setIp(InetAddress ip) {
        if(mStatus != DISCONNECTED) {
            throw new DeviceNotDisconnectedException("Can't change device properties while device is connected. disconnect first");
        }
        mIp = ip;
        return this;
    }
    public synchronized int getPort() {
        return mPort;
    }
    public synchronized MiDevice setPort(int port) {
        if(mStatus != DISCONNECTED) {
            throw new DeviceNotDisconnectedException("Can't change device properties while device is connected. disconnect first");
        }
        if(port<MIN_PORT || port>MAX_PORT){
            throw new IllegalArgumentException("Illegal port number");
        }
        mPort = port;
        return this;
    }
    public synchronized byte[] getToken() {
        return mToken.clone();
    }
    public synchronized MiDevice setToken(byte[] token) {
        if(mStatus != DISCONNECTED) {
            throw new DeviceNotDisconnectedException("Can't change device properties while device is connected. disconnect first");
        }
        if(token == null){
            Arrays.fill( mToken, (byte) 0 );
            return this;
        }
        System.arraycopy(token,0,mToken,0,mToken.length);
        return this;    }
    public synchronized byte[] getDeviceId() {
        return mDeviceId.clone();
    }
    public synchronized MiDevice setDeviceId(byte[] deviceId) {
        if(mStatus != DISCONNECTED) {
            throw new DeviceNotDisconnectedException("Can't change device properties while device is connected. disconnect first");
        }
        if(deviceId == null){
            Arrays.fill( mDeviceId, (byte) 0 );
            return this;
        }
        System.arraycopy(deviceId,0,mDeviceId,0,mDeviceId.length);
        return this;
    }
    public synchronized long getTimeDifference() {
        return mTimeDifference;
    }
    public synchronized void setTimeDifference(long timeDifference) {
        if(mStatus != DISCONNECTED) {
            throw new DeviceNotDisconnectedException("Can't change device properties while device is connected. disconnect first");
        }
        mTimeDifference = timeDifference;
    }
    public synchronized int getStatus(){
        return mStatus;
    }
    // endregion

    // region constructors {...}
    public MiDevice(InetAddress ip){
        mIp = ip;
    }
    public MiDevice(InetAddress ip, int port, @Nullable byte[] token,
                          @Nullable byte[] deviceId, long timeDifference){
        setIp(ip);
        setPort(port);
        setToken(token);
        setDeviceId(deviceId);
        setTimeDifference(timeDifference);
    }
    // endregion

    // region Public methods: {...}
    public synchronized boolean isConnected(){
        return (mStatus == CONNECTED);
    }
    public synchronized void connect() throws IOException{
        connect(DEFAULT_FLAGS);
    }

    public synchronized void connect(int flags) throws IOException
    {
        if(mStatus != DISCONNECTED) {
            throw new DeviceNotDisconnectedException("Can't connect, Device is not disconnected");
        }

        try {
            mFlags = flags;
            LOGGER.log(Level.INFO, "trying to connect");
            mSocket = new MiSocket(mIp, mPort);
            LOGGER.log(Level.INFO, "sending sync packet");
            mSocket.sync();
            LOGGER.log(Level.INFO, "waiting for sync packet back.");
            MiPacket syncPacket = new MiPacket();
            mSocket.receive(syncPacket);
            LOGGER.log(Level.INFO, "got packet: \n{0}", syncPacket);
            syncFromPacket(syncPacket,mFlags);
        } catch (IOException e){
            LOGGER.log(Level.WARNING, "connection failed");
            mSocket.close();
            mSocket = null;
            throw e;
        }

        mIncomingDataThread = new IncomingDataThread();
        mIncomingDataThread.start();
        mSyncingThread = new SyncingThread(SYNC_SLEEP_TIME,MAX_WAITING_SYNC);
        mSyncingThread.start();
        mOutgoingDataThread = Executors.newSingleThreadExecutor();
        mResponseDelivererThread = Executors.newSingleThreadExecutor(); // this better be more then one thread
        mStatus = CONNECTED;
        LOGGER.log(Level.INFO, "Device connected");

    }
    public synchronized void sync() throws IOException {
        if(mStatus != CONNECTED){
            throw new DeviceNotConnectedException("Can't sync, Device is not connected");
        }
        LOGGER.log(Level.INFO, "sending sync packet");
        mSocket.sync();
    }

    public synchronized void disconnect(){
        if(mStatus != CONNECTED){
            throw new DeviceNotConnectedException("Can't disconnect, Device is not connected");
        }
        LOGGER.log(Level.INFO, "trying to disconnect");

        LOGGER.log(Level.INFO, "trying to close socket");
        mSocket.close();


        LOGGER.log(Level.INFO, "trying to close syncing thread");
        try {
            mSyncingThread.interrupt();
            mSyncingThread.join(JOIN_TIME_OUT);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "cant terminate syncing thread");
        }

        LOGGER.log(Level.INFO, "trying to close incoming data thread");
        try {
            mIncomingDataThread.interrupt();
            mIncomingDataThread.join(JOIN_TIME_OUT);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "cant terminate incoming Data Thread");
        }

        LOGGER.log(Level.INFO, "trying to close outgoing thread");
        mOutgoingDataThread.shutdownNow();


        LOGGER.log(Level.INFO, "device disconnected");


        mSocket = null;
        mIncomingDataThread = null;
        mSyncingThread = null;
        mOutgoingDataThread = null;

        mStatus = DISCONNECTED;
    }
    public String send(String payload) throws IOException, InterruptedException, CancellationException, TimeoutException {
        Future<Integer> fut;
        int id=0;
        StringBuilder out = new StringBuilder(payload);
        synchronized (this){
            if(mStatus != CONNECTED){
                throw new DeviceNotConnectedException("Can't send, Device is not connected");
            }
            id = embedPayloadId(out);
            mBlockingArray.init(id);
            fut = mOutgoingDataThread.submit(new outgoingDataCallable(out.toString()));
        }
        try {
            fut.get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        if(id == 0){
            return null;
        }
        return mBlockingArray.get(id);

    }
    public synchronized byte[] getDeviceTime() throws IOException{
        if(mStatus != CONNECTED){
            throw new DeviceNotConnectedException("Can't get device time, Device is not connected");
        }
        long deviceTime = System.currentTimeMillis()/1000 - getTimeDifference();
        return MiProtocolHelper.toByteArray(deviceTime,4); //TODO replace this with constant
    }
    // endregion

    // region threads {...}
    private class outgoingDataCallable implements Callable<Integer>{
        String mmPayload;
        outgoingDataCallable(String payload){
            mmPayload = payload;
        }
        @Override
        public Integer call() throws Exception {
            synchronized (lockSend){ // not really necessary if single threaded
                int id = getPayloadId(mmPayload);
                mOutgoingMiPacket.setTimeStamp(getDeviceTimeWithBonus())
                                        .setDeviceId(mDeviceId)
                                        .setPayload(mmPayload)
                                        .encryptAndCalculateChecksum(mToken);
                LOGGER.log(Level.INFO,"sending packet: \n{0}",mOutgoingMiPacket);
                mSocket.send(mOutgoingMiPacket);
                return id;
            }
        }
    }
    private class DeliverResonanceRubble implements Runnable{
        String mmPayload;

        DeliverResonanceRubble(String payload){
            mmPayload = payload;
        }
        @Override
        public void run() {
            int id = getPayloadId(mmPayload);
            if(id == 0){
                return;
            }
            try {
                mBlockingArray.set(id,mmPayload);
            } catch (InterruptedException e) {
                try {
                    mBlockingArray.cancel(id);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private class IncomingDataThread extends Thread{
        private boolean listening = false;

        @Override
        public void run() {
            listening = true;
            while(listening){
                try {
                    String payload;
                    synchronized (lockReceive) { // not really necessary if single threaded
                        mSocket.receive(mIncomingMiPacket);
                        mSyncingThread.resetWaitingSyncCounter();
                        if(!mIncomingMiPacket.hasPayload()){
                            syncFromPacket(mIncomingMiPacket,mFlags & OVERRIDE_TIME);
                            continue;
                        } else{
                            mIncomingMiPacket.decrypt(getToken());
                        }
                        //LOGGER.log(Level.INFO, "got packet: {0}", mIncomingMiPacket);
                        payload = mIncomingMiPacket.getPayload();
                        if(isInterrupted()){
                            LOGGER.log(Level.WARNING, "incoming data thread interrupted. terminating");
                            return;
                        }
                    }
                    deliverResponse(payload);
                }catch (SocketException e){
                    LOGGER.log(Level.WARNING, "Socket closed. terminating");
                    return;
                }
                catch (IOException e){
                    LOGGER.log(Level.SEVERE, "{0}", e);
                }
            }
        }
    }
    private class SyncingThread extends Thread{
        private boolean mmSyncing = true;
        private int mmSleepTime;
        private int mmWaitingSyncCounter = 0;;
        private int mmMaxWaitingSync = 0;

        public synchronized void resetWaitingSyncCounter(){
            mmWaitingSyncCounter = 0;
        }
        private synchronized int getWaitingSyncCounter(){
            return mmWaitingSyncCounter;
        }
        private synchronized void increaseWaitingSyncCounter(){
            mmWaitingSyncCounter++;
        }

        public SyncingThread(int sleepTime, int maxWaitingSync){
            mmSleepTime = sleepTime;
            mmMaxWaitingSync = maxWaitingSync;
        }

        @Override
        public void run() {
            while (mmSyncing){
                try {
                    if(getWaitingSyncCounter() > mmMaxWaitingSync){
                        LOGGER.log(Level.WARNING, "syncing thread can't receive sync. disconnecting");
                        disconnect();
                        return;
                    }
                    Thread.sleep(mmSleepTime);
                    mSocket.sync();
                    increaseWaitingSyncCounter();
                    LOGGER.log(Level.INFO, "auto sending sync packet. waiting sync counter = " + getWaitingSyncCounter());

                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "syncing thread interrupted. terminating");
                    return;
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "syncing thread can't send sync. disconnecting");
                    disconnect();
                    return;
                }
            }
        }
    }
    // endregion

    @Override
    public synchronized String toString() {
        return "MiDevice{ip=" + mIp.toString() + "}";
    }
}
