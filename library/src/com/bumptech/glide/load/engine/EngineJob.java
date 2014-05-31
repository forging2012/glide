package com.bumptech.glide.load.engine;

import android.os.Handler;
import com.bumptech.glide.Resource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.request.ResourceCallback;

import java.util.HashSet;
import java.util.Set;

/**
 * @param <Z> The type of the resource that will be decoded.
 */
public class EngineJob<Z> implements ResourceCallback<Z> {
    private final EngineJobListener listener;
    private Key key;
    private MemoryCache cache;
    private Handler mainHandler;
    private Set<ResourceCallback<Z>> cbs = new HashSet<ResourceCallback<Z>>();
    private boolean isCancelled;
    private boolean isComplete;

    public EngineJob(Key key, MemoryCache cache, Handler mainHandler, EngineJobListener listener) {
        this.key = key;
        this.cache = cache;
        this.listener = listener;
        this.mainHandler = mainHandler;
    }

    public void addCallback(ResourceCallback<Z> cb) {
        cbs.add(cb);
    }

    public void removeCallback(ResourceCallback cb) {
        cbs.remove(cb);
        if (cbs.size() == 0) {
            cancel();
        }
    }

    // Exposed for testing.
    void cancel() {
        if (isComplete || isCancelled) {
            return;
        }
        isCancelled = true;
        listener.onEngineJobCancelled(key);
    }

    // Exposed for testing.
    boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void onResourceReady(final Resource<Z> resource) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isCancelled) {
                    resource.recycle();
                    return;
                }
                isComplete = true;

                // 1 for memory cache.
                resource.acquire(cbs.size() + 1);
                listener.onEngineJobComplete(key);
                cache.put(key, resource);
                for (ResourceCallback<Z> cb : cbs) {
                    cb.onResourceReady(resource);
                }

            }
        });
    }

    @Override
    public void onException(final Exception e) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isCancelled) {
                    return;
                }
                isComplete = true;

                listener.onEngineJobComplete(key);
                for (ResourceCallback cb : cbs) {
                    cb.onException(e);
                }
            }
        });
    }
}
