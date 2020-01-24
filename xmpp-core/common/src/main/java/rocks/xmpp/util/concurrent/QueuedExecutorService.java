package rocks.xmpp.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueuedExecutorService extends AbstractExecutorService {

    /**
     * Lock to wait in {@link #awaitTermination(long, TimeUnit)}.
     */
    final Object awaitTerminationLock;

    /**
     * Executor that will handle the tasks
     */
    private final ExecutorService delegate;

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
    public QueuedExecutorService(ExecutorService delegate) {
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

    void execute(Runnable command, boolean ignoreShutdown) {

        if (ignoreShutdown || !isShutdown()) {

            // Adds a task to the queue and call ThreadQueue#poll in order to process it (if the queue
            // allows it, otherwise, wait for a Thread to be available).
            tasks.add(command);
            poll(delegate);
        }

    }

    /**
     * Find outs if the queue contains any tasks and handle the next one if necessary.
     */
    private void poll(Executor executor) {

        // If the queue is not empty and no task is running, then we can continue and attempt to
        // retrieve a task.
        Runnable task = tasks.peek();
        if (task != null && !hasRunningTask.getAndSet(true)) {
            tasks.remove(task);
            // Queue the task for further processing
            executor.execute(() -> doExecute(task));
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
            // Don't run the next task (if any) with delegate.execute(), which creates a new thread,
            // but in the same thread as "task" has been run.
            poll(Runnable::run);
        }
    }
}
