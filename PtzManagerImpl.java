package logitech.hardware.camera;

import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.logitech.service.models.ptzf.PtzfData;
import com.logitech.service.baseservice.IServiceConnectionListener;
import com.logitech.service.ptzfservice.IPtzfServiceListener;
import com.logitech.service.ptzfservice.PtzfServiceListenerStub;
import com.logitech.service.ptzfservice.PtzfServiceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: probably move most of logic into a PtzManagerGlobal
 *
 * @hide
 */
public class PtzManagerImpl extends PtzManager {
    private static final String TAG = "PtzManager_KONG";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public API
    ////////////////////////////////////////////////////////////////////////////////////////////////
    enum DeviceState {
        CLAIMED,
        AVAILABLE,
        ERROR
    }

    private static final String TEST_DEVICE = "test";

    public static void getInstance(Context context, Callback<PtzManager> callback) {
        synchronized (PtzManagerImpl.class) {
            if (sInstance == null) {
Log.w(TAG, "creating new context...");
                sInstance = new PtzManagerImpl(context);
            } else {
                Log.w(TAG, "reusing PTZ manager possibly with different context...");
            }

        try {
            ((PtzManagerImpl) sInstance).pollForDevice(10, 2);
        } catch (InterruptedException exception) {
            Log.w(TAG, "Interrupted exception " + exception.getLocalizedMessage());
        }

            callback.call(sInstance);
        }
    };

    @Override
    public List<String> getPtzCameras() {
Log.w(TAG, "returning cameras.."+mDevices.size());
        return new ArrayList<>(mDevices.keySet());
    }

    @Override
    public void open(String cameraId, StateCallback callback) {
        open(cameraId, DEVICE_MODE_CONTROL, callback, null);
    }

