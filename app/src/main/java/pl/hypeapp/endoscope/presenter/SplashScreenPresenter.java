package pl.hypeapp.endoscope.presenter;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.rx2.RxTiPresenterDisposableHandler;

import pl.hypeapp.endoscope.util.SettingsPreferencesUtil;
import pl.hypeapp.endoscope.view.SplashScreenView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SplashScreenPresenter extends TiPresenter<SplashScreenView> {
    private static final long ANIMATION_DELAY = 3000;
    private static final long RUN_ACTIVITY_DELAY = 2000;
    private final RxTiPresenterDisposableHandler rxHelper = new RxTiPresenterDisposableHandler(this);
    private SettingsPreferencesUtil settingsPreferencesUtil;

    public SplashScreenPresenter(SettingsPreferencesUtil settingsPreferencesUtil) {
        this.settingsPreferencesUtil = settingsPreferencesUtil;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        delaySplashAnimation(ANIMATION_DELAY);
    }

    public void delayRunActivity() {
        rxHelper.manageDisposable(Observable.timer(RUN_ACTIVITY_DELAY, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        intentToNextActivity();
                    }
                })
        );
    }

    private void intentToNextActivity() {
        boolean isFirstRun = settingsPreferencesUtil.loadIsFirstRunPreference();
        if (isFirstRun) {
            settingsPreferencesUtil.saveIsFirstRunPreference(false);
            getView().intentToHowToUse();
        } else {
            getView().intentToMainMenu();
        }
    }

    private void delaySplashAnimation(long delay) {
        rxHelper.manageDisposable(Observable.timer(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        getView().startSplashAnimation();
                    }
                })
        );
    }
}
