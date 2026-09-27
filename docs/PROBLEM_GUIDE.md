# JSGA Optimization Problems: Implementation and Experiment Guide

## Why these examples exist

Genetic algorithms are easy to describe and easy to misuse: a bad encoding, a
missing feasibility check, or an untested operator can silently produce
nonsense that still "looks like" a fitness curve going down. This guide's
purpose is the opposite — to show, with real running code and verified
results, exactly how a genetic algorithm should be wired to a problem, so
that someone who has never used JSGA (or genetic algorithms at all) can read
one example, understand every design decision, and confidently adapt it to
their own problem.

Fifteen classic combinatorial and continuous optimization problems are
included, spanning the representations a genetic algorithm needs to handle
well: boolean vectors (OneMax, Knapsack, Max-Cut, Set Cover, MAX-SAT, Feature
Selection), permutations (TSP, N-Queens, Job-Shop Scheduling, Vehicle
Routing), bounded real vectors (Continuous Optimization, Neural-Network
weights), per-item discrete assignments (Bin Packing, Graph Coloring), and
tree structures (Symbolic Regression). Every example is:

- **Runnable in one command** — no setup beyond building the JSGA jar.
- **Fully tunable** — every GA parameter (population size, generation limit,
  tournament size, crossover/mutation rate, random seed, number of repeated
  runs) is a plain CLI flag, so you can immediately see how changing one
  knob changes the family of solutions found.
- **Independently verified** — `ProblemCatalogVerificationTest` checks small
  instances against exhaustive search or a proven bound, so the examples are
  not just "code that runs" but code whose correctness is regression-tested.
- **A template, not a black box** — Section 3 below walks through building a
  brand-new `OptimizationProblem<S>` from scratch, using the exact same API
  every bundled example uses.

If you take away one thing from this guide: a genetic algorithm is only as
good as (1) its encoding of a candidate solution, (2) its fitness function,
and (3) its crossover/mutation operators respecting that encoding. Every
example below documents all three explicitly, so you can copy the pattern
that matches your own problem's shape (bits, permutation, real vector, or
tree) instead of starting from a blank page.

This guide explains how the 15 bundled examples turn a domain problem into a
JSGA optimization problem, how to run and tune experiments, and how to add a
different problem without changing the GA engine. The examples are deliberately
small teaching instances. They demonstrate encodings, objectives, and operators;
they are not claims that a genetic algorithm always finds a global optimum or
that the examples are ready-made production solvers.

## 1. Build and run

From the repository root, build the project and run one example:

```sh
mvn package
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain onemax
```

Run every bundled example with the same settings:

```sh
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain all
```

The runner also accepts explicit GA parameters and multiple independent runs:

```sh
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain tsp \
  --population 120 --generations 500 --tournament 3 \
  --crossover 0.9 --mutation 0.04 --seed 12345 --runs 10
```

| Option | Default | Meaning |
|---|---:|---|
| `--population N` | 80 | Number of candidate solutions per run; minimum 2 |
| `--generations N` | 150 | Generation limit; 0 reports the best initial candidate |
| `--tournament N` | 3 | Candidates sampled per parent selection; from 1 to population size |
| `--crossover RATE` | 0.9 | Probability of applying the problem's crossover operator |
| `--mutation RATE` | 0.04 | Problem-specific mutation rate; from 0 to 1 |
| `--seed N` | 12345 | Seed for the first run |
| `--runs N` | 1 | Number of independent seeded runs; each next seed is previous seed + 1 |

The output prints each run's best fitness, number of generations and evaluations,
and best solution, followed by the best, mean, and worst fitness across runs.
The "family" here is the set of best solutions from independent runs, not every
individual in every intermediate population. Fitness is minimized, so the
numerically smallest summary value is best and the largest is worst; the
maximization examples negate their underlying score. Use shell redirection to
keep the complete experiment log:

```sh
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain tsp \
  --population 120 --generations 500 --seed 12345 --runs 30 > tsp-runs.txt
```

For a controlled comparison, keep the problem data and all settings fixed and
change only one parameter at a time. Increase population size when exploration
is weak; increase generations when progress is still occurring; tune mutation
to balance exploration and preservation; compare several tournament sizes to
adjust selection pressure. Run multiple seeds and compare distributions, not
only the single best run. These are starting points, not universal optimal
settings. The same `--mutation` value has different practical effects for
different representations and operators.

## 2. What each example models

Every bundled example is registered in `ProblemCatalog`, can be run by its ID,
and has its implementation in
`src/main/java/com/rafaelgpq/jsga/optimization/problems/`. Constructors accept
the instance data so Java callers can replace the catalog's teaching data.
`ProblemCatalog` is a set of sample instances, not a required registry for
custom applications.

