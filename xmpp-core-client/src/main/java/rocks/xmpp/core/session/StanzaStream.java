/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.core.session;

import rocks.xmpp.core.stanza.model.Stanza;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Manages stanza streams.
 *
 * @author Christian Schudt
 */
final class StanzaStream implements Stream<Stanza> {

    private final BlockingQueue<Stanza> stanzas = new LinkedBlockingQueue<>();

    private final Stream<Stanza> stream;

    private volatile boolean closed;

    StanzaStream(XmppSession xmppSession) {
        stream = StreamSupport.stream(new InfiniteQueueSpliterator(), false);
        stream.onClose(() -> {
            xmppSession.inboundStanzaStreams.remove(this);
            closed = true;
        });
    }

    void offer(Stanza e) {
        stanzas.offer(e);
    }

    @Override
    public Stream<Stanza> filter(Predicate<? super Stanza> predicate) {
        return stream.filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super Stanza, ? extends R> mapper) {
        return stream.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super Stanza> mapper) {
        return stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super Stanza> mapper) {
        return stream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super Stanza> mapper) {
        return stream.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super Stanza, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super Stanza, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super Stanza, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super Stanza, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    @Override
    public Stream<Stanza> distinct() {
        return stream.distinct();
    }

    @Override
    public Stream<Stanza> sorted() {
        return stream.sorted();
    }

    @Override
    public Stream<Stanza> sorted(Comparator<? super Stanza> comparator) {
        return stream.sorted();
    }

    @Override
    public Stream<Stanza> peek(Consumer<? super Stanza> action) {
        return stream.peek(action);
    }

    @Override
    public Stream<Stanza> limit(long maxSize) {
        return stream.limit(maxSize);
    }

    @Override
    public Stream<Stanza> skip(long n) {
        return stream.skip(n);
    }

    @Override
    public void forEach(Consumer<? super Stanza> action) {
        stream.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super Stanza> action) {
        stream.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    @Override
    public Stanza reduce(Stanza identity, BinaryOperator<Stanza> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    @Override
    public Optional<Stanza> reduce(BinaryOperator<Stanza> accumulator) {
        return stream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super Stanza, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Stanza> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super Stanza, A, R> collector) {
        return stream.collect(collector);
    }

    @Override
    public Optional<Stanza> min(Comparator<? super Stanza> comparator) {
        return stream.min(comparator);
    }

    @Override
    public Optional<Stanza> max(Comparator<? super Stanza> comparator) {
        return stream.max(comparator);
    }

    @Override
    public long count() {
        return stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super Stanza> predicate) {
        return stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super Stanza> predicate) {
        return stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super Stanza> predicate) {
        return stream.noneMatch(predicate);
    }

    @Override
    public Optional<Stanza> findFirst() {
        return stream.findFirst();
    }

    @Override
    public Optional<Stanza> findAny() {
        return stream.findAny();
    }

    @Override
    public Iterator<Stanza> iterator() {
        return stream.iterator();
    }

    @Override
    public Spliterator<Stanza> spliterator() {
        return stream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public Stream<Stanza> sequential() {
        return stream.sequential();
    }

    @Override
    public Stream<Stanza> parallel() {
        return stream.parallel();
    }

    @Override
    public Stream<Stanza> unordered() {
        return stream.unordered();
    }

    @Override
    public Stream<Stanza> onClose(Runnable closeHandler) {
        return stream.onClose(closeHandler);
    }

    @Override
    public void close() {
        stream.close();
    }

    private final class InfiniteQueueSpliterator extends Spliterators.AbstractSpliterator<Stanza> {

        protected InfiniteQueueSpliterator() {
            super(Long.MAX_VALUE, IMMUTABLE);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Stanza> action) {

            Stanza element = null;
            try {
                while (element == null) {
                    // If the stream has been closed.
                    if (closed) {
                        return false;
                    }
                    // Let's see, if there's some data for me.
                    element = stanzas.poll(1, TimeUnit.SECONDS);
                }
                action.accept(element);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}
