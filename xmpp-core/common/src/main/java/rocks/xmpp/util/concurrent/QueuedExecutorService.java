/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Queues tasks and delegates them to a {@link Executor} in order.
 * <p>
 * The resulting behavior is similar to a single threaded pool:
 * Tasks submitted to this class are guaranteed to be executed in the same order as they were submitted,
 * but potentially are executed by different threads of the delegate executor.
 * <p>
 * The typical use case is, that you share a single {@link java.util.concurrent.ThreadPoolExecutor} among
 * multiple instances of this class, which results in a low overall thread count (only the ones spawned by the delegate executor),
 * but still have the in-order execution behavior of a single threaded pool.
 */
public class QueuedExecutorService extends AbstractExecutorService {

    /**
     * Lock to wait in {@link #awaitTermination(long, TimeUnit)}.
     */
    final Object awaitTerminationLock;

    /**
     * Executor that will handle the tasks
     */
    private final Executor delegate;

    /**
     * Lock indicating whether or not a task is pending completion.
     */
    private final AtomicBoolean hasRunningTask;

    /**
     * Lock indicating whether or not the delegate has been shutdown.
     */
    private final AtomicBoolean shutdown;

    /**
     * Queue of all the pending tasks.
     */
    private final BlockingQueue<Runnable> tasks;

    /**
     * @param delegate the executor that will handle the tasks
     */
    public QueuedExecutorService(Executor delegate) {
        this.delegate = delegate;
        this.hasRunningTask = new AtomicBoolean(false);
        this.awaitTerminationLock = new Object();
        this.shutdown = new AtomicBoolean(false);
        this.tasks = new LinkedBlockingQueue<>();
    }

    @Override
    public void execute(Runnable command) {
        execute(command, false);
    }

    void execute(Runnable command, boolean ignoreShutdown) {

        if (ignoreShutdown || !isShutdown()) {
            // Adds a task to the queue and call ThreadQueue#poll in order to process it (if the queue
            // allows it, otherwise, wait for a Thread to be available).
            tasks.add(command);
            poll();
        } else {
            throw new RejectedExecutionException("Executor Service is shutdown");
        }
    }

    @Override
    public void shutdown() {
        shutdown.set(true);
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> result = new ArrayList<>();
        tasks.drainTo(result);
        return result;
    }

    @Override
    public boolean isShutdown() {
        return shutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return isShutdown() && tasks.isEmpty() && !hasRunningTask.get();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {

        long nanos = unit.toNanos(timeout);

        synchronized (awaitTerminationLock) {
            while (true) {
                if (isTerminated()) {
                    return true;
                } else if (nanos <= 0) {
                    return false;
                } else {
                    long now = System.nanoTime();
                    TimeUnit.NANOSECONDS.timedWait(awaitTerminationLock, nanos);
                    nanos -= System.nanoTime() - now;
                }
            }
        }
    }

    /**
     * Find outs if the queue contains any tasks and handle the next one if necessary.
     */
    private void poll() {

        // If the queue is not empty and no task is running, then we can continue and attempt to
        // retrieve a task.
        Runnable task = tasks.peek();
        if (task != null && !hasRunningTask.getAndSet(true)) {
            tasks.remove(task);
            // Queue the task for further processing
            try {
                delegate.execute(() -> doExecute(task));
            } catch (RejectedExecutionException e) {
                hasRunningTask.set(false);
                throw e;
            }
        } else if (task == null) {
            synchronized (awaitTerminationLock) {
                awaitTerminationLock.notifyAll();
            }
        }
    }

    /**
     * Performs a task.
     *
     * @param task the request being task.
     */
    private void doExecute(Runnable task) {
        try {
            task.run();
        } finally {
            // The task is complete, release the lock and queue the next task.
            hasRunningTask.set(false);
            poll();
        }
    }
}