| ID and problem | Genome and objective | Included example and how to adapt it | Important interpretation |
|---|---|---|---|
| `onemax` — OneMax | Boolean vector; minimize negative number of `true` bits | 24 bits; use `new OneMaxProblem(bitCount)` | A simple maximization example represented as minimization. |
| `knapsack` — 0/1 Knapsack | Boolean item vector; maximize value subject to total weight capacity, using an infeasibility penalty | Five items; provide `weights[]`, `values[]`, and capacity to `new KnapsackProblem(...)` | Confirm the returned selection's total weight and value against the real constraints; penalty design affects trade-offs. |
| `tsp` — Traveling Salesman | Permutation of city indices; minimize closed Euclidean tour length; ordered crossover, swap mutation, and 2-opt improvement | Six coordinate pairs; provide `List<TspProblem.City>` to `new TspProblem(cities)` | The model is a symmetric Euclidean cycle with no time windows, precedence constraints, or vehicle restrictions. |
| `graph-coloring` — Four-color map | One of four colors per vertex; penalize conflicting edges, then prefer fewer used colors | Six vertices and an edge list; provide vertex count and `List<GraphColoringProblem.Edge>` | This objective encodes a four-color feasibility goal with a color-count preference. The demo graph gets a valid three-coloring although its minimum is two; feasibility and minimum coloring are different claims. |
| `continuous` — Continuous function optimization | Bounded real vector; minimize Sphere, Rastrigin, or Rosenbrock | Three dimensions in `[-5.12, 5.12]`; constructor takes dimensions, bounds, and `ContinuousFunctionProblem.Function` | Match bounds and scaling to the objective. Different functions and dimensions have different difficulty. |
| `n-queens` — N-Queens | Permutation of row positions, one per column; minimize diagonal attacks | Eight queens; use `new NQueensProblem(boardSize)` | Permutation encoding guarantees one queen per row and column; the objective handles diagonal conflicts. |
| `job-shop` — Job-Shop Scheduling | Permutation of operation priorities, decoded in job precedence order; minimize makespan | Three jobs and three machines; provide processing-time matrix, machine-order matrix, and machine count to `new JobShopSchedulingProblem(...)` | The decoder builds a schedule from priorities. It models the supplied routing and processing times, not setup times, calendars, or arbitrary side constraints. |
| `vrp` — Capacitated Vehicle Routing | Customer permutation; greedy capacity-based route splitting; minimize distance with a fleet-limit penalty | Five customers, vehicle capacity 6, at most two vehicles; constructor takes depot-first locations, customer demands, capacity, and vehicle limit | The split heuristic is part of the model. Check routes, capacity, fleet count, and distance before applying this model to a real VRP variant. |
| `bin-packing` — Bin Packing | One bin index per item; minimize used bins with an overflow penalty | Eight item sizes and capacity 10; provide item sizes and capacity to `new BinPackingProblem(...)` | The lower bound `ceil(total weight / capacity)` is not always attainable; verify each returned bin load. |
| `max-cut` — Maximum Cut | Boolean side assignment per vertex; minimize negative sum of edge weights crossing the cut | Six vertices and weighted edges; provide vertex count and weighted-edge list to `new MaxCutProblem(...)` | Negative output is the maximized cut weight. |
| `set-cover` — Set Cover | Boolean selection per set; minimize selected cost plus an uncovered-element penalty | Six elements and five sets; provide universe size, each set's element IDs, and set costs to `new SetCoverProblem(...)` | Verify every universe element is covered; a low objective should not replace explicit feasibility checks. |
| `max-sat` — SAT / MAX-SAT | Boolean variable assignment; minimize negative satisfied-clause count | Four variables and five clauses; each literal is a signed, one-based variable ID, e.g. `new MaxSatProblem.Clause(1, -2)` | SAT is feasible if all clauses are satisfied; MAX-SAT reports the best satisfied subset if not. |
| `feature-selection` — Feature Selection | Boolean feature mask; minimize leave-one-out 1-nearest-neighbor error with a small feature-count penalty | Six two-feature rows and binary labels; provide feature matrix and labels to `new FeatureSelectionProblem(...)` | This is a teaching objective, not a substitute for a leakage-safe train/validation/test protocol. |
| `symbolic-regression` — Symbolic Regression | Immutable expression tree; minimize training mean-squared error with a small tree-size penalty | Samples of `y=x*x`; constructor takes input and target arrays and initial/maximum depths | The grammar is single-variable and tree-based. Evaluate final expressions on held-out data and inspect tree complexity. |
| `neural-network` — Neural-Network Weight Optimization | Real-valued weights for one dense hidden layer; minimize training MSE | Four XOR samples and four hidden units; constructor takes input rows, target rows, and hidden-unit count | This optimizes weights only with a GA; it is a small training example, not a general deep-learning framework or a generalization guarantee. |

Fitness is always minimized by the generic runner. For maximization objectives
such as OneMax, Max-Cut, and MAX-SAT, the problem implementation negates the
score. Penalty-based problems require checking both objective value and domain
feasibility. The exact small-instance verification tests are useful regression
tests, but not a proof of behavior on larger or different datasets.

## 3. Reuse JSGA for a new problem

