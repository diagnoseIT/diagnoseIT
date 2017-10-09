package org.diagnoseit.standalone;

import java.util.Collection;
import java.util.Collections;

import org.diagnoseit.engine.DiagnosisEngine;
import org.diagnoseit.engine.DiagnosisEngineConfiguration;
import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.engine.session.ISessionResultCollector;
import org.diagnoseit.engine.session.SessionContext;
import org.diagnoseit.engine.tag.Tag;
import org.diagnoseit.engine.tag.TagState;
import org.diagnoseit.engine.tag.Tags;

public class DiagnoseITHelloWorld {
	public static void main(String[] args) {
		DiagnosisEngineConfiguration<String, String> configuration = new DiagnosisEngineConfiguration<>();
		configuration.setRuleClasses(Collections.singleton(AddWorldRule.class));
		configuration.setResultCollector(new HelloWorldResultCollector());
		configuration.setSessionCallback(new EngineCallback());

		DiagnosisEngine<String, String> engine = new DiagnosisEngine<String, String>(configuration);
		engine.analyze("Hello");
	}

	@Rule(name = "Add world to input rule")
	public static class AddWorldRule {
		@TagValue(type = Tags.ROOT_TAG)
		private String input;

		@Action(resultTag = "ADD_WORLD_TAG")
		public String action() {
			return input + " World";
		}
	}

	public static class HelloWorldResultCollector implements ISessionResultCollector<String, String> {
		public String collect(SessionContext<String> sessionContext) {
			Collection<Tag> tags = sessionContext.getStorage().mapTags(TagState.LEAF).values();
			for (Tag tag : tags) {
				if (tag.getType().equals("ADD_WORLD_TAG")) {
					return (String) tag.getValue();
				}
			}
			return null;
		}
	}

	public static class EngineCallback implements ISessionCallback<String> {
		public void onSuccess(String result) {
			System.out.println(result);
		}

		public void onFailure(Throwable throwable) {
		}
	}
}