    @Override
    public void open(String cameraId, @DeviceMode int mode, StateCallback callback, @Nullable Handler handler) {
        if (cameraId == null) {
            throw new IllegalArgumentException("cameraId must not be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        Handler callbackHandler = handler == null ? new Handler(Looper.getMainLooper()) : handler;

        synchronized (mLock) {
            if (!mDevices.containsKey(cameraId)) {
                // todo: error
                Log.e(TAG, "open(): Can't open cameraId='"+cameraId+"', camera isn't PTZ camera");
                return;
            }

            DeviceHolder deviceHolder = mDevices.get(cameraId);
            if (deviceHolder.state != DeviceState.AVAILABLE) {
                callbackHandler.post(() -> {
                    callback.onError(deviceHolder.device, StateCallback.ERROR_PTZ_DEVICE_IN_USE);
                });
                return;
            }
            deviceHolder.clientCallback = callback;
            deviceHolder.clientHandler = callbackHandler;
            deviceHolder.state = DeviceState.CLAIMED;

            callbackHandler.post(() -> {
                callback.onOpened(deviceHolder.device);
            });
        }
    }

    @Override
    public PtzDeviceCharacteristics getCharacteristics() {
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // \end Public API

    // this is temporary solution
    @SuppressLint("StaticFieldLeak")
    private static PtzManager sInstance;

    private static final boolean DEBUG = true;

    Context mContext;
    PtzfServiceManager mService;
    final Object mLock = new Object();

    private final Map<String, DeviceHolder> mDevices = new HashMap<>();


    PtzManagerImpl(Context context) {
Log.w(TAG, "entered constructor...");
        this.mContext = context;

        mService = new PtzfServiceManager();
        mService.addConnectionListener(mServiceConnectionListener);
        if (!mService.bind()) {
            Log.e(TAG, "First attempt to connect to PTZFService failed");
        }
Log.w(TAG, "constructor completed...");
    }

    private IServiceConnectionListener mServiceConnectionListener = new IServiceConnectionListener() {
        @Override
        public void onServiceConnected() {
            Log.d(TAG, "onServiceConnected()");
            synchronized (mLock) {
                setupDevicesLocked();
            }
        }

        @Override
        public void onServiceDisconnected() {
            Log.d(TAG, "onServiceDisconnected()");
            synchronized (mLock) {
                notifyClientOfError(getPtzCameras().get(0));
            }
        }
    };

    private IPtzfServiceListener mListener = new PtzfServiceListenerStub() {
        @Override
        public void onUpdate(final PtzfData speed) {
            Log.v(TAG, "IPtzfServiceListener Update");
        }
    };

    private void notifyClientOfError(String cameraId) {
        Log.d(TAG, "notifyClientOfError() called with: cameraId = [" + cameraId + "]");
        DeviceHolder deviceHolder = mDevices.get(cameraId);

        if (deviceHolder != null) {
            final Handler handler = deviceHolder.clientHandler;

            if (handler != null) {
                handler.post(() -> {
                    StateCallback callback = deviceHolder.clientCallback;
                    Looper looper = handler.getLooper();
                    if (callback != null) {
                        callback.onError(deviceHolder.device, StateCallback.ERROR_PTZ_SERVICE);
                    }
                });
            }

            deviceHolder.clientCallback = null;
            deviceHolder.clientHandler = null;
            deviceHolder.state = DeviceState.ERROR;
        }

        sInstance = null;
        mService.removeConnectionListener(mServiceConnectionListener);
    }

    private void setupDevicesLocked() {
Log.w(TAG, "inside setupdevices locked...");
        mDevices.clear();

        // only one for now...
        // PtzDevice device = new PtzDevice(new ICameraPtzDevice(mService));

        PtzDevice device = new PtzDeviceImpl(mService, TEST_DEVICE, () -> closeDevice(TEST_DEVICE));
        DeviceHolder holder = new DeviceHolder(device);
        mDevices.put(TEST_DEVICE, holder);
Log.w(TAG, "added devices..");
    }

    private void closeDevice(String cameraId) {
        Log.d(TAG, "closeDevice() called with: cameraId = [" + cameraId + "]");
        synchronized (mLock) {
            DeviceHolder deviceHolder = mDevices.get(cameraId);

            if (deviceHolder != null) {
                if (deviceHolder.state != DeviceState.CLAIMED) {
                    Log.e(TAG, "closeDevice() for cameraId=" + cameraId + " state is " +
                            deviceHolder.state);
                    return;
                }
                final Handler handler = deviceHolder.clientHandler;

                if (handler != null) {
                    handler.post(() -> {
                        StateCallback callback = deviceHolder.clientCallback;
                        Looper looper = handler.getLooper();
                        if (callback != null) {
                            callback.onClosed(deviceHolder.device);
                        }
                    });
                }

                deviceHolder.clientCallback = null;
                deviceHolder.clientHandler = null;
                deviceHolder.state = DeviceState.AVAILABLE;
            }
        }
    }


    private String[] getCamerasz() {
        CameraManager manager = mContext.getSystemService(CameraManager.class);
        String cameraId = null;
        try {
            for (String s : manager.getCameraIdList()) {
                if (cameraId == null) cameraId = s;
                CameraCharacteristics cc = manager.getCameraCharacteristics(cameraId);
                int orientation = cc.get(CameraCharacteristics.LENS_FACING);
                if (orientation == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = s;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return cameraId == null ? new String[] {} : new String[]{ cameraId };
    }

    public void pollForDevice(Integer timeOutInMilliseconds, Integer timeBetweenPoll) throws InterruptedException {
        for (int i = 0; i <= timeOutInMilliseconds; i += timeBetweenPoll) {
            if (mDevices.size() == 0){
Log.w(TAG, "waiting now...");
 Thread.sleep(timeBetweenPoll);
}
            else break;
        }
    }

    static class DeviceHolder {
        final PtzDevice device;
        Handler clientHandler;
        StateCallback clientCallback;
        DeviceState state = DeviceState.AVAILABLE;

        private DeviceHolder(PtzDevice device) {
            this.device = device;
        }
    }


//
//
//    private void init() {
//        // should be from service
//        String[] cameras = getPtzCameras();
//
//        synchronized (mLock) {
//            mDevices.clear();
//            for (String camera : cameras) {
//                mDevices.put(camera, null);
//            }
//            //setState(STATE_CONNECTED);
//        }
//
//    }
//
//    void openDeviceLocked(String cameraId, StateCallback callback, @DeviceMode int mode) {
//        assertState(State.CONNECTED);
//
//        // todo check things
//        PtzDeviceClient client = new PtzDeviceClient();
//        client.callback = callback;
//        client.mode = mode;
//
//    void connect() {
//        synchronized (mLock) {
//            assertState(State.DISCONNECTED);
//            setState(State.CONNECTING);
//
//            Intent intent = new Intent(PTZF_SERVICE_ACTION);
//            intent.setPackage(PTZF_SERVICE_PACKAGE);
//            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        }
//    }
//
//
////        mClients.put(cameraId, )
//    }
//
//    /**
//     * TODO: replace with real implementation, from service
//     *
//     * return cameraIds associated with PTZ cameras, should come from service
//     */
//
//
//    void disconnect() {
//        synchronized (mLock) {
//
//        }
//    }
//


}
