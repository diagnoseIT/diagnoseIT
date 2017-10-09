package org.diagnoseit.traceservices.result;

import java.util.Date;

public class AffectedNodeData {
	private int count = 1;
	private Date lastOccurrence;

	public AffectedNodeData() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param lastOccurrence
	 */
	public AffectedNodeData(Date lastOccurrence) {
		this.setLastOccurrence(lastOccurrence);
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the lastOccurrence
	 */
	public Date getLastOccurrence() {
		return lastOccurrence;
	}

	/**
	 * @param lastOccurrence
	 *            the lastOccurrence to set
	 */
	public void setLastOccurrence(Date lastOccurrence) {
		this.lastOccurrence = lastOccurrence;
	}

}
