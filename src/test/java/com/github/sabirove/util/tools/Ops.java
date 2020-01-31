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

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Lambdas covering all sorts of {@link NavigableSet} APIs.
 */
public final class Ops {

    public static final List<BiFunction<NavigableSet<Integer>, Integer, Object>> VALUE_API = Arrays.asList(
            NavigableSet::add,
            NavigableSet::contains,
            NavigableSet::remove,
            NavigableSet::ceiling,
            NavigableSet::floor,
            NavigableSet::higher,
            NavigableSet::lower
    );

    public static final List<Function<NavigableSet<Integer>, Object>> QUERY_API = Arrays.asList(
            NavigableSet::pollFirst,
            NavigableSet::pollLast,
            NavigableSet::first,
            NavigableSet::last,
            NavigableSet::toArray,
            NavigableSet::isEmpty,
            NavigableSet::size,
            s -> {s.clear(); return null;}
    );

    public static final List<BiFunction<NavigableSet<Integer>, List<Integer>, Object>> BATCH_API = Arrays.asList(
            NavigableSet::addAll,
            NavigableSet::containsAll,
            NavigableSet::removeAll,
            NavigableSet::retainAll
    );
}
