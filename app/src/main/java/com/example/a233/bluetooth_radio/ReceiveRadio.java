//package com.example.a233.bluetooth_radio;
//
//import android.app.PendingIntent;
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import android.os.Message;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ReceiveRadio extends Service{
//
//    private BroadcastReceiver foundReceiver=null;
//    Handler myHandler;
//    private Looper mLooper;
//    static final Map<String,MsgBlueRadio> msgStore=new HashMap<>();
//    static final int FOUND_MESSAGE=3001;
//    private LocalBroadcastManager myLocalBroadcastManager;
//    public static final String EXTRA_CONTENT_MESSAGE_ADDRESS="com.example.a233.bluetooth_radio.EXTRA_CONTENT_MESSAGE_ADDRESS";
//    public static final String EXTRA_CONTENT_MESSAGE_TEXT="com.example.a233.bluetooth_radio.EXTRA_CONTENT_MESSAGE_TEXT";
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//    public void onCreate(){
//        super.onCreate();
//        myLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
//    }
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        foregroundNotification();
//        MyBluetoothMethodManager.starBluetoothDeviceSearch();
//        Thread myThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                mLooper = Looper.myLooper();
//                myHandler = new Handler() {
//                    public void handleMessage(Message msg) {
//                        if (msg.what == FOUND_MESSAGE) {
//                            MsgStruct contentMsg = (MsgStruct) msg.obj;
//                            if (contentMsg.text != null) {
//                                byte[] msgText = null;
//                                try {
//                                    msgText = contentMsg.text.getBytes("UTF-8");
//                                } catch (UnsupportedEncodingException e) {
//                                    e.printStackTrace();
//                                }
//                                Log.i("MyBluetoothMethod", msgText.length+"handleMessage: "+contentMsg.text);
//                                if (null!=msgText  &&
//                                        msgText.length >ChangeNameOfBluetooth.baseByteMsg &&//控制最小长度
//                                        isHaveSignal(msgText)
//                                        ) {
//                                    if (msgStore.containsKey(contentMsg.address)) {
//                                        msgStore.get(contentMsg.address).setMessage(msgText);
//                                    } else {
//                                        MsgBlueRadio item = new MsgBlueRadio(contentMsg.address);
//                                        item.setMessage(msgText);
//                                        msgStore.put(contentMsg.address, item);
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                };
//                Looper.loop();
//            }
//        });
//        broadcastFoundMessage();
//        myThread.start();
//        return START_REDELIVER_INTENT;
//    }
//    //Check the first 3 byte in array ,is it the REPLACEMENT CHARACTER in UTF8{(byte)0xef ,(byte)0xbf ,(byte)0xbd}
//    boolean isHaveSignal(byte[] source){
//        boolean flag=false;
//        final byte[] array=ChangeNameOfBluetooth.Signal;
//        if(array.length<source.length) {
//            flag=true;
//            for (int i = 0; i <array.length;i++){
//                if(array[i]!=source[i]){
//                    flag=false;
//                }
//            }
//        }
//        return flag;
//    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mLooper.quit();
//        if(foundReceiver!=null) {
//            unregisterReceiver(foundReceiver);
//        }
//        MyBluetoothMethodManager.stopBluetoothDeviceSearch();
//        stopForeground(true);
//        Intent intent = new Intent(MainActivity.ServiceOnDestroy);
//        myLocalBroadcastManager.sendBroadcast(intent);
//    }
//    //Run service on foreground
//    private void foregroundNotification() {
//        final String channelID = "com.example.a233.Receive_radio.foregroundNotification";
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setContentTitle("ReceiveBluetoothRadio")
//                .setContentText("Receive broadcast");
//
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//        startForeground(30, builder.build());
//    }
//    //Receive System Broadcast, when Bluetooth advice is found,get the name and MAC address of the advice
//    //And push it in Loop of this Service
//    void broadcastFoundMessage(){
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//         foundReceiver = new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                // When discovery finds a device
//                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    // Get the BluetoothDevice object from the Intent
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    Message msg=new Message();
//                    MsgStruct struct=new MsgStruct();
//                    struct.text=device.getName() ;
//                    struct.address =device.getAddress();
//                    msg.obj=struct;
//                    msg.what=FOUND_MESSAGE;
//                    myHandler.sendMessage(msg);
//                    Log.i("broadcastFoundMessage", "struct.address "+struct.address);
//                }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                    MyBluetoothMethodManager.starBluetoothDeviceSearch();
//                }
//                else  if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
//                    Log.i("StartNewRadio", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
//                }
//            }
//        };
//        registerReceiver(foundReceiver, filter);
//    }
//
//    //Assign one instance for each Mac address
//    class MsgBlueRadio {
//        private String addressMac;
//        private int messageTotal ;
//        private int messageID ;
//        private int localMessageTotal;
//        private List<byte[]> messageBodyList;
//        MsgBlueRadio(String MAC){
//            this.messageTotal=-1;
//            this.messageID=-1;
//            this.messageBodyList=null;
//            this.localMessageTotal=0;
//            this.addressMac=MAC;
//        }
//        void setMessage(byte[] msg){
//            final int length=msg.length;
//            final int zero=ChangeNameOfBluetooth.signalZero;
//            if(length>ChangeNameOfBluetooth.baseByteMsg) {
//                final int  base=ChangeNameOfBluetooth.baseByteMsg;
//                setMessageBody(msg[base-3]&0xFF-zero,
//                        msg[base-2]&0xFF-zero,
//                        msg[base-1]&0xFF-zero, msg);
//            }
//        }
//        //Remove the signal bytes(First 6 byte)
//        private byte[] editMessage(byte [] newMessageBody){
//           final int  base=ChangeNameOfBluetooth.baseByteMsg;
//            byte [] msg=new byte[newMessageBody.length-base];
//            System.arraycopy(newMessageBody,base,msg,0,msg.length);
//            return msg;
//        }
//        //Save pieces of massage in List
//        // when collected all pieces,stitch them to one String instance ,and send to MainActivity
//        private void setMessageBody(int newSerial,int newTotal,int newID,byte[] newMessageBody) {
//            newTotal++;
//            if (newID != this.messageID || newTotal != this.messageTotal) {
//                this.messageTotal = newTotal;
//                this.messageID = newID;
//                this.messageBodyList = new ArrayList<>(newTotal);
//                for (int i = 0; i < newTotal; i++) {
//                    this.messageBodyList.add(null);
//                }
//                this.localMessageTotal = 0;
//            }
//            byte[] messageBody = editMessage(newMessageBody);
//            if (this.messageBodyList.get(newSerial) == null) {
//                this.messageBodyList.set(newSerial, messageBody);
//                this.localMessageTotal++;
//                if (localMessageTotal == messageTotal) {
//                    String text = byteStitching(this.messageBodyList);
//                    Bundle bundle = new Bundle();
//                    bundle.putString(EXTRA_CONTENT_MESSAGE_ADDRESS, addressMac);
//                    bundle.putString(EXTRA_CONTENT_MESSAGE_TEXT, text);
//                    Intent intent = new Intent(MainActivity.LocalAction_RefreshUI);
//                    intent.putExtras(bundle);
////                saveData(contentMsg.address,contentMsg.text);TODO:SAVE_DATA
//                    myLocalBroadcastManager.sendBroadcast(intent);
//                }
//            }
//        }
//        private String byteStitching(List<byte[]> pieceList){
//            int totalLength=0;
//            for(int i=0;i<pieceList.size();i++){
//                totalLength+=pieceList.get(i).length;
//            }
//            byte[] bigByteArray=new byte[totalLength];
//            int posi=0;
//            for(int i=0;i<pieceList.size();i++){
//                System.arraycopy(pieceList.get(i),0,bigByteArray,posi,pieceList.get(i).length);
//                posi+=pieceList.get(i).length;
//            }
//            return new String(bigByteArray);
//        }
////        private void saveData(String addressMac,String text){
////            SharedPreferences sp = getSharedPreferences("MessageBluetooth_SAVE", Context.MODE_PRIVATE);
////            sp.edit().putString(addressMac,text).apply();
////        }
//    }
//}
//class MsgStruct {
//     String text ="";
//     String address ="";
//}
//
//
//
