package org.diagnoseit.engine;

import org.diagnoseit.engine.session.SessionVariables;

/**
 * This is the core interface of the diagnosis engine.
 *
 * @param <I>
 *            The input type to be analyzed.
 * @author Claudio Waldvogel
 */
public interface IDiagnosisEngine<I> {

	/**
	 * Starts analyzing the given input.
	 *
	 * @param input
	 *            Any kind of object to be analyzed.
	 */
	void analyze(I input);

	/**
	 * Starts analyzing the given input with additional session specific variables.
	 *
	 * @param input
	 *            Any kind of object to be analyzed.
	 * @param variables
	 *            Variables to be available while processing the input.
	 */
	void analyze(I input, SessionVariables variables);

	/**
	 * Stops the engine and performs housekeeping.
	 *
	 * @param awaitShutdown
	 *            The flat to indicate if method will block until shutdown is complete.
	 * @throws Exception
	 *             in case of any error.
	 */
	void shutdown(boolean awaitShutdown) throws Exception;

}