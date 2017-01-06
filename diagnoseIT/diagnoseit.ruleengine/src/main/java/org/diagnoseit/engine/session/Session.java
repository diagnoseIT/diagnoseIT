package org.diagnoseit.engine.session;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.diagnoseit.engine.util.ReflectionUtils.tryInstantiate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import org.diagnoseit.engine.DiagnosisEngineConfiguration;
import org.diagnoseit.engine.IDiagnosisEngine;
import org.diagnoseit.engine.rule.FireCondition;
import org.diagnoseit.engine.rule.RuleDefinition;
import org.diagnoseit.engine.rule.RuleInput;
import org.diagnoseit.engine.rule.RuleOutput;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.factory.Rules;
import org.diagnoseit.engine.rule.store.DefaultRuleOutputStorage;
import org.diagnoseit.engine.rule.store.IRuleOutputStorage;
import org.diagnoseit.engine.session.exception.SessionException;
import org.diagnoseit.engine.tag.Tag;
import org.diagnoseit.engine.tag.Tags;

/**
 * The Session is the core class of the {@link IDiagnosisEngine}. It executes all rules, stores
 * interim results, and prepares the final results by utilizing the {@link ISessionResultCollector}.
 * To ensure a proper execution it defines a explicit life cycle. To ensure a compliance with the
 * life cycle the current state of session is held in a {@link State} object. Additional runtime
 * information is stored in a {@link SessionContext}. <p\>
 *
 * <pre>
 * The life if a session is as follows:
 * <ul>
 *     <li>{@link #activate(Object)}
 *    Prepares the session for the next execution.
 *     </li>
 *     <li>{@link #process()}
 *     Executes all rules until no more rule can be executed.
 *     </li>
 *     <li>{@link #collectResults()}
 *     Invokes the {@link ISessionResultCollector} to gather and provide results.
 *     </li>
 *     <li>{@link #passivate()}
 *     Cleans the session and removes all data from the latest execution. The session is now ready to be reactivated by invoking {@link #activate(Object)} again.
 *     </li>
 *     <li>{@link #destroy()}
 *     Destroys the session. Session can not be revived anymore.
 *     </li>
 * </ul>
 * </pre>
 * <p>
 * In order to facilitate compliance with the life cycle it is strongly recommended to use the
 * provided {@link SessionPool} in combination with {@link ExecutorService}.
 * <p>
 *
 * <pre>
 * {
 * 	&#64;code
 * 	SessionPool<String, DefaultSessionResult<String>> pool = new SessionPool<>(configuration);
 *
 * 	Session<String, DefaultSessionResult<String>> session = pool.borrowObject("Input", new SessionVariables());
 *
 * 	DefaultSessionResult<String> result = executorService.submit(input).get();
 * }
 * </pre>
 *
 * @param <I>
 *            The type of input to be analyzed.
 * @param <R>
 *            The expected result type.
 * @author Claudio Waldvogel
 * @see IDiagnosisEngine
 * @see ISessionResultCollector
 * @see SessionContext
 */
public final class Session<I, R> implements Callable<R> {

	/**
	 * The slf4j Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Session.class);

	/**
	 * The current state of this Session. A session can enter 6 states:
	 * <p>
	 * NEW, ACTIVATED, PROCESSED, PASSIVATED, DESTROYED, FAILURE
	 *
	 * @see State
	 */
	private State state = State.NEW;

	/**
	 * The {@link SessionContext} which is associated with this Session. It is created, executed,
	 * and destroyed in accordance with the session itself.
	 */
	private SessionContext<I> sessionContext;

	/**
	 * The {@link ExecutorService} executing the rules. The {@link ExecutorService} is configurable
	 * from the {@link DiagnosisEngineConfiguration}.
	 */
	private ExecutorService executor;

	/**
	 * The {@link ISessionResultCollector} which produces the results of a session execution. The
	 * {@link ISessionResultCollector} is configurable from the {@link DiagnosisEngineConfiguration}
	 * .
	 */
	private ISessionResultCollector<I, R> resultCollector;

