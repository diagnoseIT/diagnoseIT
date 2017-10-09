package org.diagnoseit.traceservices.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.diagnoseit.traceservices.aggregated.AbstractAggregatedTimedCallable;
import org.diagnoseit.traceservices.aggregated.AggregatedMethodInvocation;
import org.diagnoseit.traceservices.aggregated.BusinessTransactionCall;
import org.diagnoseit.traceservices.aggregated.NumericStatistics;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

public class ProblemInstance {
	private static final int PROBLEM_OCCURRENCE_CAPACITY = 100;
	private static final BusinessTransactionCall btTemplate = new BusinessTransactionCall(null);
	private ProblemInstanceID instanceID;
	private AbstractAggregatedTimedCallable<? extends TimedCallable> businessTransactionData;
	private AbstractAggregatedTimedCallable<? extends TimedCallable> entryPointData;
	private AbstractAggregatedTimedCallable<? extends TimedCallable> problemContextData;
	private AbstractAggregatedTimedCallable<? extends TimedCallable> causeData;
	private NumericStatistics<Integer> causeCountStats = new NumericStatistics<Integer>(Integer.class);
	private NumericStatistics<Long> causeEclusiveTimeSumStats = new NumericStatistics<Long>(Long.class);
	private NumericStatistics<Long> causeCPUEclusiveTimeSumStats = new NumericStatistics<Long>(Long.class);
	private Queue<ProblemOccurrence> problemOccurrences;

	private Map<String, AffectedNodeData> affectedNodes;
	private CauseExecutionType causeExecType;
	private int count = 0;
	private long baseline;

	private List<AntipatternInstance> antipatterns = new ArrayList<AntipatternInstance>();

	public ProblemInstance() {
	}

	public ProblemInstance(TimedCallable problemContext, AbstractAggregatedTimedCallable<? extends TimedCallable> cause, long baseline) {
		instanceID = ProblemInstanceID.createProblemInstanceId(problemContext, cause);

		businessTransactionData = AbstractAggregatedTimedCallable.createAggregatedCallable(btTemplate, false);
		entryPointData = AbstractAggregatedTimedCallable.createAggregatedCallable((TimedCallable) cause.getContainingSubTrace().getRoot(), false);
		problemContextData = AbstractAggregatedTimedCallable.createAggregatedCallable(problemContext, false);
		causeData = AbstractAggregatedTimedCallable.createAggregatedCallable(cause.getCallables().get(0), false);
		problemOccurrences = new LinkedBlockingQueue<ProblemOccurrence>();
		affectedNodes = new HashMap<String, AffectedNodeData>();
		this.baseline = baseline;
		update(problemContext, cause);
	}

	public boolean canUpdate(TimedCallable problemContext, AbstractAggregatedTimedCallable<? extends TimedCallable> cause) {
		return instanceID.equals(ProblemInstanceID.createProblemInstanceId(problemContext, cause));
	}

	public synchronized void update(ProblemInstance otherProblemInstance) {
		businessTransactionData.aggregate(otherProblemInstance.businessTransactionData);
		entryPointData.aggregate(otherProblemInstance.entryPointData);
		problemContextData.aggregate(otherProblemInstance.problemContextData);
		causeData.aggregate(otherProblemInstance.causeData);
		causeCountStats.mergeStatistics(otherProblemInstance.causeCountStats);
		causeEclusiveTimeSumStats.mergeStatistics(otherProblemInstance.causeEclusiveTimeSumStats);
		causeCPUEclusiveTimeSumStats.mergeStatistics(otherProblemInstance.causeCPUEclusiveTimeSumStats);
		for (ProblemOccurrence pOccurrence : otherProblemInstance.getProblemOccurrences()) {
			problemOccurrences.offer(pOccurrence);
		}
		while (problemOccurrences.size() > PROBLEM_OCCURRENCE_CAPACITY) {
			problemOccurrences.poll();
		}

		for (String key : otherProblemInstance.getAffectedNodes()) {
			AffectedNodeData otherAffNodeData = otherProblemInstance.affectedNodes.get(key);
			AffectedNodeData affNodeData = null;
			if (affectedNodes.containsKey(key)) {
				affNodeData = affectedNodes.get(key);
				affNodeData.setCount(affNodeData.getCount() + otherAffNodeData.getCount());
				if (otherAffNodeData.getLastOccurrence().getTime() > affNodeData.getLastOccurrence().getTime()) {
					affNodeData.setLastOccurrence(otherAffNodeData.getLastOccurrence());
				}
			} else {
				affNodeData = otherAffNodeData;
			}

			affectedNodes.put(key, affNodeData);
		}

		count += otherProblemInstance.count;
	}

