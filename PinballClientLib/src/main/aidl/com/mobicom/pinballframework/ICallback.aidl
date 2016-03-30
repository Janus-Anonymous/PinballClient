// ICallback.aidl
package com.mobicom.pinballframework;
// Declare any non-default types here with import statements
import com.mobicom.pinballframework.IByteArray;

interface ICallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    void showResult(int result);
    void CallbackString(String s);
    void CallbackObject(out IByteArray result);
    void CallbackByte(in byte[] result, in String url);
}
