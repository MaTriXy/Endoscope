package pl.hypeapp.endoscope.presenter;

import android.Manifest;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.rx2.RxTiPresenterDisposableHandler;

import pl.hypeapp.endoscope.view.QrCodeScannerView;

import io.reactivex.functions.Consumer;

public class QrCodeScannerPresenter extends TiPresenter<QrCodeScannerView> {
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private final RxTiPresenterDisposableHandler rxHelper = new RxTiPresenterDisposableHandler(this);
    private RxPermissions rxPermissions;
    private String ipAddress;

    public QrCodeScannerPresenter(RxPermissions rxPermissions) {
        this.rxPermissions = rxPermissions;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        askForPermission();
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();
        startQrCodeCamera();
    }

    @Override
    protected void onSleep() {
        super.onSleep();
        stopQrCodeCamera();
    }

    public void askForPermission() {
        rxHelper.manageDisposable(rxPermissions.requestEach(CAMERA_PERMISSION)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            onPermissionGranted();
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            onShouldShowRequestPermissionRationale();
                        } else {
                            onPermissionNotGranted();
                        }
                    }
                }));
    }

    public void onQrCodeRead(String ipAddress) {
        this.ipAddress = ipAddress;
        getView().stopQrCodeScanner();
        getView().showQrCodeScanResult(ipAddress);
    }

    public void connectToStream() {
        getView().intentToPlayStreamActivity(this.ipAddress);
    }

    private void onPermissionNotGranted() {
        getView().showPermissionNeverAskInfo();
        getView().hidePermissionGrantButton();
    }

    private void onShouldShowRequestPermissionRationale() {
        getView().showPermissionNotGrantedInfo();
        getView().showPermissionGrantButton();
    }

    private void onPermissionGranted() {
        getView().hidePermissionGrantButton();
        getView().hidePermissionInfo();
        getView().startQrCodeScanner();
    }

    private void stopQrCodeCamera() {
        getView().stopQrCodeScanner();
    }

    private void startQrCodeCamera() {
        if (rxPermissions.isGranted(CAMERA_PERMISSION)) {
            getView().startQrCodeScanner();
            getView().hideQrCodeScanResult();
        }
    }
}
