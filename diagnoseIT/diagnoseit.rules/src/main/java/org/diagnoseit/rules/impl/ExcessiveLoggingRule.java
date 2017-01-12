package org.diagnoseit.rules.impl;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.diagnoseit.engine.rule.annotation.Action;
//import org.diagnoseit.engine.rule.annotation.Rule;
//import org.diagnoseit.engine.rule.annotation.TagValue;
//import org.diagnoseit.engine.tag.Tags;
//import org.diagnoseit.rules.RuleConstants;
//import org.spec.research.open.xtrace.api.core.Trace;
//import org.spec.research.open.xtrace.api.core.callables.Callable;
//import org.spec.research.open.xtrace.api.core.callables.LoggingInvocation;
//
///**
// * Rule for detecting excessive logging within a trace
// * 
// * @author Alper Hi
// *
// */
//@Rule(name = "ExcessiveLoggingRule")
public class ExcessiveLoggingRule {
//
//	private static final double EXCESSIVE_LOGGING_THRESHOLD = 0.03;
//
//	@TagValue(type = Tags.ROOT_TAG)
//	private Trace trace;
//
//	@Action(resultTag = RuleConstants.TAG_EXCESSIVE_LOGGING)
//	public boolean action() {
//		System.out.println("ExcessiveLoggingRule wird gefeuert!");
//		int amountOfCallablesInTrace = trace.size();
//		long amountOfLoggingInvocations = 0;
//		for (Callable callable : trace) {
//			if (callable instanceof LoggingInvocation) {
//				amountOfLoggingInvocations++;
//			}
//		}
//		if (amountOfLoggingInvocations > amountOfCallablesInTrace
//				* EXCESSIVE_LOGGING_THRESHOLD) {
//			System.out.println("Excessive Logging detected");
//			return true;
//		}
//		System.out.println("No Excessive Logging");
//		return false;
//	}
}
