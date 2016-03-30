// E3RemoteService.aidl
package com.mobicom.pinballframework;

// Declare any non-default types here with import statements
import com.mobicom.pinballframework.ICallback;

interface PinballRemoteService {
    /** Request the process ID of this service, to do evil things with it. */
    int getPid();
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);

    void putRequest(String url, int delay, String tag, String sProperty, ICallback callback);

    void putStringRequest(String url, int delay, String tag, ICallback callback);

    void putObjectRequest(String url, int delay, String tag, ICallback callback);

    String performStringRequest();

    byte[] performObjectRequest();

    void deleteRequest(String tag);

    void registerCallback(ICallback cb);

    void unregisterCallback(ICallback cb);

}