	/**
	 * Delay to wait until ExecutorService is shut down.
	 */
	private int shutDownTimeout;

	// -------------------------------------------------------------
	// Methods: Construction
	// -------------------------------------------------------------

	/**
	 * Private Constructor. Access only granted to Builder
	 */
	Session() {
	}

	/**
	 * Creates a new {@link Builder} instance. The <code>Builder</code> is the only component which
	 * is allowed to create new Session instances.
	 *
	 * @param <I>
	 *            The input type
	 * @param <R>
	 *            The result type
	 * @return A new Builder instance
	 */
	public static <I, R> Builder<I, R> builder() {
		return new Builder<>();
	}

	/**
	 * Builder to create Sessions.
	 *
	 * @param <I>
	 *            The input type of the Session
	 * @param <R>
	 *            The result type this Session produces
	 */
	public static final class Builder<I, R> {

		/**
		 * Creates a new Builder.
		 */
		Builder() {
		}

		/**
		 * The amount of threads to executed rules in parallel.
		 */
		private int numRuleWorkers = 1;

		/**
		 * The delay in seconds to await a proper shutdown of the session.
		 */
		private int shutDownTimeout = 2;

		/**
		 * The {@code RuleDefinition}s to be executed.
		 */
		private Set<RuleDefinition> ruleDefinitions;

		/**
		 * The {@link IRuleOutputStorage} implementation to be used.
		 */
		private Class<? extends IRuleOutputStorage> storageClass = DefaultRuleOutputStorage.class;

		/**
		 * The {@link ISessionResultCollector} to be used.
		 */
		private ISessionResultCollector<I, R> sessionResultCollector;

		/**
		 * Sets {@link #numRuleWorkers}.
		 *
		 * @param numRuleWorkers
		 *            New value for {@link #numRuleWorkers}
		 * @return The Builder itself
		 */
		public Builder<I, R> setNumRuleWorkers(int numRuleWorkers) {
			this.numRuleWorkers = numRuleWorkers;
			return this;
		}

		/**
		 * Sets {@link #shutDownTimeout}.
		 *
		 * @param shutDownTimeout
		 *            New value for {@link #shutDownTimeout}
		 * @return The Builder itself
		 */
		public Builder<I, R> setShutDownTimeout(int shutDownTimeout) {
			this.shutDownTimeout = shutDownTimeout;
			return this;
		}

		/**
		 * Sets {@link #ruleDefinitions}.
		 *
		 * @param ruleDefinitions
		 *            New value for {@link #ruleDefinitions}
		 * @return The Builder itself
		 */
		public Builder<I, R> setRuleDefinitions(Set<RuleDefinition> ruleDefinitions) {
			this.ruleDefinitions = ruleDefinitions;
			return this;
		}

		/**
		 * Sets {@link #storageClass}.
		 *
		 * @param storageClass
		 *            New value for {@link #storageClass}
		 * @return The Builder itself
		 */
		public Builder<I, R> setStorageClass(Class<? extends IRuleOutputStorage> storageClass) {
			this.storageClass = storageClass;
			return this;
		}

		/**
		 * Sets {@link #sessionResultCollector}.
		 *
		 * @param sessionResultCollector
		 *            New value for {@link #sessionResultCollector}
		 * @return The Builder itself
		 */
		public Builder<I, R> setSessionResultCollector(ISessionResultCollector<I, R> sessionResultCollector) {
			this.sessionResultCollector = sessionResultCollector;
			return this;
		}

		/**
		 * Constructs the session.
		 *
		 * @return A new session instance
		 */
		public Session<I, R> build() {
			// sanity checks
			checkNotNull(ruleDefinitions);
			checkNotNull(storageClass);
			checkNotNull(sessionResultCollector);
			numRuleWorkers = numRuleWorkers > 0 ? numRuleWorkers : 1;
			shutDownTimeout = shutDownTimeout >= 2 ? shutDownTimeout : 2;

			// Create a new Session
			Session<I, R> session = new Session<>();
			session.executor = Executors.newFixedThreadPool(numRuleWorkers);
			session.shutDownTimeout = shutDownTimeout;
			// Create a new SessionContext and instantiate the IRuleOutputStorage
			session.sessionContext = new SessionContext<>(ruleDefinitions, tryInstantiate(storageClass));
			session.resultCollector = sessionResultCollector;
			return session;
		}
	}

