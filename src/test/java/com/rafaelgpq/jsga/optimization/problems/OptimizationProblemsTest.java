package com.rafaelgpq.jsga.optimization.problems;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimizationProblemsTest {

    @Test
    void binaryAndConstraintBenchmarksScoreFeasibleSolutions() {
        OneMaxProblem oneMax = new OneMaxProblem(4);
        assertThat(oneMax.evaluate(List.of(true, true, true, true))).isEqualTo(-4);
        assertThat(oneMax.isSolved(-4)).isTrue();

        KnapsackProblem knapsack = new KnapsackProblem(new int[]{2, 3}, new double[]{3, 5}, 3);
        assertThat(knapsack.evaluate(List.of(false, true))).isEqualTo(-5);
        assertThat(knapsack.evaluate(List.of(true, true))).isGreaterThan(-5);

        SetCoverProblem cover = new SetCoverProblem(3, List.of(List.of(0, 1), List.of(1, 2)),
                new double[]{2, 3});
        assertThat(cover.evaluate(List.of(true, true))).isEqualTo(5);

        MaxSatProblem maxSat = new MaxSatProblem(2, List.of(
                new MaxSatProblem.Clause(1), new MaxSatProblem.Clause(-1, 2)));
        assertThat(maxSat.evaluate(List.of(true, false))).isEqualTo(-1);
    }

    @Test
    void permutationAndGraphProblemsKeepTheirSolutionsValid() {
        NQueensProblem queens = new NQueensProblem(8);
        assertThat(queens.evaluate(List.of(0, 4, 7, 5, 2, 6, 1, 3))).isZero();
        assertThat(queens.isSolved(0)).isTrue();

        TspProblem tsp = new TspProblem(List.of(new TspProblem.City(0, 0),
                new TspProblem.City(1, 0), new TspProblem.City(0, 1)));
        assertThat(tsp.evaluate(List.of(0, 1, 2))).isEqualTo(2.0 + Math.sqrt(2.0));
        assertThatThrownBy(() -> tsp.evaluate(List.of(0, 0, 2)))
                .isInstanceOf(IllegalArgumentException.class);

        TspProblem square = new TspProblem(List.of(new TspProblem.City(0, 0),
                new TspProblem.City(1, 0), new TspProblem.City(1, 1),
                new TspProblem.City(0, 1)));
        List<Integer> uncrossed = square.mutate(List.of(0, 2, 1, 3), 0.0, new java.util.Random(4));
        assertThat(square.evaluate(uncrossed)).isEqualTo(4.0);

        GraphColoringProblem coloring = new GraphColoringProblem(3, List.of(
                new GraphColoringProblem.Edge(0, 1),
                new GraphColoringProblem.Edge(1, 2),
                new GraphColoringProblem.Edge(2, 0)));
        assertThat(coloring.evaluate(List.of(0, 1, 2))).isEqualTo(3);
        assertThat(coloring.isSolved(3)).isTrue();
    }

    @Test
    void graphColoringValidatesGraphAndColoringsAndKeepsVariationInRange() {
        assertThatThrownBy(() -> new GraphColoringProblem(0, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GraphColoringProblem(2, null))
                .isInstanceOf(IllegalArgumentException.class);
        for (GraphColoringProblem.Edge invalid : List.of(
                new GraphColoringProblem.Edge(-1, 0),
                new GraphColoringProblem.Edge(0, 2),
                new GraphColoringProblem.Edge(1, 1))) {
            assertThatThrownBy(() -> new GraphColoringProblem(2, List.of(invalid)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new GraphColoringProblem(2,
                java.util.Arrays.asList((GraphColoringProblem.Edge) null)))
                .isInstanceOf(IllegalArgumentException.class);

        GraphColoringProblem.Edge edge = new GraphColoringProblem.Edge(0, 1);
        assertThat(edge.getFirst()).isZero();
        assertThat(edge.getSecond()).isEqualTo(1);
        GraphColoringProblem problem = new GraphColoringProblem(3, List.of(edge));
        java.util.Random random = new java.util.Random(19);
        List<Integer> crossover = problem.crossover(List.of(0, 1, 2), List.of(3, 2, 1), random);
        List<Integer> mutant = problem.mutate(List.of(0, 1, 2), 1.0, random);
        assertThat(crossover).hasSize(3).allMatch(color -> color >= 0 && color < 4);
        assertThat(mutant).hasSize(3).allMatch(color -> color >= 0 && color < 4);
        assertThat(problem.randomSolution(random)).hasSize(3);
        assertThatThrownBy(() -> problem.evaluate(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.evaluate(List.of(0, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.evaluate(java.util.Arrays.asList(0, null, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.evaluate(List.of(0, 4, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(problem.evaluate(List.of(0, 0, 1))).isEqualTo(6.0);
        assertThat(problem.isSolved(4.0)).isTrue();
        assertThat(problem.isSolved(4.01)).isFalse();
    }

    @Test
    void routingSchedulingPackingAndLearningObjectivesAreFinite() {
        ContinuousFunctionProblem sphere = new ContinuousFunctionProblem(
                2, -5, 5, ContinuousFunctionProblem.Function.SPHERE);
        assertThat(sphere.evaluate(List.of(3.0, 4.0))).isEqualTo(25.0);

        JobShopSchedulingProblem jobShop = new JobShopSchedulingProblem(
                new int[][]{{2, 1}, {1, 2}}, new int[][]{{0, 1}, {1, 0}}, 2);
        assertThat(jobShop.evaluate(List.of(0, 2, 1, 3))).isPositive();

        VehicleRoutingProblem vrp = new VehicleRoutingProblem(List.of(
                new TspProblem.City(0, 0), new TspProblem.City(1, 0),
                new TspProblem.City(2, 0)), new int[]{2, 2}, 2, 2);
        assertThat(vrp.evaluate(List.of(0, 1))).isEqualTo(6.0);

        BinPackingProblem packing = new BinPackingProblem(new int[]{4, 6, 5}, 10);
        assertThat(packing.evaluate(List.of(0, 0, 1))).isEqualTo(2.0);

        MaxCutProblem maxCut = new MaxCutProblem(2,
                List.of(new MaxCutProblem.WeightedEdge(0, 1, 3.0)));
        assertThat(maxCut.evaluate(List.of(false, true))).isEqualTo(-3.0);

        FeatureSelectionProblem featureSelection = new FeatureSelectionProblem(
                new double[][]{{0}, {0.1}, {1}, {1.1}}, new int[]{0, 0, 1, 1});
        assertThat(featureSelection.evaluate(List.of(true))).isLessThan(0.01);

        SymbolicRegressionProblem regression = new SymbolicRegressionProblem(
                new double[]{-1, 0, 1}, new double[]{1, 0, 1}, 2, 6);
        assertThat(regression.evaluate(Expression.variable())).isFinite();

        NeuralNetworkWeightProblem neural = new NeuralNetworkWeightProblem(
                new double[][]{{0}, {1}}, new double[][]{{0}, {1}}, 2);
        assertThat(neural.evaluate(neural.randomSolution(new java.util.Random(2)))).isFinite();
    }

    @Test
    void expressionTreesEvaluateEveryOperatorAndSupportImmutableTreeEdits() {
        Expression x = Expression.variable();
        Expression two = Expression.constant(2.0);
        Expression tree = Expression.random(3, new java.util.Random(14));

        assertThat(x.evaluate(-2.0)).isEqualTo(-2.0);
        assertThat(two.evaluate(100.0)).isEqualTo(2.0);
        assertThat(tree.size()).isPositive();
        assertThat(tree.depth()).isBetween(1, 4);
        assertThat(tree.nodeAt(0)).isSameAs(tree);
        assertThat(tree.replaceAt(0, x)).isSameAs(x);
        assertThat(tree.replaceAt(tree.size() - 1, two).size()).isPositive();
        assertThat(Expression.constant(4.0).toString()).isEqualTo("4.000");
        assertThatThrownBy(() -> Expression.constant(Double.NaN)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Expression.random(-1, new java.util.Random(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Expression.random(1, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Expression.constant(1).nodeAt(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Expression.constant(1).replaceAt(0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expressionRandomGrammarCoversUnaryBinaryAndProtectedDivisionOperators() {
        boolean add = false;
        boolean subtract = false;
        boolean multiply = false;
        boolean divide = false;
        boolean sine = false;
        boolean cosine = false;
        boolean variable = false;
        boolean constant = false;
        for (long seed = 0; seed < 2_000; seed++) {
            Expression expression = Expression.random(3, new java.util.Random(seed));
            String text = expression.toString();
            add |= text.contains(" + ");
            subtract |= text.contains(" - ");
            multiply |= text.contains(" * ");
            divide |= text.contains(" / ");
            sine |= text.contains("sin(");
            cosine |= text.contains("cos(");
            variable |= text.equals("x") || text.contains("(x") || text.contains(" x") || text.contains("x)");
            constant |= text.contains(".");
        }
        assertThat(add).as("addition generated").isTrue();
        assertThat(subtract).as("subtraction generated").isTrue();
        assertThat(multiply).as("multiplication generated").isTrue();
        assertThat(divide).as("division generated").isTrue();
        assertThat(sine).as("sine generated").isTrue();
        assertThat(cosine).as("cosine generated").isTrue();
        assertThat(variable).as("variable generated").isTrue();
        assertThat(constant).as("constant generated").isTrue();
        assertThat(Expression.constant(2.0).replaceAt(0, Expression.variable()).evaluate(4.0)).isEqualTo(4.0);
        assertThat(Expression.random(2, new java.util.Random(1))).isNotNull();
    }

    @Test
    void continuousBenchmarksValidateInputsAndClampVariationToBounds() {
        ContinuousFunctionProblem rastrigin = new ContinuousFunctionProblem(
                2, -5.12, 5.12, ContinuousFunctionProblem.Function.RASTRIGIN);
        ContinuousFunctionProblem rosenbrock = new ContinuousFunctionProblem(
                2, -5.0, 5.0, ContinuousFunctionProblem.Function.ROSENBROCK);
        assertThat(rastrigin.evaluate(List.of(0.0, 0.0))).isZero();
        assertThat(rosenbrock.evaluate(List.of(1.0, 1.0))).isZero();
        assertThat(rosenbrock.isSolved(0.0)).isFalse();
        assertThat(new ContinuousFunctionProblem(1, -1, 1, ContinuousFunctionProblem.Function.SPHERE)
                .isSolved(1e-9)).isTrue();

        java.util.Random random = new java.util.Random(8);
        List<Double> crossed = rastrigin.crossover(List.of(-5.0, 5.0), List.of(5.0, -5.0), random);
        List<Double> mutated = rastrigin.mutate(List.of(5.12, -5.12), 1.0, random);
        assertThat(crossed).allMatch(value -> value >= -5.12 && value <= 5.12);
        assertThat(mutated).allMatch(value -> value >= -5.12 && value <= 5.12);
        assertThatThrownBy(() -> new ContinuousFunctionProblem(0, -1, 1,
                ContinuousFunctionProblem.Function.SPHERE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ContinuousFunctionProblem(1, 1, -1,
                ContinuousFunctionProblem.Function.SPHERE)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ContinuousFunctionProblem(1, -1, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rastrigin.evaluate(List.of(0.0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rastrigin.evaluate(java.util.Arrays.asList((Double) null, 0.0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rastrigin.evaluate(List.of(Double.NaN, 0.0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rastrigin.evaluate(List.of(6.0, 0.0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void maxSatValidatesClausesAssignmentsAndVariation() {
        assertThatThrownBy(() -> new MaxSatProblem(0, List.of(new MaxSatProblem.Clause(1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxSatProblem(2, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxSatProblem(2, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxSatProblem(2,
                java.util.Arrays.asList((MaxSatProblem.Clause) null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxSatProblem(2, List.of(new MaxSatProblem.Clause())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxSatProblem.Clause((int[]) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxSatProblem(2, List.of(new MaxSatProblem.Clause(0))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxSatProblem(2, List.of(new MaxSatProblem.Clause(-3))))
                .isInstanceOf(IllegalArgumentException.class);

        MaxSatProblem problem = new MaxSatProblem(2, List.of(
                new MaxSatProblem.Clause(1), new MaxSatProblem.Clause(-2)));
        assertThat(problem.name()).isEqualTo("MAX-SAT");
        assertThat(problem.evaluate(List.of(true, false))).isEqualTo(-2);
        assertThat(problem.evaluate(List.of(false, true))).isZero();
        assertThat(problem.isSolved(-2)).isTrue();
        assertThat(problem.isSolved(-1)).isFalse();
        java.util.Random random = new java.util.Random(11);
        assertThat(problem.randomSolution(random)).hasSize(2);
        assertThat(problem.crossover(List.of(true, false), List.of(false, true), random)).hasSize(2);
        assertThat(problem.mutate(List.of(true, false), 1.0, random)).containsExactly(false, true);
        assertThatThrownBy(() -> problem.evaluate(List.of(true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void neuralNetworkValidatesDataAndGenomeAndKeepsMutatedWeightsBounded() {
        assertThatThrownBy(() -> new NeuralNetworkWeightProblem(null, new double[][]{{1}}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NeuralNetworkWeightProblem(new double[0][1], new double[0][1], 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NeuralNetworkWeightProblem(
                new double[][]{{1}}, new double[][]{{1}}, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NeuralNetworkWeightProblem(
                new double[][]{{1}, {1, 2}}, new double[][]{{1}, {1}}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NeuralNetworkWeightProblem(
                new double[][]{{1}, {2}}, new double[][]{{1}, {1, 0}}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NeuralNetworkWeightProblem(
                new double[][]{{Double.NaN}, {1}}, new double[][]{{1}, {1}}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NeuralNetworkWeightProblem(
                new double[][]{{1}, {2}}, new double[][]{{2}, {1}}, 1))
                .isInstanceOf(IllegalArgumentException.class);

        NeuralNetworkWeightProblem problem = new NeuralNetworkWeightProblem(
                new double[][]{{0}, {1}}, new double[][]{{0}, {1}}, 2);
        assertThatThrownBy(() -> problem.evaluate(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.evaluate(List.of(1.0)))
                .isInstanceOf(IllegalArgumentException.class);
        List<Double> invalidWeight = new java.util.ArrayList<>(problem.randomSolution(new java.util.Random(4)));
        invalidWeight.set(0, Double.POSITIVE_INFINITY);
        assertThatThrownBy(() -> problem.evaluate(invalidWeight))
                .isInstanceOf(IllegalArgumentException.class);
        List<Double> extremeWeights = new java.util.ArrayList<>(
                problem.randomSolution(new java.util.Random(3)));
        int weightCount = extremeWeights.size();
        java.util.Collections.fill(extremeWeights, 100.0);
        assertThat(problem.mutate(extremeWeights, 0.0, new java.util.Random(4)))
                .allMatch(value -> value == 5.0);
        assertThat(problem.mutate(problem.randomSolution(new java.util.Random(5)), 1.0,
                new java.util.Random(6))).hasSize(weightCount);
    }

    @Test
    void jobShopSchedulingValidatesInstancesAndSchedulesEveryEligibleOperation() {
        assertThatThrownBy(() -> new JobShopSchedulingProblem(null, new int[][]{{0}}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobShopSchedulingProblem(new int[][]{{1}}, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobShopSchedulingProblem(new int[0][0], new int[0][0], 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobShopSchedulingProblem(
                new int[][]{{1}}, new int[][]{{0}}, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobShopSchedulingProblem(
                new int[][]{{1}}, new int[][]{{0, 1}}, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobShopSchedulingProblem(
                new int[][]{{1}, {1, 2}}, new int[][]{{0}, {0, 1}}, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobShopSchedulingProblem(
                new int[][]{{0}}, new int[][]{{0}}, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JobShopSchedulingProblem(
                new int[][]{{1}}, new int[][]{{1}}, 1)).isInstanceOf(IllegalArgumentException.class);

        JobShopSchedulingProblem problem = new JobShopSchedulingProblem(
                new int[][]{{3, 2}, {2, 4}}, new int[][]{{0, 1}, {1, 0}}, 2);
        assertThat(problem.name()).isEqualTo("Job-Shop Scheduling");
        assertThat(problem.evaluate(List.of(0, 2, 1, 3))).isPositive();
        java.util.Random random = new java.util.Random(34);
        assertThat(problem.randomSolution(random)).hasSize(4);
        assertThat(problem.crossover(List.of(0, 1, 2, 3), List.of(3, 2, 1, 0), random))
                .containsExactlyInAnyOrder(0, 1, 2, 3);
        assertThat(problem.mutate(List.of(0, 1, 2, 3), 1.0, random))
                .containsExactlyInAnyOrder(0, 1, 2, 3);
        for (List<Integer> invalid : List.of(List.of(0, 1), List.of(0, 0, 2, 3), List.of(0, 1, 2, 4))) {
            assertThatThrownBy(() -> problem.evaluate(invalid)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void vehicleRoutingValidatesCustomersAndPenalizesExcessVehicles() {
        List<TspProblem.City> locations = List.of(new TspProblem.City(0, 0),
                new TspProblem.City(1, 0), new TspProblem.City(2, 0));
        assertThatThrownBy(() -> new VehicleRoutingProblem(null, new int[]{1}, 2, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(locations, null, 2, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(List.of(new TspProblem.City(0, 0)),
                new int[0], 2, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(locations, new int[]{1}, 2, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(locations, new int[]{1, 1}, 0, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(locations, new int[]{1, 1}, 2, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(
                java.util.Arrays.asList(locations.get(0), null, locations.get(2)),
                new int[]{1, 1}, 2, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(locations, new int[]{0, 1}, 2, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VehicleRoutingProblem(locations, new int[]{3, 1}, 2, 1))
                .isInstanceOf(IllegalArgumentException.class);

        VehicleRoutingProblem problem = new VehicleRoutingProblem(locations, new int[]{2, 2}, 2, 1);
        assertThat(problem.name()).isEqualTo("Capacitated Vehicle Routing");
        assertThat(problem.evaluate(List.of(0, 1))).isGreaterThan(4.0);
        java.util.Random random = new java.util.Random(41);
        assertThat(problem.randomSolution(random)).containsExactlyInAnyOrder(0, 1);
        assertThat(problem.crossover(List.of(0, 1), List.of(1, 0), random))
                .containsExactlyInAnyOrder(0, 1);
        assertThat(problem.mutate(List.of(0, 1), 1.0, random)).containsExactlyInAnyOrder(0, 1);
        for (List<Integer> invalid : List.of(List.of(0), List.of(0, 0), List.of(0, 2))) {
            assertThatThrownBy(() -> problem.evaluate(invalid)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void setCoverValidatesSetsAndPenalizesEachUncoveredElement() {
        assertThatThrownBy(() -> new SetCoverProblem(0, List.of(List.of(0)), new double[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, null, new double[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, List.of(), new double[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, List.of(List.of(0)), new double[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, java.util.Arrays.asList((List<Integer>) null),
                new double[]{1})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, List.of(List.of()), new double[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, List.of(List.of(2)), new double[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, List.of(List.of(0)), new double[]{0}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SetCoverProblem(2, List.of(List.of(0)),
                new double[]{Double.NaN})).isInstanceOf(IllegalArgumentException.class);

        SetCoverProblem problem = new SetCoverProblem(3,
                List.of(List.of(0, 1), List.of(1), List.of(2)), new double[]{2, 1, 4});
        assertThat(problem.name()).isEqualTo("Set Cover");
        assertThat(problem.evaluate(List.of(true, false, true))).isEqualTo(6.0);
        assertThat(problem.evaluate(List.of(true, false, false))).isEqualTo(2.0 + 8.0);
        java.util.Random random = new java.util.Random(19);
        assertThat(problem.randomSolution(random)).hasSize(3);
        assertThat(problem.crossover(List.of(true, false, true), List.of(false, true, false), random))
                .hasSize(3);
        assertThat(problem.mutate(List.of(true, false, true), 1.0, random)).hasSize(3);
        assertThatThrownBy(() -> problem.evaluate(List.of(true, false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void knapsackValidatesItemsAndHandlesFeasibleAndInfeasibleGenomes() {
        assertThatThrownBy(() -> new KnapsackProblem(null, new double[]{1}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[]{1}, null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[0], new double[0], 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[]{1}, new double[]{1, 2}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[]{1}, new double[]{1}, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[]{0}, new double[]{1}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[]{1}, new double[]{Double.NaN}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[]{1}, new double[]{0}, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KnapsackProblem(new int[]{1, 1},
                new double[]{Double.MAX_VALUE, Double.MAX_VALUE}, 1))
                .isInstanceOf(IllegalArgumentException.class);

        KnapsackProblem problem = new KnapsackProblem(new int[]{2, 3}, new double[]{3, 5}, 3);
        assertThat(problem.name()).isEqualTo("0/1 Knapsack");
        assertThat(problem.evaluate(List.of(false, false))).isZero();
        assertThat(problem.evaluate(List.of(true, false))).isEqualTo(-3.0);
        assertThat(problem.evaluate(List.of(true, true))).isGreaterThan(-5.0);
        java.util.Random random = new java.util.Random(1);
        assertThat(problem.randomSolution(random)).hasSize(2);
        assertThat(problem.crossover(List.of(true, false), List.of(false, true), random)).hasSize(2);
        assertThat(problem.mutate(List.of(true, false), 1.0, random)).hasSize(2);
        assertThatThrownBy(() -> problem.evaluate(java.util.Arrays.asList(true, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void binPackingValidatesAssignmentsAndReportsTheLowerBound() {
        assertThatThrownBy(() -> new BinPackingProblem(null, 5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BinPackingProblem(new int[0], 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BinPackingProblem(new int[]{1}, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BinPackingProblem(new int[]{0}, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BinPackingProblem(new int[]{6}, 5))
                .isInstanceOf(IllegalArgumentException.class);

        BinPackingProblem problem = new BinPackingProblem(new int[]{4, 6, 5}, 10);
        assertThat(problem.name()).isEqualTo("Bin Packing");
        assertThat(problem.evaluate(List.of(0, 1, 0))).isEqualTo(2.0);
        assertThat(problem.evaluate(List.of(0, 0, 0))).isGreaterThan(2.0);
        assertThat(problem.isSolved(2.0)).isTrue();
        assertThat(problem.isSolved(3.0)).isFalse();
        java.util.Random random = new java.util.Random(71);
        assertThat(problem.randomSolution(random)).hasSize(3);
        assertThat(problem.crossover(List.of(0, 0, 1), List.of(1, 1, 2), random)).hasSize(3);
        assertThat(problem.mutate(List.of(0, 0, 1), 1.0, random)).hasSize(3);
        for (List<Integer> invalid : List.of(List.of(0), List.of(0, 0, 3))) {
            assertThatThrownBy(() -> problem.evaluate(invalid)).isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> problem.evaluate(java.util.Arrays.asList(0, null, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void maxCutValidatesGraphAndEvaluatesBothCutAndUncutEdges() {
        assertThatThrownBy(() -> new MaxCutProblem(1, List.of(
                new MaxCutProblem.WeightedEdge(0, 0, 1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxCutProblem(2, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MaxCutProblem(2, List.of())).isInstanceOf(IllegalArgumentException.class);
        for (MaxCutProblem.WeightedEdge edge : java.util.Arrays.asList(
                null, new MaxCutProblem.WeightedEdge(-1, 1, 1),
                new MaxCutProblem.WeightedEdge(0, 2, 1),
                new MaxCutProblem.WeightedEdge(1, 1, 1),
                new MaxCutProblem.WeightedEdge(0, 1, 0),
                new MaxCutProblem.WeightedEdge(0, 1, Double.NaN))) {
            assertThatThrownBy(() -> new MaxCutProblem(2, java.util.Arrays.asList(edge)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        MaxCutProblem problem = new MaxCutProblem(3, List.of(
                new MaxCutProblem.WeightedEdge(0, 1, 2.0),
                new MaxCutProblem.WeightedEdge(1, 2, 3.0)));
        assertThat(problem.name()).isEqualTo("Maximum Cut");
        assertThat(problem.evaluate(List.of(false, false, false))).isZero();
        assertThat(problem.evaluate(List.of(false, true, false))).isEqualTo(-5.0);
        java.util.Random random = new java.util.Random(27);
        assertThat(problem.randomSolution(random)).hasSize(3);
        assertThat(problem.crossover(List.of(false, false, false), List.of(true, true, true), random))
                .hasSize(3);
        assertThat(problem.mutate(List.of(false, false, false), 1.0, random)).hasSize(3);
        assertThatThrownBy(() -> problem.evaluate(List.of(true, false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void featureSelectionHandlesEmptySubsetNearestNeighborTiesAndInvalidData() {
        assertThatThrownBy(() -> new FeatureSelectionProblem(null, new int[]{0, 1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FeatureSelectionProblem(new double[][]{{1}, {2}}, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FeatureSelectionProblem(new double[][]{{1}}, new int[]{0}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FeatureSelectionProblem(new double[][]{{1}, {2}}, new int[]{0}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FeatureSelectionProblem(new double[][]{{1}, {2, 3}},
                new int[]{0, 1})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FeatureSelectionProblem(new double[][]{{Double.NaN}, {2}},
                new int[]{0, 1})).isInstanceOf(IllegalArgumentException.class);

        FeatureSelectionProblem problem = new FeatureSelectionProblem(
                new double[][]{{0}, {0}, {1}}, new int[]{0, 1, 1});
        assertThat(problem.name()).isEqualTo("Feature Selection");
        assertThat(problem.evaluate(List.of(false))).isEqualTo(1.0);
        assertThat(problem.evaluate(List.of(true))).isPositive();
        java.util.Random random = new java.util.Random(1);
        assertThat(problem.randomSolution(random)).hasSize(1);
        assertThat(problem.crossover(List.of(true), List.of(false), random)).hasSize(1);
        assertThat(problem.mutate(List.of(true), 1.0, random)).hasSize(1);
        assertThatThrownBy(() -> problem.evaluate(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void symbolicRegressionValidatesTreesAndBoundsNonFiniteFitness() {
        assertThatThrownBy(() -> new SymbolicRegressionProblem(null, new double[]{1}, 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymbolicRegressionProblem(new double[]{1}, null, 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymbolicRegressionProblem(new double[0], new double[0], 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymbolicRegressionProblem(new double[]{1}, new double[]{1, 2}, 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymbolicRegressionProblem(new double[]{Double.NaN},
                new double[]{1}, 1, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymbolicRegressionProblem(new double[]{1},
                new double[]{Double.POSITIVE_INFINITY}, 1, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymbolicRegressionProblem(new double[]{1}, new double[]{1}, -1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SymbolicRegressionProblem(new double[]{1}, new double[]{1}, 2, 1))
                .isInstanceOf(IllegalArgumentException.class);

        SymbolicRegressionProblem problem = new SymbolicRegressionProblem(
                new double[]{Double.MAX_VALUE}, new double[]{-Double.MAX_VALUE}, 0, 2);
        assertThat(problem.name()).isEqualTo("Symbolic Regression");
        assertThat(problem.evaluate(Expression.constant(Double.MAX_VALUE))).isEqualTo(Double.MAX_VALUE);
        assertThatThrownBy(() -> problem.evaluate(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.evaluate(Expression.random(3, new java.util.Random(1))))
                .isInstanceOf(IllegalArgumentException.class);
        java.util.Random random = new java.util.Random(8);
        Expression expression = Expression.variable();
        assertThat(problem.mutate(expression, 0.0, random)).isSameAs(expression);
        assertThat(problem.randomSolution(random)).isNotNull();
        assertThat(problem.crossover(expression, Expression.constant(1), random)).isNotNull();
    }

}