	public synchronized void update(TimedCallable problemContext, AbstractAggregatedTimedCallable<? extends TimedCallable> cause) {
		BusinessTransactionCall btCall = new BusinessTransactionCall((TimedCallable) cause.getContainingSubTrace().getContainingTrace().getRoot().getRoot());
		businessTransactionData.aggregate(btCall);
		entryPointData.aggregate((TimedCallable) cause.getContainingSubTrace().getRoot());
		problemContextData.aggregate(problemContext);
		for (TimedCallable causeElement : cause.getCallables()) {
			causeData.aggregate(causeElement);
		}
		causeCountStats.addValue(cause.getCount());
		causeEclusiveTimeSumStats.addValue(cause.getExclusiveTimeStats().getSum());
		if (cause instanceof AggregatedMethodInvocation) {
			causeCPUEclusiveTimeSumStats.addValue(((AggregatedMethodInvocation) cause).getCPUTimeSum());
		}

		ProblemOccurrence pOccurrence = new ProblemOccurrence();
		pOccurrence.setEntryPointIdentifier(problemContext.getContainingSubTrace().getRoot().getIdentifier());
		pOccurrence.setProblemContextIdentifier(problemContext.getIdentifier());
		for (Callable clbl : cause.getCallables()) {
			pOccurrence.addCauseIdentifier(clbl.getIdentifier());
		}

		problemOccurrences.offer(pOccurrence);
		while (problemOccurrences.size() >= PROBLEM_OCCURRENCE_CAPACITY) {
			problemOccurrences.poll();
		}

		String affectedNode = cause.getContainingSubTrace().getLocation().getHost();
		Date newDate = new Date(cause.getContainingSubTrace().getRoot().getTimestamp());
		AffectedNodeData affNodeData = null;
		if (affectedNodes.containsKey(affectedNode)) {
			affNodeData = affectedNodes.get(affectedNode);
			affNodeData.setCount(affNodeData.getCount() + 1);
			if (newDate.getTime() > affNodeData.getLastOccurrence().getTime()) {
				affNodeData.setLastOccurrence(newDate);
			}
		} else {
			affNodeData = new AffectedNodeData(newDate);
		}

		affectedNodes.put(affectedNode, affNodeData);

		count++;
	}

	/**
	 * @return the instanceID
	 */
	public ProblemInstanceID getInstanceID() {
		return instanceID;
	}