	// -------------------------------------------------------------
	// Interface Implementation: Callable
	// -------------------------------------------------------------

	@Override
	public R call() throws Exception {
		// Processes and collect results.
		// If a Session is used as Callable this call might horribly fail if sessions are not
		// retrieved from SessionPool and a sessions lifeCycle is neglected. But we have not chance
		// to activate a
		// session internally due to missing input information. So simply fail
		return process().collectResults();
	}

	// -------------------------------------------------------------
	// Methods: LifeCycle -> reflects the life cycle of a
	// org.apache.commons.pool.impl.GenericObjectPool
	// -------------------------------------------------------------

	/**
	 * Tries to activate the Session for the given input. Activation means that state is changes to
	 * ACTIVATED and SessionContext is activated as well. If a session depends on
	 * {@link SessionVariables} use {@link #activate(Object, SessionVariables)}. Activation is only
	 * possible if the session is currently in NEW or PASSIVATED state, any other state forces a
	 * SessionException.
	 *
	 * @param input
	 *            The input to be processed.
	 * @return The Session itself
	 * @see SessionException
	 */
	public Session<I, R> activate(I input) {
		return activate(input, new SessionVariables());
	}

	/**
	 * Tries to activate the Session for the given input object and SessionVariables. Activation
	 * means that state is changes to ACTIVATED and SessionContext is activated as well. Activation
	 * is only possible if the session is currently in NEW or PASSIVATED state, any other state
	 * forces a SessionException.
	 *
	 * @param input
	 *            The input to be processed.
	 * @param variables
	 *            The SessionVariables to be used
	 * @return The Session itself
	 * @throws SessionException
	 * @see SessionVariables
	 * @see SessionVariable
	 * @see SessionException
	 */
	public Session<I, R> activate(I input, SessionVariables variables) {
		switch (state) {
		case NEW:
		case PASSIVATED:
			// All we need to do is to reactivate the SessionContext
			sessionContext.activate(input, variables);
			state = State.ACTIVATED;
			break;
		case DESTROYED:
			throw new SessionException("Session already destroyed.");
		case FAILURE:
		default:
			throw new SessionException("Session can not enter ACTIVATED stated from: " + state + " state. Ensure Session is in NEW or PASSIVATED state when activating.");
		}
		return this;
	}

	/**
	 * Executes the Session. Invocation is exclusively possible if {@link Session} is in ACTIVATED
	 * state, any other state forces a {@link SessionException}. Processing is enabled be inserting
	 * a initial RuleOutput to the {@link IRuleOutputStorage} which will act as input to further
	 * rules. If processing completes without errors the {@link Session} enters PROCESSED state. In
	 * any case of error it enters FAILURE state.
	 *
	 * @return The Session itself
	 * @throws SessionException
	 */
	public Session<I, R> process() {
		switch (state) {
		case ACTIVATED:
			sessionContext.getStorage().store(Rules.triggerRuleOutput(sessionContext.getInput()));
			doProcess();
			state = State.PROCESSED;
			break;
		default:
			throw new SessionException("Session can not enter process stated from: " + state + "state. Ensure that Session is in ACTIVATED state before processing.");
		}
		return this;
	}

	/**
	 * Cleans the {@link Session} by means of cleaning the {@link SessionContext} and removing all
	 * stale data. Valid transitions to PASSIVATED state are from PROCESSED and Failure.
	 *
	 * @return The Session itself/
	 */
	public Session<I, R> passivate() {
		if (!state.equals(State.PROCESSED)) {
			LOG.warn("Not processed Session gets passivated!");
		}
		// Passivate is always possible. Also it is important to passivate the Session in any case.
		// This ensures a reusable clean Session.
		sessionContext.passivate();
		state = State.PASSIVATED;
		return this;
	}

