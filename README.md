# JSGA — A Modern Java Genetic Algorithm Framework

[![Build Status](https://github.com/rafaelgpq/JSGA/actions/workflows/ci.yml/badge.svg)](https://github.com/rafaelgpq/JSGA/actions/workflows/ci.yml)

**JSGA** ("Java Simple Genetic Algorithm") is a faithful, well-tested Java port
and modernization of **GAucsd 1.4**, the classic C genetic algorithm toolkit
written by John Grefenstette and later maintained by Nicol Schraudolph at
UC San Diego. Where GAucsd was a single-purpose research tool tied to global C
variables, hand-rolled random number generators, and platform-specific file
formats, JSGA re-expresses the *same proven algorithms* — selection, crossover,
mutation, elitism, gap replacement, convergence detection, checkpoint/restart,
distributed parameter encoding (DPE), schema analysis, and per-generation
reporting — as clean, dependency-injectable, unit-tested Java classes that any
modern JVM project can embed, extend, or learn from.

## Why JSGA?

- **Battle-tested algorithms, modern engineering.** Every GAucsd behavior
  (gap sizing, tournament fractions, adaptive mutation, Gray-coded DPE
  segments, `report.c`-style output columns) was cross-checked against the
  original C source and is covered by regression tests — so you get
  decades-proven GA theory without inheriting 1990s C quirks.
- **Batteries included.** 15 ready-to-run example problems (see
  [docs/PROBLEM_GUIDE.md](docs/PROBLEM_GUIDE.md)) show OneMax, Knapsack, TSP,
  Graph Coloring, Continuous Optimization, N-Queens, Job-Shop Scheduling,
  Vehicle Routing, Bin Packing, Max-Cut, Set Cover, SAT/MAX-SAT, Feature
  Selection, Symbolic Regression, and Neural-Network weight training —
  solved end-to-end with JSGA, with clear, adaptable code for your own
  problems.
- **Two APIs, two engines (for now).** Use the modern, generic
  `OptimizationProblem<S>` interface with the lightweight `GeneticAlgorithm`
  engine to plug in *any* solution representation (bit strings, permutations,
  real vectors, trees) in a few methods — or drive the classic GAucsd-parity
  `MainSimulator` from a `.properties` file with no code at all. These are
  currently two independent evolutionary loops (`optimization.GeneticAlgorithm`
  and `MainSimulator`); unifying them behind one reusable engine is tracked as
  follow-up work.
- **Production-friendly.** Deterministic seeded execution, checkpoint/restart
  for long runs, optional diversity strategies (random immigrants, crowding,
  island migration, fitness sharing) to fight premature convergence, schema
  analysis for research, and a report pipeline for tracking convergence over
  time.
- **Extensively tested.** 250+ JUnit 5 tests, ~98% line and ~90% branch
  coverage (see `mvn verify` below), including small-instance
  exhaustive-search verification for every bundled example problem.
- **Approachable by design.** Clear package boundaries
  (`config`, `core`, `selection`, `recombine`, `diversity`, `dpe`,
  `checkpoint`, `report`, `analysis`, `schema`, `optimization`), consistent
  naming, and documentation written to be read by someone who has never seen
  GAucsd or JSGA before.

Whether you are studying classic genetic algorithms, need a dependable GA
building block for a Java application, or want a worked example of porting a
legacy scientific C codebase to idiomatic, testable Java — JSGA is built to be
read, run, and extended with confidence.

## License and attribution

JSGA's own Java source is released under the [MIT License](LICENSE). JSGA is
an independent, from-scratch Java implementation informed by and functionally
modeled on **GAucsd 1.4** (1993), originally developed as **GENESIS** by John
J. Grefenstette and later extended/maintained as GAucsd by Nicol N.
Schraudolph at UC San Diego (source: https://nic.schraudolph.org/pubs/GAucsd.sh.gz).
No original GAucsd/GENESIS C source is redistributed here — every behavior
was re-implemented in Java and independently unit-tested. See
[LICENSE](LICENSE) for the full attribution and provenance notice, including
a note on GAucsd's own (informal, non-SPDX) distribution terms.

## Run

Run with the default `jsga.properties` file, or pass a properties-file path:

```sh
mvn package
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.MainSimulator path/to/config.properties
```

The seed, population size, gene length, generation limit, operator types, and
rates are read from the selected properties file. Invalid values are rejected
before the simulation starts. Population size, gene length, and generation
limit must be positive; crossover and mutation rates must be between 0 and 1.
The configured seed is used by initialization and the genetic operators.
`max.generations` is the hard generation cap. Optional early termination can
also use `termination.trial_limit` (objective evaluations) or
`termination.stagnation.generations`; both default to `0` (disabled).
When `convergence.enabled=true`, `convergence.maxconv` enables fitness
stagnation termination after that many consecutive generations without a
fitness improvement; `0` disables that check. `convergence.threshold`
specifies the maximum absolute fitness change treated as stagnation.
`gap.size` controls the fraction replaced by selected offspring each generation
and defaults to `1.0`; the replacement count truncates `gap.size * population.size`
to match GAucsd. Unselected slots carry forward randomly sampled parent survivors.
Tournament `selection.parameter` is a tournament-size fraction of the source
population and must be in `(0, 1]`.

Population initialization defaults to `random`. To start from specified
genomes, set `initialization.type=manual` and provide comma-separated binary
strings in `initialization.genes`; every string must match `gene.length`.

Binary operators support `onepoint`, `twopoint`, and `uniform` crossover, and
`bitflip`, `adaptivebitflip`, `swap`, and `gaussian` mutation. Crossover acts
only at valid logical bit positions, including for gene lengths that are not a
multiple of eight. The mutation rate is a per-bit probability gate for
`bitflip`, `adaptivebitflip`, and `gaussian` (Gaussian mutation also requires
its perturbation to exceed the flip threshold); `swap` applies the rate once
per individual. The `mutation.adaptive` setting adjusts the rate only for
`adaptivebitflip`, capped at 1. Changed genomes are marked for re-evaluation,
while unchanged clean individuals retain their cached fitness.

Optional diversity controls are selected with `diversity.enabled` and
`diversity.strategy`. Random immigrants and consensus-based crowding replace
individuals with seeded random genomes; the island strategy periodically
migrates the best individuals around a ring of islands. Fitness sharing uses
raw minimization objective values and logical-bit Hamming-distance niches;
crowded niches move adjusted scores toward the population's worst score.
Adjusted scores are recomputed without repeating objective evaluations. Set
`schema.enabled=true` with `schema.file` and `schema.worst_fitness` to record
schema counts; results go to `<schema.file>.analysis` unless `schema.output`
is supplied. The schema source file is never overwritten.

Packed genomes store the first logical bit in the least-significant bit of the
first byte. `CheckpointWriter` and `CheckpointReader` persist and restore
population genomes, fitness, raw fitness, and evaluation flags; older
three-field population records remain readable. `ReportGenerator` summarizes
the first nine numeric columns of whitespace-separated report data and rejects
malformed or non-finite data rows.

## Problem library and demonstrations

The reusable `OptimizationProblem<S>` API lets each problem define its own
immutable solution representation, objective, crossover, and mutation. The
generic GA runner minimizes fitness, uses tournament selection and elitism,
and uses a local seeded random generator. Run one reproducible demonstration
or all bundled examples with:

```sh
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain onemax
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain all \
  --generations 150 --seed 12345
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain tsp \
  --population 120 --generations 500 --tournament 3 \
  --crossover 0.9 --mutation 0.04 --seed 12345 --runs 10
```

`ProblemDemoMain [problem-id|all]` uses small included instances. Named
parameters control population size, generation limit, tournament size,
crossover and mutation rates, random seed, and number of independent runs.
Repeated runs use consecutive seeds and print each best solution plus best,
mean, and worst fitness. The IDs and their representations are:

| ID | Problem | Representation / operators |
|---|---|---|
| `onemax` | OneMax | Boolean vector; uniform crossover and bit flips |
| `knapsack` | 0/1 Knapsack | Boolean item selection; infeasible-weight penalty |
| `tsp` | Traveling Salesman | City permutation; ordered crossover, swaps, and 2-opt local improvement |
| `graph-coloring` | Four-color graph coloring | Per-vertex colors; uniform crossover and recoloring |
| `continuous` | Continuous optimization | Bounded real vector; blend crossover and Gaussian mutation |
| `n-queens` | N-Queens | Row permutation; ordered crossover and swaps |
| `job-shop` | Job-shop scheduling | Operation-priority permutation; precedence-aware schedule decoding |
| `vrp` | Capacitated vehicle routing | Customer permutation; capacity-based route splitting |
| `bin-packing` | Bin packing | Item-to-bin assignment; overflow penalty |
| `max-cut` | Maximum Cut | Boolean partition; uniform crossover and bit flips |
| `set-cover` | Set Cover | Boolean subset selection; uncovered-element penalty |
| `max-sat` | SAT / MAX-SAT | Boolean assignment; maximizes satisfied clauses |
| `feature-selection` | Feature Selection | Boolean feature mask; leave-one-out 1-NN objective |
| `symbolic-regression` | Symbolic Regression | Expression tree; subtree crossover and mutation |
| `neural-network` | Neural-network weights | Bounded real weights; dense one-hidden-layer network and MSE |

Problems can also be constructed and solved directly from Java:

```java
OptimizationProblem<List<Boolean>> problem = new OneMaxProblem(64);
OptimizationConfig settings = new OptimizationConfig(100, 300, 3, 0.9, 0.02, 12345L);
OptimizationResult<List<Boolean>> result = new GeneticAlgorithm().solve(problem, settings);
System.out.println(result.getBestFitness() + " " + result.getBestSolution());
```

See [docs/PROBLEM_GUIDE.md](docs/PROBLEM_GUIDE.md) for an end-to-end tutorial,
problem-by-problem formulations and adaptation points, CLI option reference,
parameter-tuning workflow, reproducible multi-seed experiments, and a complete
custom `OptimizationProblem` example.

The legacy `MainSimulator` accepts problem classes implementing
`com.rafaelgpq.jsga.problem.Problem`. Parameterized objectives implement
`ParameterizedProblem` and require `dpe.enabled=true` plus comma-separated
`dpe.positions`, `dpe.factors`, and `dpe.bases`. Position entries are absolute
segment endpoints; a negative endpoint disables DPE adaptation for that
parameter. `dpe.gray=true` decodes Gray-coded parameter segments. DPE evaluates
real parameter vectors while the included OneMax problem continues to evaluate
logical bits. The problem demos are heuristic examples, not guarantees of a
global optimum; production instances should be supplied with domain-specific
data and tuned settings. Verify the included small instances with:

```sh
mvn -Dtest=ProblemCatalogVerificationTest,OptimizationProblemsTest test
mvn package
java -cp target/jsga-1.0-SNAPSHOT.jar com.rafaelgpq.jsga.optimization.ProblemDemoMain all \
  --generations 150 --seed 12345
```

The verification test compares seeded runs against exhaustive enumeration or
a proven lower bound for the small discrete instances; continuous, symbolic
regression, and neural-network examples use explicit objective tolerances.
For the catalog graph, the run returns a valid three-coloring, while exhaustive
enumeration finds that this particular bipartite graph is two-colorable. This
is feasible but not the minimum-color assignment. Passing these checks verifies
the catalog examples only; it does not establish global-optimum guarantees or
statistical robustness on other seeds or real-world instances.

Generate the JaCoCo test coverage report with:

```sh
mvn verify
```

The HTML report is written to `target/site/jacoco/index.html`; XML coverage
data is available at `target/site/jacoco/jacoco.xml`.

The legacy simulator can periodically save evaluated population checkpoints
with `checkpoint.enabled=true`, `checkpoint.file`, and
`checkpoint.interval`. Resume by setting `restart.enabled=true` and
`restart.file` to a checkpoint; `max.generations` remains the total generation
limit, and the saved generation number is continued. Restart restores genomes,
fitness, raw fitness, evaluation flags, evaluation count, and JSGA's seeded
random-generator state, including a cached Gaussian value. With the same
configuration and runtime version, checkpoint/resume continues the same
stochastic sequence. Restart is rejected with DPE, schema tracking, or
stagnation-based early stopping because their adaptive/history state is not
part of the simulator checkpoint format.

Setting `report.enabled=true` makes the simulator append one performance row
per `report.interval` generations to `report.file` (default `ga_output.txt`),
mirroring GAucsd's `measure.c`/`report.c` output: `Gen Trials Lost Conv Bias
Online Offline Best Average`. `Lost`, `Conv`, and `Bias` reflect the
convergence analysis and are always zero when `convergence.enabled=false`,
just as GAucsd reports zero when its `Convflag` is off. Summarize one or more
report files with `com.rafaelgpq.jsga.report.ReportGenerator`, which computes
the per-column mean and sample variance, the same statistics GAucsd's
`report` utility produces.

After the run completes, `MainSimulator` prints a ranked "best solutions"
summary drawn from a `BestSetManager` archive maintained across every
generation (not just the final population): the top individuals ever seen,
each with its chromosome, fitness, and the generation/trial where it was
found, followed by the single overall best ("champion"). JSGA minimizes
fitness internally, so the numerically smallest (most negative) value is
always the best solution, even for problems phrased as maximization (they
negate their score before reporting it).

## Architecture: how GAucsd maps onto JSGA

JSGA was built by porting GAucsd feature-by-feature, in five phases, each
verified against the original C source and covered by unit tests:

| Phase | Focus | GAucsd source | JSGA packages |
|---|---|---|---|
| 1 | Configuration & initialization | `global.h`, `define.h`, `format.h`, `init.c`, `generate.c`, `setflag.c`, `input.c` | `config`, `init`, `util.InputPrinter` |
| 2 | Core GA loop & elitism | `main.c`, `evaluate.c`, `elitist.c`, `best.c`, `checkpt.c`, `restart.c` | `MainSimulator`, `elitism`, `checkpoint`, `evaluation`, `track` |
| 3 | Genetic operators | `cross.c`, `mutate.c` | `recombine.crossover`, `recombine.mutation`, `core.Recombination` |
| 4 | Selection & diversity | `select.c`, `gap.c`, `measure.c`, `schema.c` | `selection` (including `selection.GapHandler`), `measure`, `schema`, `diversity` |
| 5 | Advanced features | `dpe.c`, `random.c`, `decode.c`/`encode.c`, `report.c`, `converge.c`, `done.c` | `dpe`, `util.RandomUtils`, `util.DecodeUtils`/`EncodeUtils`, `report`, `analysis.ConvergenceChecker`, `core.DoneChecker` |

The reusable `optimization` package (the `OptimizationProblem<S>`/
`GeneticAlgorithm` API and the 15 example problems) is a JSGA-native addition
built on top of this ported core. It is a separate, independent evolutionary
loop from `MainSimulator` — not the same engine — that reuses the same GA
theory to solve problems GAucsd itself never targeted — permutations, trees,
and real-valued vectors — without any C equivalent to port from. Unifying
both loops behind one shared engine is planned follow-up work.

## Learn more

- [docs/PROBLEM_GUIDE.md](docs/PROBLEM_GUIDE.md) — an end-to-end tutorial for
  the 15 bundled example problems: how to run them, how to read and tune
  every parameter, how to reproduce experiments, and how to plug in your own
  problem using the same API.
- Run `mvn test` to execute the full JUnit 5 suite, or `mvn verify` for tests
  plus a JaCoCo coverage report.
- Every package under `src/main/java/com/rafaelgpq/jsga` has a matching test
  package under `src/test/java/com/rafaelgpq/jsga` — reading the tests
  alongside the source is the fastest way to understand expected behavior.