	/**
	 * @return the problemOccurrences
	 */
	public Queue<ProblemOccurrence> getProblemOccurrences() {
		return problemOccurrences;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instanceID == null) ? 0 : instanceID.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProblemInstance other = (ProblemInstance) obj;
		if (instanceID == null) {
			if (other.instanceID != null)
				return false;
		} else if (!instanceID.equals(other.instanceID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String problemString = "-------------------------------------------------------------------------------------------------\n";
		problemString += "      Problem Instance occurrences: " + getNumOccurrences() + "\n";
		problemString += "                       Entry point: " + instanceID.getEntryPoint() + "\n";

		problemString += "                   Problem context: " + instanceID.getProblemContextMethod() + "\n";
		problemString += "                             Cause: " + instanceID.getProblemCause() + "\n";
		if (causeCountStats.getMin().equals(causeCountStats.getMean()) && causeCountStats.getMean().equals(causeCountStats.getMax())) {
			problemString += "                       Cause count: " + causeCountStats.getMean() + "\n";
			problemString += "             ms execution time sum: " + causeEclusiveTimeSumStats.getMean() / 1000000L + "ms" + "\n\n";
		} else {
			problemString += "         [Min|Avg|Max] Cause count: " + "[ " + causeCountStats.getMin() + " | " + causeCountStats.getMean() + " | " + causeCountStats.getMax() + " ]\n";
			problemString += "[Min|Avg|Max]ms execution time sum: " + "[ " + causeEclusiveTimeSumStats.getMin() / 1000000L + "ms | " + causeEclusiveTimeSumStats.getMean() / 1000000L + "ms | "
					+ causeEclusiveTimeSumStats.getMax() / 1000000L + "ms ]" + "\n\n";
		}
		GenericProblemDescriptionText descrText = generateProblemDescriptionText();
		String text = descrText.createCauseExecutionAmountText() + "\n\n";
		text += descrText.createDurationConsumptionText() + "\n\n";
		text += descrText.createCPUTimeConsumtionText() + "\n\n";
		problemString += text;

		if (!antipatterns.isEmpty()) {
			String antiDescr = antipatterns.stream().map(antipattern -> antipattern.toString()).collect(Collectors.joining("\n\n"));
			problemString += antiDescr;
		}
		problemString += "-------------------------------------------------------------------------------------------------\n";

		return problemString;
	}

	public GenericProblemDescriptionText generateProblemDescriptionText() {

		long requestTime = businessTransactionData.getResponseTimeStats().getMean() / Trace.MILLIS_TO_NANOS_FACTOR;
		long causeTime = causeEclusiveTimeSumStats.getMean() / Trace.MILLIS_TO_NANOS_FACTOR;
		long causeCPUTime = Math.min(causeTime, causeCPUEclusiveTimeSumStats.getMean() / Trace.MILLIS_TO_NANOS_FACTOR);
		double contribution = 100.0 * ((double) causeTime) / ((double) requestTime);
		double cpuProportion = 100.0 * ((double) causeCPUTime) / ((double) causeTime);

		GenericProblemDescriptionText descriptionText = new GenericProblemDescriptionText();
		descriptionText.setBusinessTransactionIdentifier(instanceID.getBusinessTransaction());
		descriptionText.setProblemContextIdentifier(instanceID.getProblemContextMethod());
		descriptionText.setCauseIdentifier(instanceID.getProblemCause());
		descriptionText.setCauseCount(causeCountStats.getMean());
		descriptionText.setCauseExclCPUTimeSum(causeCPUTime);
		descriptionText.setCauseExclTimeSum(causeTime);
		descriptionText.setCauseExecType(causeExecType);
		descriptionText.setCauseType(causeData.getType());
		descriptionText.setCpuTimeContribution(cpuProportion);
		descriptionText.setDurationContribution(contribution);
		descriptionText.setRequestDuration(requestTime);
		descriptionText.setNumOccurences(problemOccurrences.size());
		Set<Object> uniqueTraces = new HashSet<Object>();
		for (ProblemOccurrence pc : problemOccurrences) {
			uniqueTraces.add(pc.getEntryPointIdentifier());
		}
		descriptionText.setNumAffectedTraces(uniqueTraces.size());
		return descriptionText;
	}

	/**
	 * @return the causeType
	 */
	public CauseExecutionType getCauseType() {
		return causeExecType;
	}

	/**
	 * @param causeType
	 *            the causeType to set
	 */
	public void setCauseType(CauseExecutionType causeType) {
		this.causeExecType = causeType;
	}

	public String getBusinessTransaction() {
		return instanceID.getBusinessTransaction();
	}

	public String getEntryPoint() {
		return instanceID.getEntryPoint();
	}

	public String getNodeType() {
		return instanceID.getNodeType();
	}

	public String getProblemContext() {
		return instanceID.getProblemContextMethod();
	}

	public String getCause() {
		return instanceID.getProblemCause();
	}

	/**
	 * @return the businessTransactionData
	 */
	public AbstractAggregatedTimedCallable<? extends TimedCallable> getBusinessTransactionData() {
		return businessTransactionData;
	}

	/**
	 * @return the entryPointData
	 */
	public AbstractAggregatedTimedCallable<? extends TimedCallable> getEntryPointData() {
		return entryPointData;
	}

	/**
	 * @return the problemContextData
	 */
	public AbstractAggregatedTimedCallable<? extends TimedCallable> getProblemContextData() {
		return problemContextData;
	}

	/**
	 * @return the causeData
	 */
	public AbstractAggregatedTimedCallable<? extends TimedCallable> getCauseData() {
		return causeData;
	}

	/**
	 * @return the causeCountStats
	 */
	public NumericStatistics<Integer> getCauseCountStats() {
		return causeCountStats;
	}

	/**
	 * @return the causeEclusiveTimeSumStats
	 */
	public NumericStatistics<Long> getCauseEclusiveTimeSumStats() {
		return causeEclusiveTimeSumStats;
	}

	/**
	 * @return the causeCPUEclusiveTimeSumStats
	 */
	public NumericStatistics<Long> getCauseCPUEclusiveTimeSumStats() {
		return causeCPUEclusiveTimeSumStats;
	}

	/**
	 * @return the affectedNodes
	 */
	public Collection<String> getAffectedNodes() {
		return affectedNodes.keySet();
	}

	public int getAffectedNodeCount(String affectedNode) {
		AffectedNodeData data = affectedNodes.get(affectedNode);
		if (data != null) {
			return data.getCount();
		}
		throw new IllegalArgumentException("Unknown affected node!");
	}

	public Date getAffectedNodeLastOccurrence(String affectedNode) {
		AffectedNodeData data = affectedNodes.get(affectedNode);
		if (data != null) {
			return data.getLastOccurrence();
		}
		throw new IllegalArgumentException("Unknown affected node!");
	}

	/**
	 * @return the count
	 */
	public int getNumOccurrences() {
		return count;
	}

	public double getSeverity() {
		double rtProportion = ((double) baseline) / ((double) causeEclusiveTimeSumStats.getMean());
		double countProportion = 1.0 / (double) count;
		double weightRT = 9.0;
		double weightCount = 1.0;
		return 1.0 - (weightRT * rtProportion + weightCount * countProportion) / (weightRT + weightCount);
	}

	public Date getLastOccurrenceDate() {
		long time = -1L;
		for (AffectedNodeData affNodeData : affectedNodes.values()) {
			if (affNodeData.getLastOccurrence().getTime() > time) {
				time = affNodeData.getLastOccurrence().getTime();
			}
		}
		return new Date(time);
	}

	public void addAntipatternInstance(AntipatternInstance antiIns) {
		// if (!antipatterns.isPresent()) {
		// antipatterns = Optional.of(new ArrayList<AntipatternInstance>());
		// }

		if (!antipatterns.contains(antiIns)) {
			antipatterns.add(antiIns);
		}

	}

	public List<AntipatternInstance> getAntipatternInstances() {
		return antipatterns;
	}

}
