package org.diagnoseit.standalone;

import java.util.Collections;
import java.util.List;

import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spec.research.open.xtrace.api.core.Trace;

import creator.TraceCreator;

/**
 * Launcher for rules that analyze a single trace.
 * 
 * @author Alper Hi
 *
 */
public class Launcher {

	/**
	 * Rules that should be executed.
	 */
	private static final String RULES_PACKAGE1 = "org.diagnoseit.rules.impl";

	/**
	 * For the Entwicklungsprojekt. Rules that analyze mobile traces.
	 */
	private static final String RULES_PACKAGE2 = "org.diagnoseit.rules.mobile.impl";

	
	/**
	 * Path to traces that should be analyzed.
	 */
	private static final String INTROSCOPE_FILE = "path to introscope trace file";

	private static final String DYNATRACE_FILE = "path to dynatrace trace file";

	private static final String INSPECTIT_FILE = "path to inspectit trace file";

	private static final String KIEKER_FILE = "path to kieker file";

	public static void main(String[] args) throws ClassNotFoundException {
		startLauncher();
	}

	public static void startLauncher() throws ClassNotFoundException {
		DiagnoseIT diagnoseIT = new DiagnoseIT(
				Collections.singletonList(RULES_PACKAGE2));
		diagnoseIT.init(new ResultHandler());

		/**
		 * Entwicklungsprojekt
		 */
		Trace trace = TraceCreator.getTestTrace1();

		System.out.println(trace.getRoot());
		
		/**
		 * Bachelorarbeit. Traces have to be first converted into OPEN.xtrace with an adapter.
		 */

		// DynatraceTraceImporter importer = new DynatraceTraceImporter();

		// IntroscopeTraceImporter importer = new IntroscopeTraceImporter();
		//
		// InspectITTraceConverter converter = new InspectITTraceConverter();
		//
		// TraceSink sink = null;
		//
		// converter.convertTraces(INSPECTIT_FILE, sink);

		// TraceConversion kiekerParser = new TraceConversion();
		// LinkedBlockingQueue<Trace> kiekerTraceQueue = new
		// LinkedBlockingQueue<Trace>();
		// try {
		// kiekerParser.runAnalysis(kiekerTraceQueue);
		// } catch (AnalysisConfigurationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Trace trace = importer.parse();

		// boolean useCreator = false;
		// Trace trace = null;
		// if (useCreator) {
		//
		// trace = (new TraceCreator()).createTrace();
		//
		// } else {
		//
		// try {
		// trace = importer.importTraceFromFile(INTROSCOPE_FILE);
		// } catch (IllegalStateException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// List<Trace> traces = null;
		// try {
		// traces = importer.importTracesFromFile(DYNATRACE_FILE);
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// Trace trace = traces.get(63);

		// IntroscopeTraceConverter converter = new IntroscopeTraceConverter();
		// MyTraceSink myTraceSink = new MyTraceSink();

		// Properties properties = new Properties();
		// properties.put("kieker.fileimporter.datapaths", KIEKER_FILE);
		// converter.initialize(properties, myTraceSink);
		// // converter.startTraceGeneration();
		// Trace firstTrace = converter.submitNextTrace();
		// System.out.println(myTraceSink.getAllTraces().get(0));
		// System.out.println(firstTrace);

		// converter.convertTraces(INTROSCOPE_FILE, myTraceSink);
		// List<Trace> traces = new LinkedList<Trace>();
		// traces = myTraceSink.getAllTraces();

		// try {
		// PrintWriter writer = new PrintWriter("test.txt", "UTF-8");
		// writer.println(traces.get(63).getRoot());
		// writer.close();
		// } catch (Exception e) {
		// // do something
		// }

		// try {
		// ObjectInputStream stream = new ObjectInputStream(
		// new FileInputStream(
		// "C:/Users/Alper Hi/Desktop/Universität/Bachelorarbeit/OPEN.xtraces/1190212523.xtrace"));
		// Trace trace = (Trace) stream.readObject();
		// System.out.println(trace);
		// stream.close();
		// } catch (IOException | ClassNotFoundException e) {
		// e.printStackTrace();
		// }
		// System.out.println(traces.get(0).getRoot());
		// long baseline = 1000L;
		// System.out.println("Hi");
		// System.out.println(traces.get(0));
		// System.out.println(traces.get(1));
		// System.out.println(traces.get(2));
		// System.out.println(traces.get(3));
		// System.out.println(traces.size());
		// System.out.println(traces.get(0));
		// diagnoseIT.diagnose(traces.get(63), baseline);

		// Trace trace1 = kiekerTraceQueue.poll();
		// Trace trace2 = kiekerTraceQueue.poll();
		// Trace trace3 = kiekerTraceQueue.poll();
		// Trace trace4 = kiekerTraceQueue.poll();
		// Trace trace5 = kiekerTraceQueue.poll();
		// diagnoseIT.diagnose(trace, baseline);
		// diagnoseIT.diagnose(trace2, baseline);
		// diagnoseIT.diagnose(trace3, baseline);
		// diagnoseIT.diagnose(trace4, baseline);
		// diagnoseIT.diagnose(trace5, baseline);

	}

	private static class ResultHandler implements
			ISessionCallback<List<ProblemOccurrence>> {
		/** The logger of this class. */
		private static final Logger log = LoggerFactory
				.getLogger(Launcher.class);

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(List<ProblemOccurrence> result) {
			System.out.println("Success!!");
			// TODO: Do Something with diagnosis result
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable t) {
			log.warn("Failed conducting diagnosis!", t);
		}
	}

}
