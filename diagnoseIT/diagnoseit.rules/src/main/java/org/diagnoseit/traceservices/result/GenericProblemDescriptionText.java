package org.diagnoseit.traceservices.result;

import java.text.DecimalFormat;

public class GenericProblemDescriptionText {

	private static final String CAUSE_IDENTIFIER_KEY = "$1$";
	private static final String PROBLEM_CONTEXT_IDENTIFIER_KEY = "$2$";
	private static final String BT_IDENTIFIER_KEY = "$3$";
	private static final String CAUSE_COUNT_KEY = "$4$";
	private static final String CAUSE_DURATION_PROPORTION_KEY = "$5$";
	private static final String CAUSE_EXCL_TIME_SUM_KEY = "$6$";
	private static final String CAUSE_EXCL_CPU_TIME_SUM_KEY = "$7$";
	private static final String CAUSE_CPU_PROPORTION_KEY = "$8$";
	private static final String BT_DURATION_KEY = "$9$";
	private static final String EXEC_TYPE_TEXT_PART = "$10$";
	private static final String EMPH_START_KEY = "$s_emph$";
	private static final String EMPH_END_KEY = "$e_emph$";

	private static final String CAUSE_EXEC_AMOUNT_TEXT = "The cause " + CAUSE_IDENTIFIER_KEY + " has been executed " + EXEC_TYPE_TEXT_PART + " within the problem context "
			+ PROBLEM_CONTEXT_IDENTIFIER_KEY + ".";
	
	private static final String DURATION_CONSUMPTION_TEXT = "The cause " + CAUSE_IDENTIFIER_KEY + " consumes in average " + EMPH_START_KEY + CAUSE_DURATION_PROPORTION_KEY + EMPH_END_KEY + "% ("
			+ CAUSE_EXCL_TIME_SUM_KEY + "ms) of the response time (" + BT_DURATION_KEY + "ms) of the corresponding business transaction " + BT_IDENTIFIER_KEY + ".";
	
	private static final String CPU_PROPORTION_TEXT = "In average " + EMPH_START_KEY + CAUSE_CPU_PROPORTION_KEY + "%" + EMPH_END_KEY + " (" + CAUSE_EXCL_CPU_TIME_SUM_KEY + "ms) of the cause's "
			+ CAUSE_IDENTIFIER_KEY + " execution time (" + CAUSE_EXCL_TIME_SUM_KEY + "ms) is spent on CPU.";
	
	private static final String CPU_BOUND_TEXT = "In average " + EMPH_START_KEY + CAUSE_CPU_PROPORTION_KEY + "%" + EMPH_END_KEY + " (" + CAUSE_EXCL_CPU_TIME_SUM_KEY + "ms) of the cause's "
			+ CAUSE_IDENTIFIER_KEY + " execution time (" + CAUSE_EXCL_TIME_SUM_KEY + "ms) is spent on CPU. Hence, this performance problem is " + EMPH_START_KEY + "CPU bound!" + EMPH_END_KEY;

	private static final String CPU_NEUTRAL_TEXT = "The cause " + CAUSE_IDENTIFIER_KEY + " spends in average only " + EMPH_START_KEY + CAUSE_CPU_PROPORTION_KEY + "%" + EMPH_END_KEY + " ("
			+ CAUSE_EXCL_CPU_TIME_SUM_KEY + "ms) of the execution time (" + CAUSE_EXCL_TIME_SUM_KEY
			+ "ms) on the CPU. Hence, this performance problem is due to waiting times (for locks or remote calls to system parts that are not under observation).";
	
	private static final String CPU_NO_TEXT = "The cause " + CAUSE_IDENTIFIER_KEY + " spends "+EMPH_START_KEY+"no time"+EMPH_END_KEY+" on CPU. Hence, this performance problem is due to waiting times (for locks or remote calls to system parts that are not under observation).";

	private Class<?> causeType;
	private CauseExecutionType causeExecType;
	private int causeCount;
	private double durationContribution;
	private double cpuTimeContribution;
	private long causeExclTimeSum;
	private long causeExclCPUTimeSum;
	private long requestDuration;
	private long numOccurences;
	private long numAffectedTraces;
	private String businessTransactionIdentifier;
	private String problemContextIdentifier;
	private String causeIdentifier;
	private String emphStartTag = "";
	private String emphEndTag = "";
	private DecimalFormat format = new DecimalFormat("#.##");

	public void setEmphasizeTags(String startTag, String endTag) {
		emphStartTag = startTag;
		emphEndTag = endTag;
	}

	public GenericProblemDescriptionText() {

	}

	public String createOccurrencesText() {
		return "This problem instance occurred " + numOccurences + " times affecting <a href=\"affectedTraces\">" + numAffectedTraces + " traces</a>.";
	}
	
