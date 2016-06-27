package org.diagnoseit.engine.session;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.diagnoseit.engine.rule.ConditionFailure;
import org.diagnoseit.engine.rule.RuleOutput;
import org.diagnoseit.engine.tag.TagState;

/**
 * The default implementation of ISessionResultCollector. This implementation produces {@link DefaultSessionResult}.
 *
 * @param <I>
 * 		The original input type
 * @author Claudio Waldvogel
 * @see DefaultSessionResult
 */
public class DefaultSessionResultCollector<I> implements ISessionResultCollector<I, DefaultSessionResult<I>> {

	//-------------------------------------------------------------
	// Interface Implementation: ISessionResultCollector
	//-------------------------------------------------------------

	@Override
	public DefaultSessionResult<I> collect(SessionContext<I> context) {
		Multimap<String, ConditionFailure> conditionFailures = ArrayListMultimap.create();
		//unpack condition errors
		for (RuleOutput output : context.getStorage().getAllOutputsWithConditionFailures().values()) {
			conditionFailures.putAll(output.getRuleName(), output.getConditionFailures());
		}
		return new DefaultSessionResult<>(context.getInput(), conditionFailures, context.getStorage().mapTags(TagState.LEAF));
	}

}
