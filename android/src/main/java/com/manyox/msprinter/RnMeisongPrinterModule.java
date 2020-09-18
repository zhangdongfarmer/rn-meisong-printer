package com.manyox.msprinter;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.manyox.msprinter.util.UsbPrinter;

public class RnMeisongPrinterModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final UsbPrinter printer;

    public RnMeisongPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.printer = new UsbPrinter(reactContext);
    }

    @Override
    public String getName() {
        return "RnMeisongPrinter";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        this.printer.printtest();

        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    //初始化
    /**
     * 初始化
     */
    @ReactMethod
    public void init() {
        this.printer.printtest();
    }

    //打印test

}
