package rocks.xmpp.util.concurrent;

import rocks.xmpp.util.XmppUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class QueuedScheduledExecutorService extends QueuedExecutorService implements ScheduledExecutorService {

    /**
     * Executor that will schedule the tasks
     */
    private static final QueuedScheduledThreadPoolExecutor SCHEDULER = new QueuedScheduledThreadPoolExecutor(0, XmppUtils.createNamedThreadFactory("Scheduler Thread"));

    private final Set<Future<?>> futures;

    private boolean removeOnCancel;

    /**
     * @param delegate the executor that will handle the tasks
     */
    public QueuedScheduledExecutorService(ExecutorService delegate) {
        super(delegate);
        this.futures = new HashSet<>();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return SCHEDULER.schedule(new QueuedRunnable(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return SCHEDULER.schedule(new QueuedCallable<>(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return SCHEDULER.scheduleAtFixedRate(new QueuedRunnable(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return SCHEDULER.scheduleWithFixedDelay(new QueuedRunnable(command), initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {

        for (Future<?> future : Collections.unmodifiableSet(futures)) {
            future.cancel(false);
        }

        super.shutdown();

    }

    public void setRemoveOnCancelPolicy(boolean removeOnCancel) {
        this.removeOnCancel = removeOnCancel;
    }

    private abstract class QueuedTask {

        private void done(Future<?> future) {
            QueuedScheduledExecutorService.this.futures.remove(future);
        }

        private boolean getRemoveOnCancelPolicy() {
            return QueuedScheduledExecutorService.this.removeOnCancel;
        }

        private void submit(Future<?> future) {
            QueuedScheduledExecutorService.this.futures.add(future);
        }

    }

    private class QueuedCallable<T> extends QueuedTask implements Callable<T> {

        private final Callable<T> callable;

        QueuedCallable(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        public T call() throws Exception {
            return QueuedScheduledExecutorService.this.submit(callable).get();
        }

    }

    private class QueuedRunnable extends QueuedTask implements Runnable {

        private final Runnable runnable;

        QueuedRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            QueuedScheduledExecutorService.this.submit(runnable);
        }
    }

    private static class QueuedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

        QueuedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
            super(corePoolSize, threadFactory);
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {

            RunnableScheduledFuture<V> result;

            if (callable instanceof QueuedTask) {
                QueuedTask queuedTask = (QueuedTask) callable;
                result = new ScheduledFutureTask<>(callable, queuedTask, task);
                queuedTask.submit(result);
            } else {
                result = task;
            }

            return result;

        }


        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {

            RunnableScheduledFuture<V> result;

            if (runnable instanceof QueuedTask) {
                QueuedTask queuedTask = (QueuedTask) runnable;
                result = new ScheduledFutureTask<>(runnable, queuedTask, task);
                queuedTask.submit(result);
            } else {
                result = task;
            }

            return result;

        }

    }

    private static class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

        private final RunnableScheduledFuture<V> delegate;

        private final QueuedTask task;

        ScheduledFutureTask(Callable<V> callable, QueuedTask task, RunnableScheduledFuture<V> delegate) {
            super(callable);
            this.delegate = delegate;
            this.task = task;
        }

        ScheduledFutureTask(Runnable runnable, QueuedTask task, RunnableScheduledFuture<V> delegate) {
            super(runnable, null);
            this.delegate = delegate;
            this.task = task;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {

            boolean cancelled = super.cancel(mayInterruptIfRunning);

            if (cancelled && task.getRemoveOnCancelPolicy()) {
                SCHEDULER.remove(this);
            }

            return cancelled;

        }

        @Override
        public boolean isPeriodic() {
            return delegate.isPeriodic();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return delegate.getDelay(unit);
        }

        @Override
        public int compareTo(Delayed o) {
            return delegate.compareTo(o);
        }

        @Override
        protected void done() {
            super.done();
            this.task.done(this);
        }

    }

}
