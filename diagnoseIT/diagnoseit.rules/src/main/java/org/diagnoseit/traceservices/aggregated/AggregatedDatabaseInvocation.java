package org.diagnoseit.traceservices.aggregated;

import org.spec.research.open.xtrace.api.core.callables.DatabaseInvocation;
import org.spec.research.open.xtrace.api.core.callables.TimedCallable;

public class AggregatedDatabaseInvocation extends AbstractAggregatedTimedCallable<DatabaseInvocation> {

	private static final String UNKNOWN = "unknown";
	
	private String dbProductName;
	private String dbProductVersion;
	private String dbUrl;
	private String sql;

	public AggregatedDatabaseInvocation() {

	}

	public AggregatedDatabaseInvocation(boolean keepCallables) {
		super(keepCallables);
	}

	public AggregatedDatabaseInvocation(DatabaseInvocation dbInvocation, boolean keepCallables) {
		super(dbInvocation, keepCallables);
	}

	public String getDBProductName() {
		return dbProductName;
	}

	public String getDBProductVersion() {
		return dbProductVersion;
	}

	public String getDBUrl() {
		return dbUrl;
	}

	public String getSQLStatement() {
		return sql;
	}

	@Override
	public void add(DatabaseInvocation dbInvocation) {
		if (sql == null) {
			dbProductName = dbInvocation.getDBProductName().orElse(UNKNOWN);
			dbProductVersion = dbInvocation.getDBProductVersion().orElse(UNKNOWN);
			dbUrl = dbInvocation.getDBUrl().orElse(UNKNOWN);
			sql = dbInvocation.getSQLStatement();
		}

	}

	public boolean canAggregate(DatabaseInvocation dbInvocation) {
		if (getCount() == 0) {
			return true;
		} else {
			return sql.equals(dbInvocation.getSQLStatement());
		}
	}

	@Override
	protected void add(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		if (sql == null) {
			AggregatedDatabaseInvocation aggDBInvocation = (AggregatedDatabaseInvocation) other;
			dbProductName = aggDBInvocation.dbProductName;
			dbProductVersion = aggDBInvocation.dbProductVersion;
			dbUrl = aggDBInvocation.dbUrl;
			sql = aggDBInvocation.sql;
		}
	}

	@Override
	protected boolean canAggregate(AbstractAggregatedTimedCallable<? extends TimedCallable> other) {
		AggregatedDatabaseInvocation aggDBInvocation = (AggregatedDatabaseInvocation) other;
		if (getCount() == 0) {
			return true;
		} else {
			return sql.equals(aggDBInvocation.sql);
		}
	}

}
