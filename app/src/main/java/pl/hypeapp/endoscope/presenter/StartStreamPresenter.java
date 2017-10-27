package pl.hypeapp.endoscope.presenter;

import android.Manifest;
import android.support.annotation.NonNull;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.rx2.RxTiPresenterDisposableHandler;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import pl.hypeapp.endoscope.view.StartStreamView;

import io.reactivex.functions.Consumer;


public class StartStreamPresenter extends TiPresenter<StartStreamView> implements RtspServer.CallbackListener {
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final String RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;
    private static final String RTSP_SERVER_ERROR = "RTSP SERVER ERROR ";
    private static final int RTSP_STREAM_CONNECTING = 0;
    private final RxTiPresenterDisposableHandler rxHelper = new RxTiPresenterDisposableHandler(this);
    private final RxPermissions rxPermissions;
    private RtspServer rtspServer;

    public StartStreamPresenter(RxPermissions rxPermissions) {
        this.rxPermissions = rxPermissions;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        askForCameraPermission();
        startRtspServer();
    }

    @Override
    protected void onAttachView(@NonNull StartStreamView view) {
        super.onAttachView(view);
        getView().registerReceivers();
        getView().putIpAddressToSharedPreferences();
        getView().initViewPager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRtspServer();
    }

    @Override
    public void onError(RtspServer server, Exception e, int error) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(RTSP_SERVER_ERROR).append(error).append(" ").append(e.getMessage());
        getView().logError(stringBuilder.toString());
    }

    @Override
    public void onMessage(RtspServer server, int message) {
        if (message == RTSP_STREAM_CONNECTING) getView().showStreamConnectingToast();
    }

    public void onSessionStopped() {
        if (getView() != null) {
            getView().showAboutConnectionLayout();
            getView().setNotFullscreenWindow();
        }
    }

    public void onSessionStarted() {
        getView().hideAboutConnectionLayout();
        getView().setFullscreenWindow();
    }

    public void onSessionError(Exception e) {
        if (getView() != null)
            getView().logError(e.getMessage());
    }

    private void askForCameraPermission() {
        rxHelper.manageDisposable(rxPermissions.requestEach(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {

                        } else if (permission.shouldShowRequestPermissionRationale) {
                            onShouldShowRequestPermissionRationale();
                        } else {
                            onNeverAskPermission();
                        }
                    }
                }));
    }

    private void onShouldShowRequestPermissionRationale() {
        getView().onPermissionNotGranted();
    }

    private void onNeverAskPermission() {
        getView().onNeverAskPermission();
    }

    private void startRtspServer() {
        rtspServer = new RtspServer();
        rtspServer.addCallbackListener(this);
        rtspServer.start();
    }

    private void stopRtspServer() {
        rtspServer.stop();
    }

    public void onSurfaceCreated() {
        getView().buildSession();
    }

    public void onSurfaceDestroyed() {
        getView().releaseSession();
    }
}
