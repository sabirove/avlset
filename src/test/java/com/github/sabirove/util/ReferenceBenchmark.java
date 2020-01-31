/*
 *  Copyright 2020 Sabirov Evgenii (sabirov.e@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.sabirove.util;

import com.github.sabirove.util.tools.Rnd;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 * Benchmark testing basic operations against the reference {@link TreeSet} impl.
 */
@Fork(value = 1, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ReferenceBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ReferenceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @State(Scope.Benchmark)
    public static class InputData {
        @Param
        Dataset dataset;
        Integer[] values;

        @Setup(Level.Trial)
        public void setUp() {
            this.values = dataset.getValues();
        }
    }

    @State(Scope.Thread)
    public static class EmptySet {
        @Param
        Impl impl;
        NavigableSet<Integer> set;

        @Setup(Level.Invocation)
        public void setUp() {
            set = impl.build.get();
        }
    }

    @State(Scope.Thread)
    public static class FullSet {
        @Param
        Impl impl;
        NavigableSet<Integer> set;

        @Setup(Level.Invocation)
        public void setUp(InputData data) {
            set = impl.build.get();
            Collections.addAll(set, data.values);
        }
    }

    @Benchmark
    public static void benchAdd(EmptySet emptySet, InputData data) {
        NavigableSet<Integer> set = emptySet.set;
        for (Integer value : data.values) {
            set.add(value);
        }
    }

    @Benchmark
    public static void benchContains(FullSet fullSet, InputData data, Blackhole blackhole) {
        NavigableSet<Integer> set = fullSet.set;
        for (Integer value : data.values) {
            blackhole.consume(set.contains(value));
        }
    }

    @Benchmark
    public static void benchRemove(FullSet fullSet, InputData data) {
        NavigableSet<Integer> set = fullSet.set;
        for (Integer value : data.values) {
            set.remove(value);
        }
    }

    public enum Impl {
        AVL_SET(AvlSet::of),
        TREE_SET(TreeSet::new);

        final Supplier<NavigableSet<Integer>> build;

        Impl(Supplier<NavigableSet<Integer>> build) {
            this.build = build;
        }
    }

    public enum Dataset {
        INTS_10K(10_000),
        INTS_100K(100_000),
        INTS_1M(1_000_000);

        final int size;
        Integer[] values;

        Dataset(int size) {
            this.size = size;
        }

        public synchronized Integer[] getValues() {
            if (values != null) {
                return values;
            }
            Integer[] read = readFromFile();
            if (read == null) {
                writeToFile();
                read = readFromFile();
            }
            values = read;
            return read;
        }

        private String filePath() {
            return Paths.get("src", "test", "resources", name() + ".bin").toString();
        }

        private void writeToFile() {
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath())))) {
                for (int i = 0; i < size; i++) {
                    dos.writeInt(Rnd.rndInt());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Integer[] readFromFile() {
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath())))) {
                Integer[] ints = new Integer[size];
                for (int i = 0; i < size; i++) {
                    ints[i] = dis.readInt();
                }
                return ints;
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
