package org.qcri.rheem.core.optimizer;

import org.qcri.rheem.core.plan.rheemplan.*;
import org.qcri.rheem.core.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dummy implementation: just resolve alternatives by looking for those alternatives that are execution operators.
 */
@Deprecated
public class Optimizer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Queue<EnumerationPath> enumerationPaths = new LinkedList<>();

    private Collection<RheemPlan> executionPlans = new LinkedList<>();

    public RheemPlan buildExecutionPlan(RheemPlan rheemPlan) {

        // Find all sources.
        final Collection<Operator> sources = new PlanTraversal(true, false)
                .traverse(rheemPlan.getSinks())
                .getTraversedNodesWith(Operator::isSource);

        // Create the working paths.
        final Queue<WorkingPath> workingPathQueue = sources.stream()
                .map(WorkingPath::new)
                .collect(Collectors.toCollection(LinkedList::new));

        // Start working on the working paths.
        this.enumerationPaths.add(new EnumerationPath(workingPathQueue, new ExecutionPlanBuilder()));
        while (!this.enumerationPaths.isEmpty()) {
            this.enumerationPaths.poll().run();
        }

        this.logger.info("Found {} execution plan(s).", this.executionPlans.size());

        return this.executionPlans.stream().findAny().get();
    }

    private class EnumerationPath implements Runnable {

        private final Queue<WorkingPath> workingPaths;

        private final ExecutionPlanBuilder executionPlanBuilder;

        private final Set<Operator> processedOperators;

        private EnumerationPath(Queue<WorkingPath> workingPaths,
                                ExecutionPlanBuilder executionPlanBuilder) {
            this(workingPaths, executionPlanBuilder, new HashSet<>());
        }

        private EnumerationPath(Queue<WorkingPath> workingPaths,
                                ExecutionPlanBuilder executionPlanBuilder,
                                Set<Operator> processedOperators) {
            this.workingPaths = workingPaths;
            this.executionPlanBuilder = executionPlanBuilder;
            this.processedOperators = processedOperators;
        }

        @Override
        public void run() {
            while (!this.workingPaths.isEmpty()) {
                final WorkingPath workingPath = this.workingPaths.poll();
                if (this.processedOperators.contains(workingPath.operatorToBeProcessed)) {
                    continue;
                }
                Optimizer.this.logger.debug("Processing working path with {} ({} left in queue).",
                        workingPath.operatorToBeProcessed,
                        this.workingPaths.size());
                this.process(workingPath);
            }

            this.executionPlanBuilder.build().stream().forEach(Optimizer.this.executionPlans::add);
        }

        private void process(WorkingPath workingPath) {
            final Operator nextOperator = workingPath.operatorToBeProcessed;
            if (nextOperator.isAlternative()) {
                OperatorAlternative operatorAlternative = (OperatorAlternative) nextOperator;
                OperatorAlternative.Alternative unforkedAlternative = null;
                for (OperatorAlternative.Alternative alternative : operatorAlternative.getAlternatives()) {
                    if (Objects.isNull(unforkedAlternative)) {
                        unforkedAlternative = alternative;
                    } else {
                        this.forkWith(alternative);
                    }
                }
                this.workingPaths.add(new WorkingPath(unforkedAlternative.getOperator()));
                this.processedOperators.add(nextOperator);
                return;
            }

            if (nextOperator.isSubplan()) {
                Subplan subplan = (Subplan) nextOperator;
                Arrays.stream(subplan.getAllInputs())
                        .flatMap(input -> subplan.followInput(input).stream())
                        .map(InputSlot::getOwner)
                        .map(WorkingPath::new)
                        .forEach(this.workingPaths::add);
                this.processedOperators.add(nextOperator);
                return;

            }

            if (nextOperator.isElementary()) {
                if (nextOperator.isExecutionOperator()) {
                    ExecutionOperator executionOperator = (ExecutionOperator) nextOperator;

                    // Check if and how we can satisfy the inputs of the operator.
                    boolean isAllInputsServed = true;
                    OutputSlot[] servingOutputSlots = new OutputSlot[executionOperator.getNumInputs()];
                    for (int i = 0; i < executionOperator.getNumInputs(); i++) {
                        Optional<OutputSlot> optionalServingOutputSlot = this.executionPlanBuilder.getServingOutputSlot(executionOperator.getInput(i));
                        if (!optionalServingOutputSlot.isAvailable()) {
                            isAllInputsServed = false;
                            break;
                        } else {
                            servingOutputSlots[i] = optionalServingOutputSlot.getValue();
                        }
                    }

                    // Defer if not all inputs are available, yet.
                    if (!isAllInputsServed) {
                        this.workingPaths.add(workingPath);
                        return;
                    }

                    // Otherwise add the operator.
                    this.executionPlanBuilder.add(executionOperator, servingOutputSlots);

                    // Schedule the processing of the following operators.
                    Arrays.stream(executionOperator.getAllOutputs())
                            .flatMap(outputSlot -> OutputSlot.followOutputRecursively(outputSlot).stream())
                            .flatMap(outputSlot -> outputSlot.getOccupiedSlots().stream())
                            .map(InputSlot::getOwner)
                            .distinct()
                            .map(WorkingPath::new)
                            .forEach(this.workingPaths::add);
                }

                // Ignore non-execution elementary operators.
                this.processedOperators.add(nextOperator);
                return;
            }

            throw new IllegalStateException("Unknown operator type: " + nextOperator);
        }

        public void forkWith(OperatorAlternative.Alternative alternative) {
            // Add the Alternative as WorkingPath to the fork.
            final LinkedList<WorkingPath> workingPathsCopy = new LinkedList<>(this.workingPaths);
            workingPathsCopy.add(new WorkingPath(alternative.getOperator()));

            // Mark the OperatorAlternative as processed in the fork.
            final HashSet<Operator> processedOperatorsCopy = new HashSet<>(this.processedOperators);
            processedOperatorsCopy.add(alternative.toOperator());

            // Create the fork.
            EnumerationPath fork = new EnumerationPath(
                    workingPathsCopy,
                    this.executionPlanBuilder.copy(),
                    processedOperatorsCopy
            );
            Optimizer.this.enumerationPaths.add(fork);
        }
    }

    private static class WorkingPath {

        private final Operator operatorToBeProcessed;

        public WorkingPath(Operator operatorToBeProcessed) {
            this.operatorToBeProcessed = operatorToBeProcessed;
        }
    }
}
