package org.diagnoseit.rules.result;

import org.diagnoseit.rules.result.ProblemOccurrence.CauseStructure;
import org.diagnoseit.rules.util.AggregatedCallable;

import rocks.cta.api.core.Trace;
import rocks.cta.api.core.callables.Callable;

public class ProblemOccurrence {

	public ProblemOccurrence(Trace inputTrace, Callable globalContext,
			Callable problemContext, AggregatedCallable rootCauseInvocations,
			CauseStructure causeStructure) {
		// TODO Auto-generated constructor stub
	}

	public static class CauseStructure {
		private CauseType causeType;
		private int depth;

		/**
		 * @param causeType
		 * @param depth
		 */
		public CauseStructure(CauseType causeType, int depth) {
			super();
			this.causeType = causeType;
			this.depth = depth;
		}

		/**
		 * Gets {@link #causeType}.
		 *
		 * @return {@link #causeType}
		 */
		public CauseType getCauseType() {
			return causeType;
		}

		/**
		 * Sets {@link #causeType}.
		 *
		 * @param causeType
		 *            New value for {@link #causeType}
		 */
		public void setCauseType(CauseType causeType) {
			this.causeType = causeType;
		}

		/**
		 * Gets {@link #depth}.
		 *
		 * @return {@link #depth}
		 */
		public int getDepth() {
			return depth;
		}

		/**
		 * Sets {@link #depth}.
		 *
		 * @param depth
		 *            New value for {@link #depth}
		 */
		public void setDepth(int depth) {
			this.depth = depth;
		}

	}

	public static enum CauseType {
		SINGLE, ITERATIVE, RECURSIVE
	}
}
