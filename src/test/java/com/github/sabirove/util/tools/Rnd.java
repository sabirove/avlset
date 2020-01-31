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

package com.github.sabirove.util.tools;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Rnd {
    public static boolean rollTheDice() {
        return ThreadLocalRandom.current().nextBoolean();
    }
    public static int rndInt(int from, int to) {
        return ThreadLocalRandom.current().nextInt(from, to);
    }
    public static int rndInt() {
        return ThreadLocalRandom.current().nextInt();
    }
    public static <T> T oneOf(List<T> values) {
        return values.get(rndInt(0, values.size()));
    }
    @SafeVarargs
    public static <T> T oneOf(T... values) {
        return values[rndInt(0, values.length)];
    }
    public static int generateValue() {
        return rndInt(-10000, 10001);
    }
    public static List<Integer> generateValues(int size) {
        return Stream.generate(Rnd::generateValue)
                .limit(size)
                .collect(Collectors.toList());
    }
}