	/**
	 * Destroys this session. If the session was not yet passivated, it will be passivated in
	 * advance. While the session is destroyed, the ExecutorService is shutdown and the
	 * SessionContext is destroyed. After the session is destroyed it is unusable!
	 *
	 */
	public void destroy() {
		switch (state) {
		case PROCESSED:
			// We can destroy the session but it was not yet passivated. To stay in sync with the
			// state lifeCycle we passivate first
			passivate();
			break;
		case DESTROYED:
			// Already destroyed. Warn?
			return;
		case NEW:
		case ACTIVATED:
			LOG.warn("Session is destroy before it was processed.");
			break;
		default:
			break;
		}
		try {
			executor.shutdown();
			if (!executor.awaitTermination(shutDownTimeout, TimeUnit.SECONDS)) {
				LOG.error("Session Executor did not shut down within: {} seconds.", shutDownTimeout);
			}
		} catch (InterruptedException e) {
			throw new SessionException("Failed to destroy Session", e);
		} finally {
			// ensure context is properly destroyed and all collected data is wiped out
			sessionContext.destroy();
		}
	}

	/**
	 * Marks the session as failed and passivates it.
	 *
	 * @param cause
	 *            The root cause of failure.
	 * @throws SessionException
	 */
	private void failure(Exception cause) {
		// enter failure state
		state = State.FAILURE;
		// ensure that Session gets passivated to enable reuse
		passivate();
		// Propagate the cause of failure
		throw new SessionException("Diagnosis Session failed with error(s)", cause);
	}

	/**
	 * Starts collecting all gathered results. The result collection process is delegated to the
	 * {@link ISessionResultCollector}.
	 *
	 * @return Depends on the {@link ISessionResultCollector} implementation.
	 */
	public R collectResults() {
		return resultCollector.collect(sessionContext);
	}

	/**
	 * Gets {@link #state}.
	 *
	 * @return {@link #state}
	 */
	State getState() {
		return state;
	}

	/**
	 * Gets {@link #sessionContext}.
	 *
	 * @return {@link #sessionContext}
	 */
	SessionContext<I> getSessionContext() {
		return sessionContext;
	}

	// -------------------------------------------------------------
	// Methods: Internals
	// -------------------------------------------------------------

	/**
	 * Internal processing routine to execute all rules. This methods blocks as long as further
	 * rules can be executed. If this method returns it is assured that all possible rules are
	 * executed and all possible results are available in the IRuleOutputStorage.
	 */
	private void doProcess() {
		Collection<RuleExecution> nextRules = findNextRules();
		while (!nextRules.isEmpty()) {
			try {
				// 1. invoke next set of rules
				List<Future<Collection<RuleOutput>>> futures = executor.invokeAll(nextRules);
				// 2. iterate over all created futures
				for (Future<Collection<RuleOutput>> future : futures) {
					try {
						// 3. block till a of RuleOutputs is received
						Collection<RuleOutput> outputs = future.get();
						// 4. store outputs in IRuleOutputStorage
						sessionContext.getStorage().store(outputs);
					} catch (Exception ex) {
						// ensure a proper exception handling
						failure(ex);
					}
				}
			} catch (InterruptedException ex) {
				throw new SessionException("Failed to retrieve RuleOutput", ex);
			}
			// Fetch the next executable rules
			nextRules = findNextRules();
		}
	}

