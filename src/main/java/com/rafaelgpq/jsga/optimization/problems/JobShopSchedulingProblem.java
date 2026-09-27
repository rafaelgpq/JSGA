package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Random;

/** Job-shop makespan minimization using operation-priority permutations. */
public final class JobShopSchedulingProblem implements OptimizationProblem<List<Integer>> {

    private final int[][] processingTimes;
    private final int[][] machineOrder;
    private final int machineCount;
    private final int operationCount;

    public JobShopSchedulingProblem(int[][] processingTimes, int[][] machineOrder, int machineCount) {
        if (processingTimes == null || machineOrder == null || processingTimes.length == 0
                || processingTimes.length != machineOrder.length || machineCount <= 0
                || machineOrder[0] == null || processingTimes[0] == null) {
            throw new IllegalArgumentException("Job-shop instance dimensions are invalid.");
        }
        this.machineCount = machineCount;
        long operationCount = (long) processingTimes.length * machineOrder[0].length;
        if (operationCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Job-shop instance has too many operations.");
        }
        this.operationCount = (int) operationCount;
        if (machineOrder[0].length == 0) throw new IllegalArgumentException("Each job must contain operations.");
        this.processingTimes = new int[processingTimes.length][];
        this.machineOrder = new int[machineOrder.length][];
        for (int job = 0; job < processingTimes.length; job++) {
            if (processingTimes[job] == null || machineOrder[job] == null
                    || processingTimes[job].length != machineOrder[job].length
                    || processingTimes[job].length != machineOrder[0].length) {
                throw new IllegalArgumentException("Every job must have matching processing and machine sequences.");
            }
            this.processingTimes[job] = processingTimes[job].clone();
            this.machineOrder[job] = machineOrder[job].clone();
            for (int operation = 0; operation < processingTimes[job].length; operation++) {
                if (processingTimes[job][operation] <= 0 || machineOrder[job][operation] < 0
                        || machineOrder[job][operation] >= machineCount) {
                    throw new IllegalArgumentException("Processing times and machine indices must be valid.");
                }
            }
        }
    }

    @Override public String name() { return "Job-Shop Scheduling"; }
    @Override public List<Integer> randomSolution(Random random) {
        return OptimizationUtils.randomPermutation(operationCount, random);
    }

    @Override
    public double evaluate(List<Integer> priorities) {
        validate(priorities);
        int jobs = processingTimes.length;
        int operationsPerJob = machineOrder[0].length;
        int[] nextOperation = new int[jobs];
        long[] jobReady = new long[jobs];
        long[] machineReady = new long[machineCount];

        for (int scheduled = 0; scheduled < operationCount; scheduled++) {
            int operationId = -1;
            for (int priority : priorities) {
                int job = priority / operationsPerJob;
                int operation = priority % operationsPerJob;
                if (operation == nextOperation[job]) {
                    operationId = priority;
                    break;
                }
            }
            if (operationId < 0) throw new IllegalStateException("No eligible job-shop operation remains.");
            int job = operationId / operationsPerJob;
            int operation = operationId % operationsPerJob;
            int machine = machineOrder[job][operation];
            long finish = Math.max(jobReady[job], machineReady[machine])
                    + processingTimes[job][operation];
            jobReady[job] = finish;
            machineReady[machine] = finish;
            nextOperation[job]++;
        }

        long makespan = 0;
        for (long ready : jobReady) makespan = Math.max(makespan, ready);
        return makespan;
    }

    @Override public List<Integer> crossover(List<Integer> first, List<Integer> second, Random random) {
        validate(first);
        validate(second);
        return OptimizationUtils.orderedCrossover(first, second, random);
    }

    @Override public List<Integer> mutate(List<Integer> solution, double rate, Random random) {
        validate(solution);
        return OptimizationUtils.swapMutation(solution, rate, random);
    }

    private void validate(List<Integer> priorities) {
        if (priorities == null || priorities.size() != operationCount
                || priorities.stream().anyMatch(id -> id == null || id < 0 || id >= operationCount)
                || priorities.stream().distinct().count() != operationCount) {
            throw new IllegalArgumentException("Job-shop solution must permute all operation priorities.");
        }
    }
}
