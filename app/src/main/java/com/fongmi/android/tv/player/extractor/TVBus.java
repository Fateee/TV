package com.fongmi.android.tv.player.extractor;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.LiveConfig;
import com.fongmi.android.tv.bean.Core;
import com.fongmi.android.tv.player.Source;
import com.google.gson.JsonObject;
import com.tvbus.engine.Listener;
import com.tvbus.engine.TVCore;

public class TVBus implements Source.Extractor, Listener {

    private TVCore tvcore;
    private String hls;
    private Core core;

    @Override
    public boolean match(String scheme, String host) {
        return scheme.equals("tvbus");
    }

    private void init(Core core) {
        App.get().setHook(true);
        tvcore = new TVCore(core.getSo());
        tvcore.auth(core.getAuth()).broker(core.getBroker());
        tvcore.name(core.getName()).pass(core.getPass());
        tvcore.serv(0).play(8902).mode(1).listener(this);
        tvcore.init();
    }

    @Override
    public String fetch(String url) throws Exception {
        if (core != null && !core.equals(LiveConfig.get().getHome().getCore())) change();
        if (tvcore == null) init(core = LiveConfig.get().getHome().getCore());
        tvcore.start(url);
        onWait();
        return hls;
    }

    private void onWait() throws InterruptedException {
        synchronized (this) {
            wait();
        }
    }

    private void onNotify() {
        synchronized (this) {
            notify();
        }
    }

    private void change() {
        App.post(() -> System.exit(0), 250);
        Setting.putBootLive(true);
    }

    @Override
    public void stop() {
        if (tvcore != null) tvcore.stop();
        if (hls != null) hls = null;
    }

    @Override
    public void exit() {
        if (tvcore != null) tvcore.quit();
        tvcore = null;
    }

    @Override
    public void onPrepared(String result) {
        JsonObject json = App.gson().fromJson(result, JsonObject.class);
        if (json.get("hls") == null) return;
        hls = json.get("hls").getAsString();
        onNotify();
    }

    @Override
    public void onStop(String result) {
    }

    @Override
    public void onInited(String result) {
    }

    @Override
    public void onStart(String result) {
    }

    @Override
    public void onInfo(String result) {
    }

    @Override
    public void onQuit(String result) {
    }
}