	public String createCauseExecutionAmountText() {
		String executionTextPart = null;
		switch (causeExecType) {
		case ITERATIVE:
			executionTextPart = emphStartTag + "iteratively" + emphEndTag + " in average " + emphStartTag + format.format(causeCount) + " times" + emphEndTag;
			break;
		case RECURSIVE:
			executionTextPart = emphStartTag + "recursively" + emphEndTag + " in average " + emphStartTag + format.format(causeCount) + " times" + emphEndTag;
			break;
		case SINGLE_CALL:
			executionTextPart = emphStartTag + "once" + emphEndTag;
			break;
		case SOME_FEW_CALLS:
			executionTextPart = emphStartTag + format.format(causeCount) + " times" + emphEndTag;
			break;
		case RECURSIVE_ITERATIVE_MIX:
			executionTextPart = emphStartTag + " in a mix of iterative and recursive calls" + emphEndTag + " in average" + emphStartTag + format.format(causeCount) + " times" + emphEndTag;
			break;
		default:
			break;
		}
		executionTextPart += " per problem occurrence";

		return CAUSE_EXEC_AMOUNT_TEXT.replace(CAUSE_IDENTIFIER_KEY, causeIdentifier).replace(EXEC_TYPE_TEXT_PART, executionTextPart).replace(PROBLEM_CONTEXT_IDENTIFIER_KEY, problemContextIdentifier)
				.replace(EMPH_START_KEY, emphStartTag).replace(EMPH_END_KEY, emphEndTag);
	}

	public String createDurationConsumptionText() {
		return DURATION_CONSUMPTION_TEXT.replace(CAUSE_IDENTIFIER_KEY, causeIdentifier).replace(CAUSE_DURATION_PROPORTION_KEY, format.format(durationContribution))
				.replace(CAUSE_EXCL_TIME_SUM_KEY, format.format(causeExclTimeSum)).replace(BT_DURATION_KEY, format.format(requestDuration))
				.replace(BT_IDENTIFIER_KEY, businessTransactionIdentifier).replace(EMPH_START_KEY, emphStartTag).replace(EMPH_END_KEY, emphEndTag);
	}

	public String createCPUTimeConsumtionText() {
		if (cpuTimeContribution < 3.0) {
			return CPU_NO_TEXT.replace(CAUSE_IDENTIFIER_KEY, causeIdentifier).replace(EMPH_START_KEY, emphStartTag).replace(EMPH_END_KEY, emphEndTag);
		} else
		if (cpuTimeContribution < 20.0) {
			return CPU_NEUTRAL_TEXT.replace(CAUSE_IDENTIFIER_KEY, causeIdentifier).replace(CAUSE_CPU_PROPORTION_KEY, format.format(cpuTimeContribution))
					.replace(CAUSE_CPU_PROPORTION_KEY, format.format(cpuTimeContribution)).replace(CAUSE_EXCL_CPU_TIME_SUM_KEY, format.format(causeExclCPUTimeSum))
					.replace(CAUSE_EXCL_TIME_SUM_KEY, format.format(causeExclTimeSum)).replace(EMPH_START_KEY, emphStartTag).replace(EMPH_END_KEY, emphEndTag);
		} else if (cpuTimeContribution > 70.0) {
			return CPU_BOUND_TEXT.replace(CAUSE_IDENTIFIER_KEY, causeIdentifier).replace(CAUSE_CPU_PROPORTION_KEY, format.format(cpuTimeContribution))
					.replace(CAUSE_CPU_PROPORTION_KEY, format.format(cpuTimeContribution)).replace(CAUSE_EXCL_CPU_TIME_SUM_KEY, format.format(causeExclCPUTimeSum))
					.replace(CAUSE_EXCL_TIME_SUM_KEY, format.format(causeExclTimeSum)).replace(EMPH_START_KEY, emphStartTag).replace(EMPH_END_KEY, emphEndTag);
		} else {
			return CPU_PROPORTION_TEXT.replace(CAUSE_IDENTIFIER_KEY, causeIdentifier).replace(CAUSE_CPU_PROPORTION_KEY, format.format(cpuTimeContribution))
					.replace(CAUSE_CPU_PROPORTION_KEY, format.format(cpuTimeContribution)).replace(CAUSE_EXCL_CPU_TIME_SUM_KEY, format.format(causeExclCPUTimeSum))
					.replace(CAUSE_EXCL_TIME_SUM_KEY, format.format(causeExclTimeSum)).replace(EMPH_START_KEY, emphStartTag).replace(EMPH_END_KEY, emphEndTag);
		}
	}

	/**
	 * @return the causeCount
	 */
	public int getCauseCount() {
		return causeCount;
	}

