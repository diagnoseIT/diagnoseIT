package org.diagnoseit.rules.impl;


import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.LoggingInvocation;

/**
 * Rule for detecting the Phantom Logging anti-pattern
 * 
 * @author Alper Hi
 *
 */
@Rule(name = "PhantomLoggingRule")
public class PhantomLoggingRule {
	private static final LoggingLevel LOG_THRESHOLD = LoggingLevel.ERROR;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	private Logger log = Logger.getGlobal();

	/**
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_PHANTOM_LOGGING)
	public boolean action() {
		log.info("Executing PhantomLoggingRule.." + trace.getTraceId());
		for (Callable callable : trace) {
			if (callable instanceof LoggingInvocation) {
				LoggingInvocation loggingCallable = (LoggingInvocation) callable;
				if (loggingCallable.getLoggingLevel().isPresent()) {
					String logLevel = loggingCallable.getLoggingLevel().get();
					LoggingLevel level = LoggingLevel.valueOf(logLevel);
					if (level.level < LOG_THRESHOLD.level) {
						log.severe("PhantomLoggingRule: Phantom Log" + trace.getTraceId());
						return true;
					}
				}
			}
		}
		log.info("PhantomLoggingRule: No Phantom Log" + trace.getTraceId());
		return false;
	}

	enum LoggingLevel {
		ALL(0), TRACE(1), DEBUG(2), INFO(3), WARN(4), ERROR(5), FATAL(6), OFF(7);
		int level;

		LoggingLevel(int level) {
			this.level = level;
		}
	}
}
