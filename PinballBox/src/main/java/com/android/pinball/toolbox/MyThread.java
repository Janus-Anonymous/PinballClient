
package com.android.pinball.toolbox;

public class MyThread extends Thread {

    public void run() {
        synchronized (this) {
            notify();
        }
    }

}
