package org.diagnoseit.rules.impl;

import java.util.HashMap;
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

/**
 * Rule for detecting Stifle anti-pattern
 * 
 * @author Alper Hi
 *
 */
@Rule(name = "TheStifleRule")
public class TheStifleRule {

	private static final int NUMBER_OF_CALLS_THRESHOLD = 10;

	@TagValue(type = RuleConstants.TAG_PROBLEM_CAUSE)
	private AbstractAggregatedTimedCallable<? extends TimedCallable> cause;

	@TagValue(type = RuleConstants.TAG_N_PLUS_ONE)
	private boolean resultOfNPlusOneRule;

	@Action(resultTag = RuleConstants.TAG_THE_STIFLE)
	public boolean action() {
		System.out.println("Executing TheStifleRule..");
		if (resultOfNPlusOneRule == true) {
			System.out.println("TheStifleRule: N+1 detected, so there is also the Stifle");
			return true;
		}

		boolean result = this.checkDatabaseProblem();

		if (result) {
			System.out.println("TheStifleRule: The Stifle detected");
		} else {
			System.out.println("TheStifleRule: No Stifle");
		}

		return result;

	}

	public boolean checkDatabaseProblem() {

		if (cause.getType() != DatabaseInvocation.class) {
			return false;
		}

		LinkedList<TimedCallable> listDatabaseInvocations = new LinkedList<TimedCallable>();
		NestingCallable parent = cause.getCallables().get(0).getParent()
				.getParent();

		if (parent != null) {
			TreeIterator<Callable> callableIterator = parent.iterator();
			while (callableIterator.hasNext()) {
				Callable current = callableIterator.next();
				if (current instanceof DatabaseInvocation) {
					DatabaseInvocation databaseInvocation = (DatabaseInvocation) current;
					listDatabaseInvocations.add(databaseInvocation);
				}
			}
		} else {
			for (Callable callable : cause.getCallables()) {
				if (callable instanceof DatabaseInvocation) {
					DatabaseInvocation databaseInvocation = (DatabaseInvocation) callable;
					listDatabaseInvocations.add(databaseInvocation);
				}
			}
		}

		return isStifleProblem(listDatabaseInvocations);
	}

	public boolean isStifleProblem(
			List<? extends TimedCallable> causeInvocations) {

		if (causeInvocations.size() < NUMBER_OF_CALLS_THRESHOLD + 1) {
			return false;
		}

		HashMap<String, Long> queryMap = new HashMap<String, Long>();

		for (TimedCallable invocation : causeInvocations) {
			DatabaseInvocation databaseInvocation = (DatabaseInvocation) invocation;
			String sqlCommand = databaseInvocation.getSQLStatement();

			if (queryMap.containsKey(sqlCommand)) {
				queryMap.put(sqlCommand, queryMap.get(sqlCommand) + 1);
			} else {
				long amount = 1;
				queryMap.put(sqlCommand, amount);
			}
		}

		boolean isStifle = false;

		for (long amount : queryMap.values()) {
			System.out.println("amount= " + amount);
			if (amount > NUMBER_OF_CALLS_THRESHOLD) {
				isStifle = true;
			}
		}
		return isStifle;

	}
}
