[![Build Status](https://travis-ci.org/sabirove/avlset.svg?branch=master)](https://travis-ci.org/sabirove/avlset)
[![Coverage Status](https://coveralls.io/repos/github/sabirove/avlset/badge.svg)](https://coveralls.io/github/sabirove/avlset)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.sabirove/avlset/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.sabirove/avlset)


This is an AVL tree based `java.util.NavigableSet` implementation.

> The **AVL tree** is a self-balancing binary search tree named after its two Soviet inventors,
> *Georgy Adelson-Velsky* (on the right) and *Evgenii Landis* (on the left), who published it in their 1962 paper
> *"An algorithm for the organization of information"*. It was the first such data structure to be invented.
> 
> In an AVL tree, the heights of the two child subtrees of any node differ by at most one; if at any time they differ
> by more than one, rebalancing is done to restore this property. 
> Lookup, insertion, and deletion all take `O(log n)` time in both the average and worst cases, 
> where `n` is the number of nodes in the tree prior to the operation. 
> Insertions and deletions may require the tree to be rebalanced by one or more tree rotations.
> 
> AVL trees are often compared with red–black trees because both support the same set of operations
> and take `O(log n)` time for the basic operations. 
> For lookup-intensive applications, AVL trees are faster than red–black trees because they are more strictly balanced.
> Similar to red–black trees, AVL trees are height-balanced.

![](https://beeimg.com/images/x49882218863.png) ![](https://upload.wikimedia.org/wikipedia/commons/f/fd/AVL_Tree_Example.gif) ![](https://beeimg.com/images/l20024524023.png)  

source: https://en.wikipedia.org/wiki/AVL_tree

#### Compared with the reference `TreeSet` implementation

##### Memory

`java.util.TreeSet` is a red-black tree based `NavigableSet` implementation that delegates to the underlying `TreeMap`
by using it's `EntrySet` as the actual set. Map values are filled with dummy object references which yields an overhead
of extra 8 bytes for each node.

`TreeMap.Entry`
- compressed pointers: `40 bytes`  
- regular pointers: `64 bytes`  
![](https://beeimg.com/images/c65603350883.png)

`AvlSet.Node`  
- with compressed pointers: `32 bytes`  
- no compressed pointers: `56 bytes`  
![](https://beeimg.com/images/n03889651861.png)

##### Performance

Here's a quick benchmark covering the basic set of operations (`add`/`contains`/`remove`) executed against the
very same dataset of `1 million` random integers:  

![](https://beeimg.com/images/k86980947283.png)

Roughly `AvlSet` is `2 - 7`% faster across the board which is negligible. 

Benchmark parameters:
- CPU: 2,7 GHz Quad-Core Intel Core i7
- MEM: 16 GB 2133 MHz LPDDR3
- JMH params  
![](https://beeimg.com/images/s90714720152.png)  
- Source: [ReferenceBenchmark.java](src/test/java/com/github/sabirove/util/ReferenceBenchmark.java)

###### TODO
- "subset view" family of APIs (`descendingSet`, `subSet`, `tailSet`, `headSet`) is not implemented for now
- no `ConcurrentModificationException` safeguard implemented for now
