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

import com.github.sabirove.util.tools.AvlSpec;
import com.github.sabirove.util.tools.Ops;
import com.github.sabirove.util.tools.Rnd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Randomized tests against the reference {@link TreeSet} impl.
 */
class ReferenceTest {
    private TreeSet<Integer> ref;
    private AvlSet<Integer> avl;

    @BeforeEach
    void before() {
        Comparator<Integer> cmp = Rnd.oneOf(Comparator.naturalOrder(), Comparator.reverseOrder());
        List<Integer> values = Rnd.generateValues(1000);
        ref = new TreeSet<>(cmp);
        ref.addAll(values);
        avl = AvlSet.of(cmp, values);
        assertEquals(ref.size(), avl.size());
        AvlSpec.test(avl);
    }

    @RepeatedTest(1000)
    void apiTest() {
        for (int i = 0; i < 1000; i++) {
            int r = (int) (System.nanoTime() % 10);

            if (r < 5) { // ~ probability 5/9
                BiFunction<NavigableSet<Integer>, Integer, Object> op = Rnd.oneOf(Ops.VALUE_API);
                int value = Rnd.generateValue();
                assertEqualsOrThrowSame(() -> op.apply(ref, value), () -> op.apply(avl, value));

            } else if (r < 8) { // ~ probability 3/9
                Function<NavigableSet<Integer>, Object> op = Rnd.oneOf(Ops.QUERY_API);
                assertEqualsOrThrowSame(() -> op.apply(ref), () -> op.apply(avl));

            } else {    // ~ probability 2/9
                BiFunction<NavigableSet<Integer>, List<Integer>, Object> op = Rnd.oneOf(Ops.BATCH_API);
                List<Integer> values = Rnd.generateValues(Rnd.rndInt(0, 100));
                assertEqualsOrThrowSame(() -> op.apply(ref, values), () -> op.apply(avl, values));
            }

            assertEquals(ref.size(), avl.size());
            AvlSpec.test(avl);
        }
    }

    @RepeatedTest(100)
    void iteratorTest() {
        boolean asc = Rnd.rollTheDice();
        Iterator<Integer> refIt = asc ? ref.iterator() : ref.descendingIterator();
        Iterator<Integer> avlIt = asc ? avl.iterator() : avl.descendingIterator();

        while (refIt.hasNext()) {
            assertEquals(refIt.next(), avlIt.next());

            if (Rnd.rollTheDice()) {
                refIt.remove();
                avlIt.remove();
                assertEquals(ref.size(), avl.size());
                AvlSpec.test(avl);
            }
        }

        assertFalse(avlIt.hasNext());
        assertThrows(NoSuchElementException.class, refIt::next);
        assertThrows(NoSuchElementException.class, avlIt::next);
    }

    private static void assertEqualsOrThrowSame(Supplier<Object> o1, Supplier<Object> o2) {
        Object r1 = null;
        Exception e1 = null;
        try {
            r1 = o1.get();
        } catch (Exception e) {
            e1 = e;
        }

        Object r2 = null;
        Exception e2 = null;
        try {
            r2 = o2.get();
        } catch (Exception e) {
            e2 = e;
        }

        if (e1 == null) {
            assertNull(e2);
            if (r1 == null) {
                assertNull(r2);
            } else if (r1.getClass().isArray()) {
                assertArrayEquals((Object[]) r1, (Object[]) r2);
            } else {
                assertEquals(r1, r2);
            }
        } else {
            assertNotNull(e2);
            assertSame(e1.getClass(), e2.getClass());
        }
    }
}
