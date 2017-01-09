package org.diagnoseit.traceservices.result;

import org.diagnoseit.traceservices.aggregated.AbstractAggregatedTimedCallable;
import org.spec.research.open.xtrace.api.core.callables.DatabaseInvocation;
import org.spec.research.open.xtrace.api.core.callables.HTTPRequestProcessing;
import org.spec.research.open.xtrace.api.core.callables.MethodInvocation;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

public class ProblemInstanceID {
	
	public static ProblemInstanceID createProblemInstanceId(TimedCallable problemContext, AbstractAggregatedTimedCallable<? extends TimedCallable> cause) {
		String businessTransactionStr = cause.getContainingSubTrace().getLocation().getBusinessTransaction().orElse(UNKNOWN);
		String entryPointStr = getIdentifier((TimedCallable)cause.getContainingSubTrace().getRoot());
		String nodeTypeStr = cause.getContainingSubTrace().getLocation().getNodeType().orElse(UNKNOWN);
		String problemContextStr = getIdentifier(problemContext);
		String causeStr = getIdentifier(cause.getCallables().get(0));
		return new ProblemInstanceID(businessTransactionStr,entryPointStr,nodeTypeStr,problemContextStr, causeStr);
	}
	
	private static String getIdentifier(TimedCallable tCallable) {
		String str = "";
		if (tCallable instanceof RemoteInvocation) {
			str = "REMOTE (" + ((RemoteInvocation) tCallable).getTarget() + ")";
		} else if (tCallable instanceof DatabaseInvocation) {
			str = "SQL (" + ((DatabaseInvocation) tCallable).getSQLStatement() + ")";
		} else if (tCallable instanceof MethodInvocation) {
			str = "METHOD (" + ((MethodInvocation) tCallable).getSignature() + ")";
		} else if (tCallable instanceof HTTPRequestProcessing) {
			str = "HTTP " + ((HTTPRequestProcessing) tCallable).getRequestMethod() + " (" + ((HTTPRequestProcessing) tCallable).getUri() + ")";
		}
		return str;
	}
	
	private static final String UNKNOWN = "unknown";
	private String businessTransaction = UNKNOWN;
	private String entryPoint = UNKNOWN;
	private String nodeType = UNKNOWN;
	private String problemContextMethod = UNKNOWN;
	private String problemCause = UNKNOWN;

	public ProblemInstanceID() {
		// TODO Auto-generated constructor stub
	}

	public ProblemInstanceID(String businessTransaction, String entryPoint, String nodeType, String problemContextMethod, String problemCause) {
		this.businessTransaction = businessTransaction;
		this.entryPoint = entryPoint;
		this.nodeType = nodeType;
		this.problemContextMethod = problemContextMethod;
		this.problemCause = problemCause;
	}

	/**
	 * @return the businessTransaction
	 */
	public String getBusinessTransaction() {
		return businessTransaction;
	}

	/**
	 * @param businessTransaction
	 *            the businessTransaction to set
	 */
	public void setBusinessTransaction(String businessTransaction) {
		this.businessTransaction = businessTransaction;
	}

	/**
	 * @return the nodeType
	 */
	public String getNodeType() {
		return nodeType;
	}

	/**
	 * @param nodeType
	 *            the nodeType to set
	 */
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

//	/**
//	 * @return the globalContextMethod
//	 */
//	public String getGlobalContextMethod() {
//		return globalContextMethod;
//	}
//
//	/**
//	 * @param globalContextMethod
//	 *            the globalContextMethod to set
//	 */
//	public void setGlobalContextMethod(String globalContextMethod) {
//		this.globalContextMethod = globalContextMethod;
//	}

	/**
	 * @return the problemContextMethod
	 */
	public String getProblemContextMethod() {
		return problemContextMethod;
	}

	/**
	 * @param problemContextMethod
	 *            the problemContextMethod to set
	 */
	public void setProblemContextMethod(String problemContextMethod) {
		this.problemContextMethod = problemContextMethod;
	}

	/**
	 * @return the problemCause
	 */
	public String getProblemCause() {
		return problemCause;
	}

	/**
	 * @param problemCause
	 *            the problemCause to set
	 */
	public void setProblemCause(String problemCause) {
		this.problemCause = problemCause;
	}

	/**
	 * @return the entryPoint
	 */
	public String getEntryPoint() {
		return entryPoint;
	}

	/**
	 * @param entryPoint
	 *            the entryPoint to set
	 */
	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((businessTransaction == null) ? 0 : businessTransaction.hashCode());
		result = prime * result + ((entryPoint == null) ? 0 : entryPoint.hashCode());
		result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
		result = prime * result + ((problemCause == null) ? 0 : problemCause.hashCode());
		result = prime * result + ((problemContextMethod == null) ? 0 : problemContextMethod.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		ProblemInstanceID other = (ProblemInstanceID) obj;
		if (businessTransaction == null) {
			if (other.businessTransaction != null)
				return false;
		} else if (!businessTransaction.equals(other.businessTransaction))
			return false;
		if (entryPoint == null) {
			if (other.entryPoint != null)
				return false;
		} else if (!entryPoint.equals(other.entryPoint))
			return false;
		if (nodeType == null) {
			if (other.nodeType != null)
				return false;
		} else if (!nodeType.equals(other.nodeType))
			return false;
		if (problemCause == null) {
			if (other.problemCause != null)
				return false;
		} else if (!problemCause.equals(other.problemCause))
			return false;
		if (problemContextMethod == null) {
			if (other.problemContextMethod != null)
				return false;
		} else if (!problemContextMethod.equals(other.problemContextMethod))
			return false;
		return true;
	}
	
	

}
