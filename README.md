#Assignment 4 — Directed Graph Algorithms (SCC, Condensation DAG, Topo Sort, Shortest & Longest Paths)#

This project implements and analyzes several core algorithms for directed weighted graphs:

* Tarjan’s Strongly Connected Components (SCC)
* Condensation DAG construction
* Topological Sorting (Kahn’s algorithm)
* Shortest Path in DAG
* Longest Path (Critical Path) in DAG

Each algorithm is tested on 9 datasets (small / medium / large) with different densities and structures.
Results include execution time, operation counts, and graph structural metrics.



## 📊 Data Summary ##

| Dataset | Vertices | Edges | Density | Type              | Weight Model |
| ------- | -------- | ----- | ------- | ----------------- | ------------ |
| small1  | 6        | 6     | 0.30    | DAG               | edge         |
| small2  | 8        | 8     | 0.40    | Cyclic            | edge         |
| small3  | 9        | 9     | 0.45    | Mixed (SCC + DAG) | edge         |
| medium1 | 12       | 11    | 0.20    | DAG               | edge         |
| medium2 | 15       | 15    | 0.30    | Cyclic            | edge         |
| medium3 | 18       | 17    | 0.35    | Mixed             | edge         |
| large1  | 25       | 21    | 0.20    | Sparse Mixed      | edge         |
| large2  | 35       | 30    | 0.40    | Dense DAG         | edge         |
| large3  | 45       | 44    | 0.50    | Dense Cyclic      | edge         |

All datasets follow the format:
```
{
  "directed": true,
  "n": <vertex_count>,
  "edges": [{"u": 0, "v": 1, "w": 3}, ...],
  "source": <start_vertex>,
  "weight_model": "edge"
}
```


## ⚙️ Implementation Details ##

| Algorithm                        | Class              | Core Idea                      | Complexity |
| -------------------------------- | ------------------ | ------------------------------ | ---------- |
| **Tarjan SCC**                   | `TarjanSCC`        | DFS low-link method            | O(V + E)   |
| **Condensation**                 | `Condensation`     | Merge each SCC → new node      | O(V + E)   |
| **Topological Sort**             | `TopoSorter`       | Kahn’s queue-based             | O(V + E)   |
| **Shortest Path (DAG)**          | `DagShortestPaths` | Relax edges in topo order      | O(V + E)   |
| **Longest Path (Critical Path)** | `DagLongestPath`   | Maximize weights in topo order | O(V + E)   |

Results Summaries can be found in `/results` folder

## 🔍 Analysis ##
🔸 SCC Computation
* Tarjan’s algorithm scales linearly with edges; very efficient even for large cyclic graphs.
* Bottleneck: recursion depth and stack management for dense strongly connected components.
* Graphs with many small SCCs (DAG-like) run fastest; dense cycles slow down due to repeated backtracking.

🔸 Condensation + Topological Sort
* Condensation DAG size equals number of SCCs → smaller for cyclic graphs (many nodes merge).
* TopoSort performance remains O(V + E); almost negligible in total runtime.
* Bottleneck: verifying zero-in-degree list for large DAGs (medium overhead for large2).

🔸 DAG Shortest / Longest Paths
* On DAGs, both run in linear time over edges.
* Structure sensitivity:
  * Sparse DAG → fast but limited path variety.
  * Dense DAG → more relaxations, higher op count.
  * Cyclic graphs require condensation first → extra overhead.
* Longest path cost grows with DAG depth (large2 > small1).

## 📈 Observations & Insights ##
* Density ↑ → runtime ↑ due to more edge relaxations.
* SCC merging drastically reduces node count for cyclic graphs (e.g., large3 shrinks from 45 → 9 components).
* Shortest vs Longest paths: almost identical time for similar DAG sizes — difference is constant-factor.
* DAG topological structure dominates performance more than absolute node count.


## Conclusions & Recommendations ##

| Scenario                         | Recommended Algorithm       | Reason                              |
| -------------------------------- | --------------------------- | ----------------------------------- |
| Large acyclic network            | DAG Shortest / Longest Path | Linear performance                  |
| Dense cyclic graph               | Tarjan SCC + Condensation   | Prevents infinite cycles            |
| Mixed graph with moderate cycles | Condensation → DAG SP       | Balances accuracy + speed           |
| Real-time or large-scale systems | Tarjan SCC                  | Most stable linear-time performance |


### Practical tips:
* Always run SCC detection first on unknown graphs.
* Reuse the condensation DAG for both shortest and longest path computations.
* For dense graphs, prefer iterative SCC implementations to avoid deep recursion.
* For reporting, record both component count and critical path length to understand graph structure.


### Project Structure
```
src/main/java/graph/
 ├─ model/Edge.java
 ├─ scc/TarjanSCC.java
 ├─ condense/Condensation.java
 ├─ topo/TopoSorter.java
 ├─ dag/DagShortestPaths.java
 ├─ dag/DagLongestPath.java
 ├─ util/SimpleMetrics.java
 └─ Main.java
data/
 ├─ small1.json … large3.json
results/
 ├─ result_small1.txt … result_large3.txt

```

### How to Run
```
# Compile
javac -d out $(find src/main/java -name "*.java")

# Execute pipeline for all datasets
java -cp out graph.Main

# Check results
cat results/result_small1.txt

```
