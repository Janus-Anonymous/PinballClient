package com.mobicom.pinballclientlib;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.Tag;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import com.android.pinball.PinballRequest;
import com.android.pinball.Response;
import com.mobicom.pinballframework.IByteArray;
import com.mobicom.pinballframework.ICallback;
import com.mobicom.pinballframework.PinballRemoteService;

public class PinballFrameworkClient extends Thread {


    private PriorityBlockingQueue<PinballRequest> mQueue = new PriorityBlockingQueue<PinballRequest>();
    /**
     * listenerMap stands for the map relation between url and its response listener
     */
    private Map<String, Response.Listener<byte[]>> listenerMap = new HashMap<String, Response.Listener<byte[]>>();
    /**
     * EResponseMap: url --> EResponse
     * Every url requires an output stream buffer to store corresponding data.
     * Here we use a Map rather than a single variable, taking this situation into consideration:
     * multiple requests receive their callback data simultaneously.
     */
    private Map<String, ByteArrayOutputStream> ByteResponseMap = new HashMap<String, ByteArrayOutputStream>();
    /**
     * ERequestMap:  url --> ERequest
     */
    private Map<String, ByteRequest> ByteRequestMap = new HashMap<String, ByteRequest>();
    /**
     * TAG of client
     */
    String TAG = "PinballFrameworkClient";
    /**
     * Context
     */
    Context context = null;
    /**
     * Remote service
     */
    PinballRemoteService pbRemoteService;
    /**
     * Pinball framework client instance.
     */
    static PinballFrameworkClient instance = null;
    /**
     * Whether the remote service is successfully binded.
     */
    public boolean isBind = false;

    /**
     * The content length of every request(B).
     */
    final long CONTENT_LENGTH_PER_REQUEST = 1000 * 1024;


    /**
     * Here, bindService will try to bind remote service and do remote operation.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /**
             *   Following the example above for an AIDL interface,
             *   this gets an instance of the IRemoteInterface,
             *   which we can use to call on the service
             */
            Log.d(TAG, "Connected to remote service.");
            pbRemoteService = PinballRemoteService.Stub.asInterface(service);
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Called when the connection with the service disconnects unexpectedly
            Log.e(TAG, "Service has unexpectedly disconnected");
            pbRemoteService = null;
        }
    };


    /**
     * run() method will be executed in the constructor.
     */
    public void run() {
        final Intent intent = new Intent();
        intent.setAction("com.mobicom.pinballframework.PinballService");
        final Intent eintent = new Intent(createExplicitFromImplicitIntent(context, intent));
        context.bindService(eintent, mConnection, Context.BIND_AUTO_CREATE);

        /**
         * Wait until the service is successfully binded.
         */
        while (!isBind) ;
        ICallback.Stub mCallback = new ICallback.Stub() {

            /**
             * Demonstrates some basic types that you can use as parameters
             * and return values in AIDL.
             *
             * @param anInt
             * @param aLong
             * @param aBoolean
             * @param aFloat
             * @param aDouble
             * @param aString
             */
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }

            @Override
            public void showResult(int result) throws RemoteException {
                Log.e(TAG, "showResult is called.");
            }

            @Override
            public void CallbackString(String s) {
                Log.e(TAG, "Show Downloaded String:\n"+ s);
            }

            public void CallbackObject(IByteArray b) {
                if (b.getByte() == null) {
                    Log.e(TAG, "b.getByte()==null");
                } else {
                    Log.e(TAG, "received byte, size= " + b.getByte().length);
                }
            }

            public void CallbackByte(byte[] result, String url) throws RemoteException{
                /**
                 * result.length < CONTENT_LENGTH_PER_REQUEST
                 * means the entire data of this url has been downloaded completely,
                 * so the corresponding listener's onResponse() method can be called.
                 */
                try {
                    ByteResponseMap.get(url).write(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(result.length <= CONTENT_LENGTH_PER_REQUEST ) {
                    try {
                        listenerMap.get(url).onResponse(ByteResponseMap.get(url).toByteArray());
                    }catch (Exception e) {
                        /**
                         * Forbid remote exception warnings.
                         * Do not use e.printStackTrace() here.
                         */
                    }
                }
                else {
                    _putByteRequest(ByteRequestMap.get(url), listenerMap.get(url), Long.parseLong(ByteRequestMap.get(url).sProperty.split("-")[1]));
                }
            }

        };

        while(true)
        {
            try {
                PinballRequest request = mQueue.take();
                Log.d(TAG, "call remote service after checking binding state: " + isBind);
                pbRemoteService.putRequest(request.url, request.delay, request.tag, request.sProperty, mCallback);

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * This constructor make sure that only one instance exists.
     * @param context
     */
    private PinballFrameworkClient(Context context) {
        this.context = context;
        this.start();
    }

    public static PinballFrameworkClient getInstance(Context context) {
        if (instance == null) {
            instance = new PinballFrameworkClient(context);
        }
        return instance;
    }

    public void putRequest(ByteRequest er, final Response.Listener<byte[]> rl){
        String url = er.byteRequestUrl;
        Log.i(TAG,url);
        listenerMap.put(url, rl);
        ByteRequestMap.put(url, er);
        ByteResponseMap.put(url, new ByteArrayOutputStream());
        _putByteRequest(er, rl, 0);
    }

    private void _putByteRequest(ByteRequest er, final Response.Listener<byte[]> rl, long nStartPos){
        er.setListener(rl);
        er.sProperty = "bytes=" + nStartPos + "-" + (nStartPos + CONTENT_LENGTH_PER_REQUEST);
        mQueue.add(er);
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     * <p/>
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     * <p/>
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     *
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

}
