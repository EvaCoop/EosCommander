package io.plactal.eoscommander.app;

import android.app.Application;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import io.plactal.eoscommander.BuildConfig;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.data.remote.model.abi.EosAbiMain;
import io.plactal.eoscommander.data.remote.model.api.EosChainInfo;
import io.plactal.eoscommander.data.remote.model.api.PushTxnResponse;
import io.plactal.eoscommander.di.component.AppComponent;
import io.plactal.eoscommander.di.component.DaggerAppComponent;
import io.plactal.eoscommander.di.module.AppModule;
import io.plactal.eoscommander.ui.base.RxCallbackWrapper;
import io.plactal.eoscommander.util.StringUtils;
import io.plactal.eoscommander.util.rx.EoscSchedulerProvider;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Created by swapnibble on 2017-11-03.
 */

public class EosCommanderApp extends Application {
    private AppComponent mAppComponent;

    @Inject
    EoscDataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // https://android-developers.googleblog.com/2013/08/some-securerandom-thoughts.html
        PRNGFixes.apply();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        mAppComponent = DaggerAppComponent.builder()
                .appModule( new AppModule(this))
                .build();

        mAppComponent.inject( this );
        mDataManager.getPreferenceHelper().putNodeosConnInfo("http", "167.99.181.173", 8888);
        System.out.println("yo!");

        EoscSchedulerProvider scheduler = new EoscSchedulerProvider();
        String data = null;
        try {
            data = new JSONObject()
                    .put("from", "eva")
                    .put("to", "Hello my World!")
                    .put("quantity", "10.0000 EVA")
                    .put("memo", "rien").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] permissions = new String[1];
        permissions[0] = "eva@active";
        String privKey = "5J8WRSxpL4nx7bm4yrA2CT6i2X8iVckXKnwV29PC4PPS4V2MC9C";
        String contract = "eosio.token";

        mDataManager
                .getChainInfo()
                .subscribeOn(scheduler.computation())
                .subscribe( new Consumer<EosChainInfo>() {
                    @Override
                    public void accept(EosChainInfo info){
                        System.out.println("info!");
                        System.out.println(info.getChain_id());
                    }
                });
        doPushAction();
    }

    public void doPushAction(){
        String data = null;
        try {
            data = new JSONObject()
                    .put("from", "eva")
                    .put("to", "m1evfycor1b1")
                    .put("quantity", "10.0000 EVA")
                    .put("memo", "rien").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String permissionAccount = "eva";
        String permissionName = "active";
        String[] permissions = ( StringUtils.isEmpty(permissionAccount) || StringUtils.isEmpty( permissionName))
                ? null : new String[]{permissionAccount + "@" + permissionName };
        String privKey = "5J8WRSxpL4nx7bm4yrA2CT6i2X8iVckXKnwV29PC4PPS4V2MC9C";
        String contract = "eosio.token";
        String action = "transfer";
        String messageReplaced = data.replaceAll("\\r|\\n","");
        EoscSchedulerProvider scheduler = new EoscSchedulerProvider();

        System.out.println("contract: " + contract);
        System.out.println("action: " + action);
        System.out.println("message: " + messageReplaced);
        System.out.println("permissionAcct: " + permissions[0]);

        mDataManager
                .pushActionNoWallet(contract, action, messageReplaced, permissions, "5J8WRSxpL4nx7bm4yrA2CT6i2X8iVckXKnwV29PC4PPS4V2MC9C")
                .subscribeOn(scheduler.computation())
                .subscribe( new Consumer<PushTxnResponse>() {
                    @Override
                    public void accept(PushTxnResponse pushTxnResponse) throws Exception {
                        System.out.println("into push");
                        System.out.println(pushTxnResponse.getTransactionId());
                    }
                });
    }

    public void showInfo(EosChainInfo info){
        System.out.println(info.getChain_id());
    }

    public static EosCommanderApp get( Context context ){
        return (EosCommanderApp) context.getApplicationContext();
    }

    public AppComponent getAppComponent() { return mAppComponent; }
}
