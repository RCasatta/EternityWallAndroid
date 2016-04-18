package it.eternitywall.eternitywall;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.util.LruCache;

import com.subgraph.orchid.crypto.PRNGFixes;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Timer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import it.eternitywall.eternitywall.wallet.EWBinder;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;
import it.eternitywall.eternitywall.wallet.WalletObserver;

public class EWApplication extends MultiDexApplication {
    private static final String TAG = "EWApplication";
    private EWWalletService ewWalletService;
    private WalletObservable walletObservable;
    private WalletObserver walletObserver;

    private Timer timer;
    private TimedLogStat timedLogStat;

    private LruCache<String, Bitmap> bitmapCache;

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className,
                                       final IBinder service) {
            Log.i(TAG,".onServiceConnected()");

            final EWBinder binder = (EWBinder) service;
            ewWalletService = binder.ewWalletService;
            walletObservable = binder.walletObservable;
            walletObserver = new WalletObserver(ewWalletService);
            walletObservable.addObserver(walletObserver);

        }

        @Override
        public void onServiceDisconnected(final ComponentName arg0) {
            Log.i(TAG,".onServiceDisconnected()");
            //walletObservable.deleteObserver(walletObserver);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, ".onCreate()");
        PRNGFixes.apply();
        final Intent intent = new Intent(this, EWWalletService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //startService(intent);

        initLogging();

        if (BuildConfig.DEBUG) {
            timer = new Timer();
            timedLogStat = new TimedLogStat(this);
            timer.schedule(timedLogStat, 30000L, 30000L);
        }

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        Log.i(TAG, "cache size=" + cacheSize);

        bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public LruCache<String, Bitmap> getBitmapCache() {
        return bitmapCache;
    }

    public EWWalletService getEwWalletService() {
        return ewWalletService;
    }

    public WalletObservable getWalletObservable() {
        return walletObservable;
    }

    private static boolean TEST = true;
    private void initLogging()
    {
        final File logDir = getDir("log", TEST ? Context.MODE_WORLD_READABLE : MODE_PRIVATE);
        final File logFile = new File(logDir, "wallet.log");

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
        filePattern.setContext(context);
        filePattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %logger{0} - %msg%n");
        filePattern.start();

        final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
        fileAppender.setContext(context);
        fileAppender.setFile(logFile.getAbsolutePath());

        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/wallet.%d.log.gz");
        rollingPolicy.setMaxHistory(7);
        rollingPolicy.start();

        fileAppender.setEncoder(filePattern);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
        logcatTagPattern.setContext(context);
        logcatTagPattern.setPattern("%logger{0}");
        logcatTagPattern.start();

        final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
        logcatPattern.setContext(context);
        logcatPattern.setPattern("[%thread] %msg%n");
        logcatPattern.start();

        final LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(context);
        logcatAppender.setTagEncoder(logcatTagPattern);
        logcatAppender.setEncoder(logcatPattern);
        logcatAppender.start();

        final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
        log.addAppender(fileAppender);
        log.addAppender(logcatAppender);
        log.setLevel(Level.INFO);
    }
}