Implement `OptimizationProblem<S>` for a solution type `S` that suits the
domain. Keep candidate solutions immutable: the runner retains the elite by
reference. The problem implementation owns solution generation, validation,
fitness evaluation, crossover, and mutation; the generic runner owns parent
selection, elitism, seeded execution, and stopping at the generation limit or
when `isSolved` reports success.

Save this as `CountSelected.java` (with the JSGA jar on the compile/runtime
classpath). It shows the full wiring for a new binary maximization problem:

```java
import com.rafaelgpq.jsga.optimization.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class CountSelected implements OptimizationProblem<List<Boolean>> {
    private final int itemCount;

    CountSelected(int itemCount) {
        if (itemCount <= 0) throw new IllegalArgumentException("itemCount must be positive");
        this.itemCount = itemCount;
    }

    @Override public String name() { return "Count selected"; }

    @Override public List<Boolean> randomSolution(Random random) {
        List<Boolean> bits = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) bits.add(random.nextBoolean());
        return List.copyOf(bits);
    }

    @Override public double evaluate(List<Boolean> solution) {
        requireValid(solution);
        return -solution.stream().filter(Boolean::booleanValue).count();
    }

    @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
        requireValid(first);
        requireValid(second);
        List<Boolean> child = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) child.add(random.nextBoolean() ? first.get(i) : second.get(i));
        return List.copyOf(child);
    }

    @Override public List<Boolean> mutate(List<Boolean> solution, double rate, Random random) {
        requireValid(solution);
        List<Boolean> child = new ArrayList<>(solution);
        for (int i = 0; i < itemCount; i++) {
            if (random.nextDouble() < rate) child.set(i, !child.get(i));
        }
        return List.copyOf(child);
    }

    @Override public boolean isSolved(double fitness) { return fitness <= -itemCount; }

    public static void main(String[] args) {
        OptimizationProblem<List<Boolean>> problem = new CountSelected(40);
        OptimizationConfig config = new OptimizationConfig(100, 300, 3, 0.9, 0.025, 12345L);
        OptimizationResult<List<Boolean>> result = new GeneticAlgorithm().solve(problem, config);
        System.out.println("fitness=" + result.getBestFitness());
        System.out.println("solution=" + result.getBestSolution());
        System.out.println("evaluations=" + result.getEvaluations());
    }

    private void requireValid(List<Boolean> solution) {
        if (solution == null || solution.size() != itemCount || solution.contains(null)) {
            throw new IllegalArgumentException("Expected exactly " + itemCount + " Boolean values");
        }
    }
}
```

For a new problem, also add focused tests for known candidate scores, malformed
solutions, operator validity, constraints, deterministic seeded execution, and
small cases with an exact solver or exhaustive oracle where practical. Keep
domain parsing and data validation outside the objective where possible. Do
not return mutable shared arrays/lists from the problem.

To run a bundled class with new input data from Java, construct it directly
instead of editing `ProblemCatalog`; for example:

```java
OptimizationProblem<List<Integer>> tsp = new TspProblem(List.of(
        new TspProblem.City(0, 0),
        new TspProblem.City(3, 0),
        new TspProblem.City(3, 4),
        new TspProblem.City(0, 4)));
OptimizationResult<List<Integer>> result = new GeneticAlgorithm().solve(tsp,
        new OptimizationConfig(80, 200, 3, 0.9, 0.04, 7L));
```

## 4. Reproducibility and verification

Reproduce the shipped small-instance checks and the full unit test suite with:

```sh
mvn -Dtest=ProblemCatalogVerificationTest,OptimizationProblemsTest test
mvn test
```

`ProblemCatalogVerificationTest` compares the fixed-seed demo results with
exhaustive enumeration or a proven bound for small discrete problems and uses
explicit tolerances for continuous and learning problems. Graph coloring is
checked as a valid four-coloring, not a minimum-coloring result. The tests do
not measure generalization for machine learning, statistical robustness across
seeds, or scalability. For an experiment report, preserve the exact command,
problem data/version, all CLI settings, seed range, and every run's output.
Report the distribution across seeds and independently validate feasibility
and held-out quality before drawing conclusions.

## 5. Where to go next

- Start from the closest matching representation above (boolean vector,
  permutation, bounded real vector, or tree) and copy that example's
  `evaluate`/`crossover`/`mutate` structure for your own domain.
- Read [../README.md](../README.md) for the full JSGA architecture, the
  GAucsd-parity `MainSimulator`/`.properties` workflow, checkpoint/restart,
  diversity strategies, DPE, and per-generation reporting — everything the
  `optimization` package's generic engine builds on.
- Every example here also has a matching JUnit 5 test under
  `src/test/java/com/rafaelgpq/jsga/optimization/problems/`; reading a
  problem's implementation next to its test is the fastest way to see both
  the intended behavior and its edge cases.

JSGA aims to make genetic algorithms approachable without hiding their
pitfalls: every example is small enough to read in a few minutes, verified
against a known-correct answer, and built from the same handful of
well-tested building blocks you can reuse for the next fifteen problems you
have not thought of yet.
