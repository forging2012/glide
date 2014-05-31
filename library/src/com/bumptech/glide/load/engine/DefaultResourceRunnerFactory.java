package com.bumptech.glide.load.engine;

import android.os.Handler;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.resource.ResourceFetcher;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

class DefaultResourceRunnerFactory implements ResourceRunnerFactory {
    private final Handler bgHandler;
    private MemoryCache memoryCache;
    private DiskCache diskCache;
    private Handler mainHandler;
    private ExecutorService service;

    public DefaultResourceRunnerFactory(MemoryCache memoryCache, DiskCache diskCache, Handler mainHandler,
            ExecutorService service, Handler bgHandler ) {
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
        this.mainHandler = mainHandler;
        this.service = service;
        this.bgHandler = bgHandler;
    }

    @Override
    public <T, Z> ResourceRunner<Z> build(Key key, int width, int height,
            ResourceDecoder<InputStream, Z> cacheDecoder, ResourceFetcher<T> fetcher, ResourceDecoder<T, Z> decoder,
            Transformation<Z> transformation, ResourceEncoder<Z> encoder, Priority priority,
            EngineJobListener listener) {

        EngineJob<Z> engineJob = new EngineJob<Z>(key, memoryCache, mainHandler, listener);

        SourceResourceRunner<T, Z> sourceRunner = new SourceResourceRunner<T, Z>(key, width, height, fetcher, decoder,
                transformation, encoder, diskCache, priority, engineJob);

        return new ResourceRunner<Z>(key, width, height, diskCache, cacheDecoder, sourceRunner, service, bgHandler,
                engineJob);
    }
}
