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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import rocks.xmpp.util.XmppUtils;

/**
 * A {@link ScheduledExecutorService} implementation of a queued executor service.
 *
 * @see QueuedExecutorService
 */
public class QueuedScheduledExecutorService extends QueuedExecutorService implements ScheduledExecutorService {

    /**
     * Executor that will schedule the tasks.
     */
    private static final ScheduledThreadPoolExecutor SCHEDULER = new ScheduledThreadPoolExecutor(1, XmppUtils.createNamedThreadFactory("Scheduler Thread"));

    /**
     * Sequence number to break scheduling ties.
     */
    private static final AtomicLong SEQUENCER = new AtomicLong();

    /**
     * List of all the tasks currently running.
     */
    private final Set<RunnableScheduledFuture<?>> futures;

    /**
     * False if should cancel/suppress periodic tasks on shutdown.
     */
    private volatile boolean keepPeriodic;

    /**
     * False if should cancel non-periodic tasks on shutdown.
     */
    private volatile boolean keepDelayed;

    /**
     * True if ScheduledFutureTask.cancel should remove from queue
     */
    private volatile boolean removeOnCancel;

    /**
     * @param delegate the executor that will handle the tasks
     */
    public QueuedScheduledExecutorService(Executor delegate) {

        super(delegate);

        this.futures = Collections.synchronizedSet(new HashSet<>());
        this.keepPeriodic = false;
        this.keepDelayed = true;
        this.removeOnCancel = false;
    }

    private static long getInitialDelay(long delay, TimeUnit unit) {
        return System.nanoTime() + unit.toNanos(delay);
    }

    @Override
    public boolean isTerminated() {
        return super.isTerminated() && futures.isEmpty();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return newScheduledFuture(Executors.callable(command, null), getInitialDelay(delay, unit), 0, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return newScheduledFuture(Objects.requireNonNull(callable), getInitialDelay(delay, unit), 0, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return newScheduledFuture(Executors.callable(command, null), getInitialDelay(initialDelay, unit), period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return newScheduledFuture(Executors.callable(command, null), getInitialDelay(initialDelay, unit), -delay, unit);
    }

    private <V> ScheduledFuture<V> newScheduledFuture(Callable<V> callable, long initialDelay, long delay, TimeUnit unit) {
        if (isShutdown()) {
            throw new RejectedExecutionException("Executor is shutdown");
        }
        return new ScheduledFutureTask<>(callable, initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        onShutdown();
    }

    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
        keepPeriodic = value;

        if (!value && isShutdown()) {
            onShutdown();
        }
    }

    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
        keepDelayed = value;

        if (!value && isShutdown()) {
            onShutdown();
        }
    }

    public void setRemoveOnCancelPolicy(boolean removeOnCancel) {
        this.removeOnCancel = removeOnCancel;
    }

    private void onShutdown() {
        Set<RunnableScheduledFuture<?>> copy;
        synchronized (this.futures) {
            copy = new HashSet<>(this.futures);
        }
        for (RunnableScheduledFuture<?> future : copy) {
            if ((future.isPeriodic() ? !keepPeriodic : !keepDelayed) || future.isCancelled()) {
                future.cancel(false);
            }
        }
    }

    private final class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

        private final Callable<V> callable;

        private final long period;

        private final long sequence;

        private long time;

        private ScheduledFutureTask(Callable<V> callable, long time, long period, TimeUnit unit) {

            super(callable);

            this.callable = callable;
            this.period = unit.toNanos(period);
            this.sequence = SEQUENCER.getAndIncrement();
            this.time = time;

            QueuedScheduledExecutorService.this.futures.add(this);

            queueNextRun();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {

            boolean cancelled = super.cancel(mayInterruptIfRunning);

            if (cancelled && QueuedScheduledExecutorService.this.removeOnCancel) {
                SCHEDULER.remove(this);
            }
            return cancelled;
        }

        @Override
        public boolean isCancelled() {
            return super.isCancelled();
        }

        @Override
        public boolean isDone() {
            return super.isDone();
        }

        @Override
        public boolean isPeriodic() {
            return period != 0;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return super.get();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return super.get(timeout, unit);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(time - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed other) {

            if (other == this) {
                return 0;
            }

            if (other instanceof QueuedScheduledExecutorService.ScheduledFutureTask) {

                QueuedScheduledExecutorService.ScheduledFutureTask<?> otherTask;
                otherTask = (QueuedScheduledExecutorService.ScheduledFutureTask<?>) other;

                long deltaTime = time - otherTask.time;

                if (deltaTime < 0) {
                    return -1;
                } else if (deltaTime > 0) {
                    return 1;
                } else if (sequence < otherTask.sequence) {
                    return -1;
                } else {
                    return 1;
                }

            }

            long deltaDelay = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);

            return (deltaDelay < 0) ? -1 : (deltaDelay > 0) ? 1 : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ScheduledFutureTask)) {
                return false;
            }
            ScheduledFutureTask other = (ScheduledFutureTask) o;
            return Objects.equals(callable, other.callable)
                    && period == other.period
                    && sequence == other.sequence
                    && time == other.time;
        }

        @Override
        public int hashCode() {
            return Objects.hash(callable, period, sequence, time);
        }

        @Override
        public void run() {
            if (canRun()) {
                QueuedScheduledExecutorService.this.execute(this::doRun, true);
            } else {
                cancel(false);
            }
        }

        @Override
        protected void done() {
            QueuedScheduledExecutorService.this.futures.remove(this);
            synchronized (QueuedScheduledExecutorService.this.awaitTerminationLock) {
                QueuedScheduledExecutorService.this.awaitTerminationLock.notifyAll();
            }
        }

        private boolean canRun() {
            return !QueuedScheduledExecutorService.this.isShutdown() || (isPeriodic() ? keepPeriodic : keepDelayed);
        }

        private void doRun() {
            boolean isPeriodic = isPeriodic();
            try {
                if (!canRun()) {
                    cancel(false);
                } else if (!isPeriodic) {
                    set(callable.call());
                } else {
                    callable.call();
                }
            } catch (Exception e) {
                setException(e);
            } finally {
                if (isPeriodic) {
                    if (period > 0) {
                        time += period;
                    } else {
                        time = System.nanoTime() - period;
                    }

                    queueNextRun();
                }
            }
        }

        private void queueNextRun() {
            SCHEDULER.getQueue().add(this);

            if (QueuedScheduledExecutorService.this.isShutdown() && !canRun() && SCHEDULER.remove(this)) {
                cancel(false);
            } else {
                SCHEDULER.prestartCoreThread();
            }
        }
    }
}
