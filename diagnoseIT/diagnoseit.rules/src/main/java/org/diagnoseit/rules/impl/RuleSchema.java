package org.diagnoseit.rules.impl;

import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Condition;
import org.diagnoseit.engine.rule.annotation.SessionVariable;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.rule.annotation.TagValue.InjectionStrategy;

/**
 * This class does not do anything, actually.
 * It is used to explain how to write rules.
 * @author Dusan Okanovic
 *
 */
@Rule(name = "Rule name", description = "Some description", fireCondition = { "TagXY" })
public class RuleSchema {

	// InjectionStrategy.BY_TAG also possible
	@TagValue(type = "RequestedTag", injectionStrategy = InjectionStrategy.BY_VALUE)
	private String ValueOfRequestedTag;

	// optional = true also possible
	@SessionVariable(name = "SomeVariableToInject", optional = false)
	private String sessionVariableToInject;

	@Condition(name = "SomeCondition", hint = "This condition can be passed by ...")
	public boolean condition() {
		return true;
	}

	// Action.Quantity.MULTIPLE also possible
	@Action(resultTag = "resultOfThisRule", resultQuantity = Action.Quantity.SINGLE)
	public String action() {
		return "Some value, that is wrapped into the resultOfThisRule Tag";
	}
}
