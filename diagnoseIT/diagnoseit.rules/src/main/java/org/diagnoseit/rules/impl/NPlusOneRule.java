package org.diagnoseit.rules.impl;

import java.util.LinkedList;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.diagnoseit.traceservices.aggregated.AbstractAggregatedTimedCallable;
import org.spec.research.open.xtrace.api.core.TreeIterator;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.DatabaseInvocation;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

@Rule(name = "NPlusOneRule")
public class NPlusOneRule {
	private static final int NUMBER_OF_CALLS_THRESHOLD = 10;

	@TagValue(type = RuleConstants.TAG_PROBLEM_CAUSE)
	private AbstractAggregatedTimedCallable<? extends TimedCallable> cause;

	@Action(resultTag = RuleConstants.TAG_N_PLUS_ONE)
	public boolean action() {
		System.out.println("Executing NPlusOneRule..");
		
		if (cause.getType() != DatabaseInvocation.class) {
			return false;
		}

		NestingCallable parent = cause.getCallables().get(0).getParent()
				.getParent();
		if (parent == null) {
			return false;
		}

		boolean result = this.checkDatabaseProblem();

		if (result) {
			System.out.println("NPlusOneRule: N+1 anti-pattern detected");
		}

		return result;
	}

	public boolean checkDatabaseProblem() {

		if (cause.getType() != DatabaseInvocation.class) {
			return false;
		}

		LinkedList<DatabaseInvocation> listDatabaseInvocations = new LinkedList<DatabaseInvocation>();
		NestingCallable parent = cause.getCallables().get(0).getParent()
				.getParent();

		TreeIterator<Callable> callableIterator = parent.iterator();
		while (callableIterator.hasNext()) {
			Callable current = callableIterator.next();
			if (current instanceof DatabaseInvocation) {
				DatabaseInvocation databaseInvocation = (DatabaseInvocation) current;
				listDatabaseInvocations.add(databaseInvocation);
			}
		}
		return isNPlusOneProblem(listDatabaseInvocations);
	}

	public boolean isNPlusOneProblem(List<DatabaseInvocation> causeInvocations) {

		if (causeInvocations.size() < NUMBER_OF_CALLS_THRESHOLD + 1) {
			return false;
		}
		DatabaseInvocation firstInvocation = causeInvocations.get(0);
		DatabaseInvocation secondInvocation = causeInvocations.get(1);
		long amountOfNQueries = 0;

		if (!(secondInvocation.getSQLStatement().equals(firstInvocation
				.getSQLStatement()))) {
			for (int i = 2; i < causeInvocations.size(); i++) {
				DatabaseInvocation databaseInvocation = (DatabaseInvocation) causeInvocations
						.get(i);
				if (databaseInvocation.getSQLStatement().equals(
						secondInvocation.getSQLStatement())) {
					amountOfNQueries++;
				} else if (databaseInvocation.getSQLStatement().equals(
						firstInvocation.getSQLStatement())) {
					break;
				}
			}
		}
		if (amountOfNQueries > NUMBER_OF_CALLS_THRESHOLD) {
			System.out.println("NPlusOneRule: Amount of 'N' queries= " + amountOfNQueries);
			return true;
		}
		return false;
	}
}
