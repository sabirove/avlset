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

import com.github.sabirove.util.AvlSet;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AVL/BST spec validator featuring complete all-in-one recursive integrity test.
 *
 * Each node is tested for:
 * - node.parent, node.left and node.right references integrity
 * - node.height field against real calculated height value
 * - calculated node balance against AVL BST spec
 * - key ordering against BST spec
 */
public final class AvlSpec {
    private AvlSpec() { }

    public static void test(AvlSet<?> avlSet) {
        try {
            Object root = Accessor.root(avlSet);
            @SuppressWarnings("unchecked")
            Comparator<Object> cpr = (Comparator<Object>) Accessor.comparator(avlSet);
            assertNotNull(cpr);
            if (!avlSet.isEmpty()) {
                assertNull(Accessor.parent(root));
                checkIntegrity(root, cpr);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //recursive stack will be small when dealing with balanced BST
    private static byte checkIntegrity(Object node, Comparator<Object> cpr) throws IllegalAccessException {
        Integer key = Accessor.key(node);

        Object left = Accessor.left(node);
        byte lh = 0;
        if (left != null) {
            int cmp = cpr.compare(key, Accessor.key(left));
            assertTrue(cmp > 0);
            assertSame(node, Accessor.parent(left));
            lh = checkIntegrity(left, cpr);
        }

        Object right = Accessor.right(node);
        byte rh = 0;
        if (right != null) {
            int cmp = cpr.compare(key, Accessor.key(right));
            assertTrue(cmp < 0);
            assertSame(node, Accessor.parent(right));
            rh = checkIntegrity(right, cpr);
        }

        byte balance = (byte) (rh - lh);
        assertTrue(Math.abs(balance) < 2);

        byte calculatedHeight = (byte) (1 + (lh > rh ? lh : rh));
        byte actualHeight = Accessor.height(node);
        assertEquals(calculatedHeight, actualHeight);
        return calculatedHeight;
    }

    /*
     * Introspection
     */
    private static final class Accessor {
        private static final Field root;
        private static final Field comparator;
        private static final Field key;
        private static final Field height;
        private static final Field left;
        private static final Field right;
        private static final Field parent;

        static Object root(AvlSet<?> avlSet) throws IllegalAccessException {
            return root.get(avlSet);
        }

        static Object comparator(AvlSet<?> avlSet) throws IllegalAccessException {
            return comparator.get(avlSet);
        }

        @SuppressWarnings("unchecked")
        static <T> T key(Object node) throws IllegalAccessException {
            return (T) key.get(node);
        }

        static byte height(Object node) throws IllegalAccessException {
            return (byte) height.get(node);
        }

        static Object left(Object node) throws IllegalAccessException {
            return left.get(node);
        }

        static Object right(Object node) throws IllegalAccessException {
            return right.get(node);
        }

        static Object parent(Object node) throws IllegalAccessException {
            return parent.get(node);
        }

        static {
            try {
                root = AvlSet.class.getDeclaredField("root");
                comparator = AvlSet.class.getDeclaredField("comparator");

                Class<?> nodeClass = Stream.of(AvlSet.class.getDeclaredClasses())
                        .filter(c -> c.getName().endsWith("$Node"))
                        .findFirst().get();

                key = nodeClass.getDeclaredField("key");
                height = nodeClass.getDeclaredField("height");
                left = nodeClass.getDeclaredField("left");
                right = nodeClass.getDeclaredField("right");
                parent = nodeClass.getDeclaredField("parent");

                Stream.of(root, comparator, key, height, left, right, parent)
                        .forEach(f -> f.setAccessible(true));

            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