	/**
	 * Utility method to determine the next executable rules. The next rules are determined by
	 * comparing all, so far collected, types of tags in {@link IRuleOutputStorage} and the
	 * {@link FireCondition} of each {@link RuleDefinition}.
	 *
	 * @return Collection of {@link RuleExecution}s.
	 * @see org.diagnoseit.engine.rule.FireCondition
	 * @see RuleDefinition
	 * @see IRuleOutputStorage
	 */
	private Collection<RuleExecution> findNextRules() {
		Set<String> available = sessionContext.getStorage().getAvailableTagTypes();
		Set<RuleExecution> nextRules = new HashSet<>();
		Iterator<RuleDefinition> iterator = sessionContext.getRuleSet().iterator();
		while (iterator.hasNext()) {
			RuleDefinition rule = iterator.next();
			if (rule.getFireCondition().canFire(available)) {
				// wrap RuleDefinition in a callable object to be used with ExecutorService
				nextRules.add(new RuleExecution(rule));
				iterator.remove();
			}
		}
		return nextRules;
	}

	/**
	 * Collects all available inputs for a single {@link RuleDefinition}. Each RuleInput is
	 * equivalent to an execution of the RuleInput.
	 *
	 * @param definition
	 *            The {@link RuleDefinition} to be executed.
	 * @return A Collection of RuleInputs
	 * @see RuleInput
	 * @see RuleDefinition
	 */
	private Collection<RuleInput> collectInputs(RuleDefinition definition) {
		Set<String> requiredInputTags = definition.getFireCondition().getTagTypes();
		Collection<RuleOutput> leafOutputs = sessionContext.getStorage().findLatestResultsByTagType(requiredInputTags);
		Set<RuleInput> inputs = Sets.newHashSet();
		// A single can produce n inputs. Each embedded tag in ruleOutput.getTags() will be
		// reflected in a new RuleInput
		// Although this is an O(n²) loop the iterated lists are expected to be rather short.
		// Also the nested while loop is expected to be very short.
		for (RuleOutput output : leafOutputs) {
			for (Tag leafTag : output.getTags()) {
				Collection<Tag> tags = Tags.unwrap(leafTag, requiredInputTags);
				if (tags.size() != requiredInputTags.size()) {
					LOG.warn("Invalid Value definitions for {}. All values must be reachable from the latest Tag " + "value.", definition.getName());
				} else {
					// Create and store a new RuleInput
					inputs.add(new RuleInput(leafTag, tags));
				}
			}
		}
		return inputs;
	}

	// -------------------------------------------------------------
	// Inner classes
	// -------------------------------------------------------------

	/**
	 * Utility class to wrap {@link RuleDefinition#execute(RuleInput, SessionVariables)} into a
	 * <code>Callable</code>.
	 */
	private class RuleExecution implements Callable<Collection<RuleOutput>> {

		/**
		 * The {@link RuleDefinition} to be executed}.
		 */
		private final RuleDefinition definition;

		/**
		 * Default Constructor.
		 *
		 * @param definition
		 *            {@link RuleDefinition} to be executed}.
		 */
		RuleExecution(RuleDefinition definition) {
			this.definition = definition;
		}

		// -------------------------------------------------------------
		// Interface Implementation: Callable
		// -------------------------------------------------------------

		@Override
		public Collection<RuleOutput> call() throws Exception {
			return definition.execute(collectInputs(definition), Session.this.sessionContext.getSessionVariables());
		}
	}

	/**
	 * Internal enum representing the current state of this session.
	 */
	enum State {
		/**
		 * The initial State of each Session.
		 */
		NEW,

		/**
		 * The state as soon as an {@link Session} gets activated. This stated can be entered from
		 * <code>NEW</code> and <code>PASSIVATED</code> states.
		 */
		ACTIVATED,

		/**
		 * An {@link Session} enters the <code>PROCESSING</code> state after all applicable rules
		 * were executed.
		 */
		PROCESSED,
		/**
		 * An {@link Session} can enter the <code>PASSIVATED</code> stated only from
		 * <code>PROCESSED</code> stated. <code>PASSIVATED</code> is the only state which enables a
		 * transition back to <code>ACTIVATED</code>.
		 */
		PASSIVATED,

		/**
		 * {@link Session} is destroyed and not longer usable.
		 */
		DESTROYED,

		/**
		 * {@link Session} encountered an error and is in a failure stated.
		 */
		FAILURE
	}
}
