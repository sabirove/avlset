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

import java.io.Serializable;
import java.util.*;

/**
 * AVL tree based {@link NavigableSet} implementation.
 */
public final class AvlSet<T extends Comparable<? super T>>
        extends AbstractCollection<T>
        implements NavigableSet<T>, Serializable {

    private final Comparator<? super T> comparator;
    private Node<T> root;
    private int size;

    private AvlSet(Comparator<? super T> comparator) {
        this.size = 0;
        this.comparator = Objects.requireNonNull(comparator, "comparator can't be null");
    }

    /*
     * AVL tree node impl.
     * note: byte is more than enough to store the height of a balanced BST.
     */
    private static final class Node<T> {
        Node<T> parent;
        Node<T> left;
        Node<T> right;
        T key;
        byte height = 1;

        Node(T key) {
            this.key = key;
        }
    }

    /*
     **************************** Factory API ******************************
     */

    /**
     * Create empty AvlSet that will use {@link Comparator#naturalOrder()} for sorting the elements.
     */
    public static <T extends Comparable<? super T>> AvlSet<T> of() {
        return new AvlSet<>(Comparator.naturalOrder());
    }

    /**
     * Create empty AvlSet that will use the specified {@link Comparator} for sorting the elements.
     */
    public static <T extends Comparable<? super T>> AvlSet<T> of(Comparator<? super T> comparator) {
        return new AvlSet<T>(comparator);
    }

    /**
     * Create AvlSet that will use {@link Comparator#naturalOrder()} for sorting the elements
     * and fill it with elements from the specified {@link Iterable}.
     * <p>If the provided {@link Iterable} happens to be an instance of {@link SortedSet}, it's
     * comparator is used instead.
     */
    public static <T extends Comparable<? super T>> AvlSet<T> of(Iterable<T> elements) {
        Comparator<? super T> comparator = elements instanceof SortedSet
                ? ((SortedSet<T>) elements).comparator()
                : Comparator.naturalOrder();
        return of(comparator, elements);
    }

    /**
     * Create AvlSet that will use {@link Comparator#naturalOrder()} for sorting the elements
     * and fill it with the specified elements.
     */
    @SafeVarargs
    public static <T extends Comparable<? super T>> AvlSet<T> of(T... elements) {
        return of(Comparator.naturalOrder(), elements);
    }

    /**
     * Create AvlSet that will use the specified {@link Comparator} for sorting the elements
     * and fill it with the specified elements.
     */
    @SafeVarargs
    public static <T extends Comparable<? super T>> AvlSet<T> of(Comparator<? super T> comparator,
                                                                 T... elements) {
        AvlSet<T> set = new AvlSet<T>(comparator);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Create AvlSet that will use the specified {@link Comparator} for sorting the elements
     * and fill it with the elements from the provided {@link Iterable}.
     */
    public static <T extends Comparable<? super T>> AvlSet<T> of(Comparator<? super T> comparator,
                                                                 Iterable<T> elements) {
        AvlSet<T> set = new AvlSet<T>(comparator);
        for (T element : elements) {
            set.add(element);
        }
        return set;
    }

    /*
     **************************** NavigableSet API ******************************
     */

    @Override
    public boolean add(T e) {
        return insert(e);
    }

    @Override
    public boolean contains(Object o) {
        return find(o) != null;
    }

    @Override
    public boolean remove(Object o) {
        Node<T> k = find(o);
        if (k != null) {
            delete(k);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public T lower(T t) {
        return getLower(t);
    }

    @Override
    public T floor(T t) {
        return getFloor(t);
    }

    @Override
    public T ceiling(T t) {
        return getCeiling(t);
    }

    @Override
    public T higher(T t) {
        return getHigher(t);
    }

    @Override
    public T pollFirst() {
        return pollKey(getFirst());
    }

    @Override
    public T pollLast() {
        return pollKey(getLast());
    }

    @Override
    public T first() {
        return getRequiredKey(getFirst());
    }

    @Override
    public T last() {
        return getRequiredKey(getLast());
    }

    @Override
    public Iterator<T> iterator() {
        return new AscKeyIterator();
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new DescKeyIterator();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean mod = false;
        for (Object o : c) {
            Node<T> n = find(o);
            if (n != null) {
                delete(n);
                mod = true;
            }
        }
        return mod;
    }

    //TODO not implemented

    @Override
    public NavigableSet<T> descendingSet() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException("not implemented");
    }


    /*
     ******************* AVL TREE ENGINE LOGIC *******************
     */

    private static <T extends Comparable<? super T>> int balance(Node<T> node) {
        return (node.right != null ? node.right.height : 0)
                - (node.left != null ? node.left.height : 0);
    }

    //remove child (leaf), return next node which is not a leaf
    private static <T extends Comparable<? super T>> Node<T> removeChildGetNextNonLeaf(Node<T> parent, Node<T> child) {
        if (child == parent.left) {
            parent.left = null;
            if (parent.right == null) {
                parent.height = 1;
                return parent.parent;
            }
        } else {
            parent.right = null;
            if (parent.left == null) {
                parent.height = 1;
                return parent.parent;
            }
        }
        return parent;
    }

    //right turn around node x
    private static <T extends Comparable<? super T>> void swapLinksRight(Node<T> x, Node<T> y) {
        x.left = y.right;
        if (x.left != null) {
            x.left.parent = x;
        }
        x.parent = y;
        y.right = x;
    }

    //left turn around node x
    private static <T extends Comparable<? super T>> void swapLinksLeft(Node<T> x, Node<T> y) {
        x.right = y.left;
        if (x.right != null) {
            x.right.parent = x;
        }
        x.parent = y;
        y.left = x;
    }

    //swap old node to the new one keeping the old parent
    private void swapSubTreeRoot(Node<T> oldNode, Node<T> newNode) {
        Node<T> parent = oldNode.parent;
        newNode.parent = parent;
        if (parent != null) {
            if (parent.right == oldNode) {
                parent.right = newNode;
            } else {
                parent.left = newNode;
            }
        } else {
            root = newNode;
        }
    }

    private void turnRightOnInsert(Node<T> A) {
        A.height--;
        Node<T> B = A.left;
        if (balance(B) == 1) {     //big right turn
            Node<T> C = B.right;
            C.height = B.height--;
            swapSubTreeRoot(A, C);
            swapLinksLeft(B, C);
            swapLinksRight(A, C);
        } else {                    //right turn
            swapSubTreeRoot(A, B);
            swapLinksRight(A, B);
        }
    }

    private void turnLeftOnInsert(Node<T> A) {
        A.height--;
        Node<T> B = A.right;
        if (balance(B) == -1) {     //big left turn
            Node<T> C = B.left;
            C.height = B.height--;
            swapSubTreeRoot(A, C);
            swapLinksRight(B, C);
            swapLinksLeft(A, C);
        } else {                    //left turn
            swapSubTreeRoot(A, B);
            swapLinksLeft(A, B);
        }
    }

    private Node<T> turnRightOnDelete(Node<T> A) {
        Node<T> B = A.left;
        switch (balance(B)) {
            case 1:                 //big right turn
                A.height -= 2;
                Node<T> C = B.right;
                C.height = B.height--;
                swapSubTreeRoot(A, C);
                swapLinksLeft(B, C);
                swapLinksRight(A, C);
                return C.parent;
            case -1:                //right turn
                A.height -= 2;
                swapSubTreeRoot(A, B);
                swapLinksRight(A, B);
                return B.parent;
            case 0:                 //right turn, return null to break the caller's loop
                A.height--;
                B.height++;
                swapSubTreeRoot(A, B);
                swapLinksRight(A, B);
                return null;
        }
        return null;
    }

    private Node<T> turnLeftOnDelete(Node<T> A) {
        Node<T> B = A.right;
        switch (balance(B)) {
            case -1:                //big left turn
                A.height -= 2;
                Node<T> C = B.left;
                C.height = B.height--;
                swapSubTreeRoot(A, C);
                swapLinksRight(B, C);
                swapLinksLeft(A, C);
                return C.parent;
            case 1:                 //left turn
                A.height -= 2;
                swapSubTreeRoot(A, B);
                swapLinksLeft(A, B);
                return B.parent;
            case 0:                 //left turn, return null to break the caller's loop
                A.height--;
                B.height++;
                swapSubTreeRoot(A, B);
                swapLinksLeft(A, B);
                return null;
        }
        return null;
    }

    private void balanceAfterInsert(Node<T> node) {
        while (node != null) {
            switch (balance(node)) {
                case -2:
                    turnRightOnInsert(node);
                    return;
                case 0:
                    return;
                case 2:
                    turnLeftOnInsert(node);
                    return;
                default:
                    node.height++;
                    node = node.parent;
            }
        }
    }

    private void balanceAfterDelete(Node<T> node) {
        while (node != null) {
            switch (balance(node)) {
                case -2:
                    node = turnRightOnDelete(node);
                    break;
                case 0:
                    node.height--;
                    node = node.parent;
                    break;
                case 2:
                    node = turnLeftOnDelete(node);
                    break;
                default:
                    return;
            }
        }
    }

    /*
     ******************* PRIMARY OPERATIONS *******************
     */

    private Node<T> find(Object key) {
        @SuppressWarnings("unchecked")
        T k = (T) key;
        Comparator<? super T> cpr = comparator;
        Node<T> node = root;
        while (node != null) {
            int cmp = cpr.compare(k, node.key);
            if (cmp < 0) {
                node = node.left;
            } else if (cmp > 0) {
                node = node.right;
            } else {
                return node;
            }
        }
        return null;
    }

    private boolean insert(T key) {
        Node<T> node = root;

        if (node != null) {
            Comparator<? super T> cpr = comparator;
            Node<T> last;
            int cmp;
            do {
                cmp = cpr.compare(key, node.key);
                if (cmp < 0) {
                    node = (last = node).left;
                } else if (cmp > 0) {
                    node = (last = node).right;
                } else {
                    return false;
                }
            } while (node != null);

            Node<T> newNode = new Node<>(key);
            newNode.parent = last;

            if (cmp > 0) {
                last.right = newNode;
            } else {
                last.left = newNode;
            }

            if (last.height == 1) {
                last.height = 2;
                balanceAfterInsert(last.parent);
            }

        } else {
            root = new Node<>(key);
        }

        size++;
        return true;
    }

    private void delete(Node<T> r) {
        Node<T> rp = r.parent;
        Node<T> rl = r.left;
        Node<T> rr = r.right;

        //node being removed is a leaf
        if (rl == null && rr == null) {
            if (rp != null) {
                rp = removeChildGetNextNonLeaf(rp, r);
            } else {
                root = null;
            }

        //node being removed has only one child (which is a leaf by default)
        } else if (rl == null || rr == null) {
            swapSubTreeRoot(r, (rl == null ? rr : rl));

        //node being removed has both child nodes
        } else {
            while (rl.right != null) {
                rl = rl.right;
            }
            r.key = rl.key; //cheat: swap the value instead of swapping all the links to move target node inplace
            rp = rl.parent;
            if (rl.left != null) {
                swapSubTreeRoot(rl, rl.left);
            } else {
                rp = removeChildGetNextNonLeaf(rp, rl);
            }
        }

        balanceAfterDelete(rp);
        size--;
    }

    /*
     ******************* SECONDARY OPERATIONS LOGIC *******************
     */

    private static <T extends Comparable<? super T>> Node<T> getNext(Node<T> node) {
        if (node.right != null) {
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }
        Node<T> parent = node.parent;
        while (parent != null && parent.left != node) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    private static <T extends Comparable<? super T>> Node<T> getPrevious(Node<T> node) {
        if (node.left != null) {
            node = node.left;
            while (node.right != null) {
                node = node.right;
            }
            return node;
        }
        Node<T> parent = node.parent;
        while (parent != null && parent.right != node) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    private T getLower(T key) {
        Node<T> node = root;
        T target = null;
        Comparator<? super T> cpr = comparator;
        while (node != null) {
            if (cpr.compare(key, node.key) > 0) {
                target = node.key;
                node = node.right;
            } else {
                node = node.left;
            }
        }
        return target;
    }

    private T getHigher(T key) {
        Node<T> node = root;
        T target = null;
        Comparator<? super T> cpr = comparator;
        while (node != null) {
            if (cpr.compare(key, node.key) < 0) {
                target = node.key;
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return target;
    }

    private T getFloor(T key) {
        Node<T> node = root;
        T target = null;
        Comparator<? super T> cpr = comparator;
        while (node != null) {
            T t = node.key;
            int cmp = cpr.compare(key, t);
            if (cmp < 0) {
                node = node.left;
            } else if (cmp > 0) {
                target = t;
                node = node.right;
            } else {
                return t;
            }
        }
        return target;
    }

    private T getCeiling(T key) {
        Node<T> node = root;
        T target = null;
        Comparator<? super T> cpr = comparator;
        while (node != null) {
            T t = node.key;
            int cmp = cpr.compare(key, t);
            if (cmp < 0) {
                target = t;
                node = node.left;
            } else if (cmp > 0) {
                node = node.right;
            } else {
                return t;
            }
        }
        return target;
    }

    private Node<T> getFirst() {
        Node<T> k = root;
        if (k != null) {
            while (k.left != null) {
                k = k.left;
            }
        }
        return k;
    }

    private Node<T> getLast() {
        Node<T> k = root;
        if (k != null) {
            while (k.right != null) {
                k = k.right;
            }
        }
        return k;
    }

    private T pollKey(Node<T> node) {
        if (node != null) {
            T key = node.key;
            delete(node);
            return key;
        }
        return null;
    }

    private static <T extends Comparable<? super T>> T getRequiredKey(Node<T> node) {
        if (node != null) {
            return node.key;
        }
        throw new NoSuchElementException();
    }

    /*
     **************************** ITERATOR IMPL ******************************
     */

    private abstract class KeyIterator implements Iterator<T> {
        Node<T> next;
        Node<T> current;

        KeyIterator(Node<T> next) {
            this.next = next;
        }

        @Override
        public final boolean hasNext() {
            return next != null;
        }

        @Override
        public final T next() {
            if (next != null) {
                current = next;
                next = getNext();
                return current.key;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public final void remove() {
            if (current != null) {
                delete(current);
                afterDelete();
                current = null;
            } else {
                throw new NoSuchElementException();
            }
        }

        abstract Node<T> getNext();

        void afterDelete() {
        }
    }

    private final class AscKeyIterator extends KeyIterator {
        AscKeyIterator() {
            super(getFirst());
        }

        @Override
        Node<T> getNext() {
            return AvlSet.getNext(next);
        }
    }

    private final class DescKeyIterator extends KeyIterator {
        DescKeyIterator() {
            super(getLast());
        }

        @Override
        Node<T> getNext() {
            return getPrevious(next);
        }

        @Override
        void afterDelete() {
            if (next != null && current.key.equals(next.key)) {
                // required for DESC iteration because of the cheat in .delete(Node<T> r)
                // with swapping key from the predecessor node which affects
                // the ongoing getPrevious() traversal
                next = current;
            }
        }
    }

    private static final long serialVersionUID = 1677359823482349238L;
}
