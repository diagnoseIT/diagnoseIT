package org.diagnoseit.engine.rule.exception;

import org.diagnoseit.engine.rule.ExecutionContext;

/**
 * Exception is raised if the execution of a rule fails.
 *
 * @author Claudio Waldvogel
 */
public class RuleExecutionException extends RuntimeException {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5382006828445473398L;

	/**
	 * Default constructor.
	 *
	 * @param message
	 *            The error message
	 * @param context
	 *            A reference to the failed {@link ExecutionContext}
	 * @see ExecutionContext
	 */
	public RuleExecutionException(String message, ExecutionContext context) {
		this(message, context, null);
	}

	/**
	 * Constructor that allows definition of the technical root cause.
	 *
	 * @param message
	 *            The error message
	 * @param context
	 *            A reference to the failed {@link ExecutionContext}
	 * @param cause
	 *            The technical root cause
	 * @see ExecutionContext
	 */
	public RuleExecutionException(String message, ExecutionContext context, Throwable cause) {
		super(prefix(message, context), cause);
	}

	/**
	 * Utility method to enrich the error message with.
	 *
	 * @param message
	 *            The error message
	 * @param context
	 *            The failed execution context
	 * @return Enriched error message
	 */
	private static String prefix(String message, ExecutionContext context) {
		return "Rule: \'" + context.getDefinition().getName() + "\' failed with error: " + message;
	}

}
