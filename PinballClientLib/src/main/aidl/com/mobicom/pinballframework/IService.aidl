// IService.aidl
package com.mobicom.pinballframework;

import com.mobicom.pinballframework.ICallback;
import com.mobicom.pinballframework.IByteArray;

// Declare any non-default types here with import statements

interface IService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    void registerCallback(ICallback cb);
    void unregisterCallback(ICallback cb);
}