	/**
	 * @param causeCount
	 *            the causeCount to set
	 */
	public void setCauseCount(int causeCount) {
		this.causeCount = causeCount;
	}

	/**
	 * @return the durationContribution
	 */
	public double getDurationContribution() {
		return durationContribution;
	}

	/**
	 * @param durationContribution
	 *            the durationContribution to set
	 */
	public void setDurationContribution(double durationContribution) {
		this.durationContribution = durationContribution;
	}

	/**
	 * @return the cpuTimeContribution
	 */
	public double getCpuTimeContribution() {
		return cpuTimeContribution;
	}

	/**
	 * @param cpuTimeContribution
	 *            the cpuTimeContribution to set
	 */
	public void setCpuTimeContribution(double cpuTimeContribution) {
		this.cpuTimeContribution = cpuTimeContribution;
	}

	/**
	 * @return the causeExclTimeSum
	 */
	public long getCauseExclTimeSum() {
		return causeExclTimeSum;
	}

	/**
	 * @param causeExclTimeSum
	 *            the causeExclTimeSum to set
	 */
	public void setCauseExclTimeSum(long causeExclTimeSum) {
		this.causeExclTimeSum = causeExclTimeSum;
	}

	/**
	 * @return the causeExclCPUTimeSum
	 */
	public long getCauseExclCPUTimeSum() {
		return causeExclCPUTimeSum;
	}

	/**
	 * @param causeExclCPUTimeSum
	 *            the causeExclCPUTimeSum to set
	 */
	public void setCauseExclCPUTimeSum(long causeExclCPUTimeSum) {
		this.causeExclCPUTimeSum = causeExclCPUTimeSum;
	}

	/**
	 * @return the requestDuration
	 */
	public long getRequestDuration() {
		return requestDuration;
	}

	/**
	 * @param requestDuration
	 *            the requestDuration to set
	 */
	public void setRequestDuration(long requestDuration) {
		this.requestDuration = requestDuration;
	}

	/**
	 * @return the causeExecAmountText
	 */
	public static String getCauseExecAmountText() {
		return CAUSE_EXEC_AMOUNT_TEXT;
	}

	/**
	 * @return the durationConsumptionText
	 */
	public static String getDurationConsumptionText() {
		return DURATION_CONSUMPTION_TEXT;
	}

	/**
	 * @return the causeType
	 */
	public Class<?> getCauseType() {
		return causeType;
	}

	/**
	 * @param causeType
	 *            the causeType to set
	 */
	public void setCauseType(Class<?> causeType) {
		this.causeType = causeType;
	}

	/**
	 * @return the businessTransactionIdentifier
	 */
	public String getBusinessTransactionIdentifier() {
		return businessTransactionIdentifier;
	}

	/**
	 * @param businessTransactionIdentifier
	 *            the businessTransactionIdentifier to set
	 */
	public void setBusinessTransactionIdentifier(String businessTransactionIdentifier) {
		this.businessTransactionIdentifier = businessTransactionIdentifier;
	}

	/**
	 * @return the problemContextIdentifier
	 */
	public String getProblemContextIdentifier() {
		return problemContextIdentifier;
	}

	/**
	 * @param problemContextIdentifier
	 *            the problemContextIdentifier to set
	 */
	public void setProblemContextIdentifier(String problemContextIdentifier) {
		this.problemContextIdentifier = problemContextIdentifier;
	}

	/**
	 * @return the causeIdentifier
	 */
	public String getCauseIdentifier() {
		return causeIdentifier;
	}

	/**
	 * @param causeIdentifier
	 *            the causeIdentifier to set
	 */
	public void setCauseIdentifier(String causeIdentifier) {
		this.causeIdentifier = causeIdentifier;
	}

	/**
	 * @return the causeExecType
	 */
	public CauseExecutionType getCauseExecType() {
		return causeExecType;
	}

	/**
	 * @param causeExecType
	 *            the causeExecType to set
	 */
	public void setCauseExecType(CauseExecutionType causeExecType) {
		this.causeExecType = causeExecType;
	}

	/**
	 * @return the numOccurences
	 */
	public long getNumOccurences() {
		return numOccurences;
	}

	/**
	 * @param numOccurences the numOccurences to set
	 */
	public void setNumOccurences(long numOccurences) {
		this.numOccurences = numOccurences;
	}

	/**
	 * @return the numAffectedTraces
	 */
	public long getNumAffectedTraces() {
		return numAffectedTraces;
	}

	/**
	 * @param numAffectedTraces the numAffectedTraces to set
	 */
	public void setNumAffectedTraces(long numAffectedTraces) {
		this.numAffectedTraces = numAffectedTraces;
	}

}
