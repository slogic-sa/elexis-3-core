package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.model.issue.Visibility;
import ch.rgw.tools.JdbcLink;

public class Test_Query extends AbstractPersistentObjectTest {
	
	static final String FIRST_NAME = "first";
	static final String SECOND_NAME = "second";
	static final String THIRD_NAME = "third";
	
	private Organisation org1;
	private Organisation org2;
	private Organisation org3;
	private Reminder rem1;
	
	public Test_Query(JdbcLink link){
		super(link);
	}
	
	@Before
	public void before(){
		// create a instance of an PersistentObject ex. Organisation to test the query
		org1 = new Organisation(SECOND_NAME, SECOND_NAME + "_zusatz");
		org2 = new Organisation(FIRST_NAME, FIRST_NAME + "_zusatz");
		org2.set(Organisation.FLD_LAW_CODE, "law123");
		org3 = new Organisation(THIRD_NAME, THIRD_NAME + "_zusatz");
		rem1 = new Reminder(null, "20190201", Visibility.ALWAYS, null, null);

	}
	
	@After
	public void after(){
		org1.delete();
		org2.delete();
		org3.delete();
		rem1.delete();
	}
	
	@Test
	public void testConstructor(){
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		// clear will access the template which is set in constructor ...
		// if it does not fail with an exception the constructor worked ...
		query.clear();
		
		query = new Query<Organisation>(Organisation.class, Organisation.FLD_NAME1, FIRST_NAME);
		// clear will access the template which is set in constructor ...
		// if it does not fail with an exception the constructor worked ...
		query.clear();
	}
	
	@Test
	public void testConstructorFail(){
		try {
			new Query<PersistentObjectImpl>(PersistentObjectImpl.class);
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
		
		try {
			new Query<PersistentObjectImpl>(PersistentObjectImpl.class, "", "");
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
	}
	
	@Test
	public void testGetPreparedStatement(){
		PreparedStatement ps = getLink().getPreparedStatement(
			"SELECT " + Organisation.FLD_NAME1 + " FROM " + Organisation.TABLENAME);
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		ArrayList<String> result = query.execute(ps, new String[0]);
		int nrOrgs = result.size();
		Organisation organisation = new Organisation("NeueOrganistation", "Zusatznamen2");
		result = query.execute(ps, new String[0]);
		getLink().releasePreparedStatement(ps);
		assertEquals(nrOrgs + 1, result.size());
		organisation.delete();
	}
	
	@Test
	public void testOrderBy(){
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		query.clear();
		query.orderBy(false, Organisation.FLD_NAME1);
		List<Organisation> result = query.execute();
		assertEquals(3, result.size());
		assertEquals(FIRST_NAME, result.get(0).get(Organisation.FLD_NAME1));
		assertEquals(SECOND_NAME, result.get(1).get(Organisation.FLD_NAME1));
		assertEquals(THIRD_NAME, result.get(2).get(Organisation.FLD_NAME1));
		
		query.clear();
		query.orderBy(true, Organisation.FLD_NAME1);
		result = query.execute();
		assertEquals(3, result.size());
		assertEquals(FIRST_NAME, result.get(2).get(Organisation.FLD_NAME1));
		assertEquals(SECOND_NAME, result.get(1).get(Organisation.FLD_NAME1));
		assertEquals(THIRD_NAME, result.get(0).get(Organisation.FLD_NAME1));
	}
	
	@Test
	public void testOrderByReverse(){
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		query.clear();
		query.orderBy(true, Organisation.FLD_NAME1);
		List<Organisation> result = query.execute();
		assertEquals(3, result.size());
		assertEquals(FIRST_NAME, result.get(2).get(Organisation.FLD_NAME1));
		assertEquals(SECOND_NAME, result.get(1).get(Organisation.FLD_NAME1));
		assertEquals(THIRD_NAME, result.get(0).get(Organisation.FLD_NAME1));
	}
	
	@Test
	public void testExecute(){
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		query.clear();
		query.add(Organisation.FLD_NAME1, "=", FIRST_NAME);
		List<Organisation> result = query.execute();
		assertEquals(1, result.size());
	}
	
	@Ignore
	public void testExecuteOnDBConnection() throws IOException{
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		query.clear();
		query.add(Organisation.FLD_NAME1, "=", FIRST_NAME);
		
		// create a new DBConnection
		DBConnection connection = new DBConnection();
		connection.setDBConnectString("jdbc:h2:mem:test_query_mem");
		connection.setDBUser("sa");
		connection.setDBPassword("");
		assertTrue(connection.connect());
		//		initElexisDatabase(connection);
		
		List<Organisation> result = query.execute(connection);
		assertEquals(0, result.size());
		
		DBConnection initialConnection = PersistentObject.getDefaultConnection();
		
		// change default connection of PersistenObject and create an Organization
		PersistentObject.connect(connection);
		new Organisation(FIRST_NAME, "orgzusatz1");
		
		result = query.execute(connection);
		assertEquals(1, result.size());
		
		// cleanup new connection and reset to initial connection
		PersistentObject.disconnect();
		PersistentObject.connect(initialConnection);
	}
	
	@Test
	public void testQueryExpression(){
		PreparedStatement ps = getLink().prepareStatement(
			"SELECT " + Organisation.FLD_NAME1 + " FROM " + Organisation.TABLENAME);
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		ArrayList<String> result = query.execute(ps, new String[0]);
		int nrOrgs = result.size();
		System.out.println("Before creating new organistaion found " + nrOrgs + " Organisation");
		for (String s : result) {
			System.out.println("Organisation: found " + s);
		}
		Organisation organisation = new Organisation("NeueOrganistation", "Zusatznamen2");
		result = query.execute(ps, new String[0]);
		System.out
			.println("After creating new organistaion found " + result.size() + " Organisation");
		for (String s : result) {
			System.out.println("Organisation: found " + s);
		}
		assertEquals(nrOrgs + 1, result.size());
		organisation.delete();
	}
	
	@Test
	public void testQueryNotNullExpression(){
		Artikel art = new Artikel("TestARtikel", "Eigenartikel");
		art.set(Artikel.FLD_SUB_ID, "notNull");
		new Artikel("TestARtikel", "Eigenartikel");
		
		Query<Artikel> qbe = new Query<Artikel>(Artikel.class);
		qbe.startGroup();
		qbe.add(Artikel.FLD_SUB_ID, Query.NOT_EQUAL, null);
		qbe.or();
		qbe.add(Artikel.FLD_EAN, Query.NOT_EQUAL, null);
		qbe.add(Artikel.FLD_EXTID, Query.NOT_EQUAL, null);
		qbe.endGroup();
		List<Artikel> execute = qbe.execute();
		assertEquals(1, execute.size());
		
		art.delete();
	}
	
	@Test
	public void testQueryWithDefinedConditionEqualsAddedCondition() {
		Query<Artikel> qbe = new Query<Artikel>(Artikel.class);
		qbe.add(Artikel.FLD_SUB_ID, Query.EQUALS, "17");
		
		Query<Artikel> qbe2 = new Query<Artikel>(Artikel.class, Artikel.FLD_SUB_ID, "17");
		assertEquals(qbe.getActualQuery(), qbe2.getActualQuery());
	}
	
	@Test
	public void testQueryWithApostrophe() {
		Patient testPatient = new Patient("D'Andrea", "Max", "1.1.2000", "m");
		assertEquals("D'Andrea", testPatient.getName());
		Query<Person> qre = new Query<Person>(Person.class);
		qre.add(Person.FLD_NAME1, Query.EQUALS, "D'Andrea");
		List<Person> result = qre.execute();
		assertEquals(1, result.size());
		assertEquals("D'Andrea", result.get(0).getName());
		testPatient.delete();
	}
	
	@Test
	public void testQueryMappedExpression(){
		final String MappingName = "TitelSuffix";
		PreparedStatement ps = getLink().getPreparedStatement(
			"SELECT " + Organisation.FLD_NAME1 + " FROM " + Organisation.TABLENAME);
		// Setup Query which will return always true
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		query.clear();
		query.add(Organisation.FLD_COUNTRY, Query.LESS_OR_EQUAL, MappingName);
		System.out.println(
			"Must query via " + MappingName + ". Fieldname is " + Organisation.FLD_LAW_CODE);
		System.out.println("getActualQuery: " + query.getActualQuery());
		ArrayList<String> result = query.execute(ps, new String[0]);
		assertTrue(result.size() >= 3);
		// Setup Query which will return always false
		Query<Organisation> query2 = new Query<Organisation>(Organisation.class);
		query2.clear();
		query2.add(Organisation.FLD_COUNTRY, Query.GREATER, MappingName);
		List<Organisation> result2 = query2.execute();
		assertEquals(0, result2.size());
	}
	
	@Test
	public void testQueryCachingWithPrefetchWithPO() {
		PersistentObject.getDefaultConnection().getCache().clear();
		Query<Organisation> query = new Query<Organisation>(Organisation.class, Organisation.TABLENAME, false, new String[] {Organisation.FLD_MEDIPORT_SUPPORT, Organisation.FLD_LAW_CODE});
		query.clear();
		query.add(Organisation.FLD_NAME1, Query.EQUALS, FIRST_NAME);
		List<Organisation> result = query.execute();
		assertEquals(1, result.size());
		// FLD_LAW_CODE is mapped as TitelSuffix
		Organisation organisation = result.get(0);

		// check that the po field is in cache
		assertEquals("law123", organisation.getDBConnection().getCache().get(organisation.getKey(Organisation.FLD_LAW_CODE)));
		// check that the db field is not in cache because the fetch is done with the po field
		assertTrue(organisation.getDBConnection().getCache().get(organisation.getKey("TitelSuffix")) == null);
		// check db result with po field
		assertEquals("law123", organisation.get(Organisation.FLD_LAW_CODE));
		// check db result with db field
		assertEquals("law123", organisation.get("TitelSuffix"));
		// after first query with the db field - the db field is now in cache
		assertEquals("law123", organisation.getDBConnection().getCache().get(organisation.getKey("TitelSuffix")));
	}
	
	@Test
	public void testQueryCachingWithPrefetchDb() {
		PersistentObject.getDefaultConnection().getCache().clear();
		Query<Organisation> query = new Query<Organisation>(Organisation.class, Organisation.TABLENAME, false, new String[] {"TitelSuffix"});
		query.clear();
		query.add(Organisation.FLD_NAME1, Query.EQUALS, FIRST_NAME);
		List<Organisation> result = query.execute();
		assertEquals(1, result.size());
		// FLD_LAW_CODE is mapped as TitelSuffix
		Organisation organisation = result.get(0);
		
		// check that the po field is not in cache because the fetch is done with the db field
		assertTrue(organisation.getDBConnection().getCache().get(organisation.getKey(Organisation.FLD_LAW_CODE)) == null);
		// check that the db field is in cache
		assertEquals("law123", organisation.getDBConnection().getCache().get(organisation.getKey("TitelSuffix")));
	
		// check db result with po field
		assertEquals("law123", organisation.get(Organisation.FLD_LAW_CODE));
		// check db result with db field
		assertEquals("law123", organisation.get("TitelSuffix"));
		// after first query with the po field - the po field is now in cache
		assertEquals("law123", organisation.getDBConnection().getCache().get(organisation.getKey(Organisation.FLD_LAW_CODE)));
	}
	
	@Test
	public void testQueryMappedExpressionWithDate(){
		PersistentObject.getDefaultConnection().getCache().clear();
		Query<Reminder> query2 = new Query<Reminder>(Reminder.class, Reminder.TABLENAME, false, new String[] {Reminder.FLD_CREATOR, Reminder.FLD_DUE});
	
		List<Reminder> result2 = query2.execute();
		assertEquals(1, result2.size());

		// first look inside cache
		assertEquals("01.02.2019", rem1.getDBConnection().getCache().get(rem1.getKey(Reminder.FLD_DUE)));
		assertEquals("01.02.2019", rem1.get(Reminder.FLD_DUE));
		assertEquals(CoreHub.actUser.getId(), rem1.getDBConnection().getCache().get(rem1.getKey(Reminder.FLD_CREATOR)));
		assertEquals(CoreHub.actUser.getId(), rem1.get(Reminder.FLD_CREATOR));
		
		// clear cache check as usual
		PersistentObject.getDefaultConnection().getCache().clear();
		assertEquals("01.02.2019", rem1.get(Reminder.FLD_DUE));
		assertEquals("01.02.2019", rem1.getDBConnection().getCache().get(rem1.getKey(Reminder.FLD_DUE)));
		assertEquals(CoreHub.actUser.getId(), rem1.get(Reminder.FLD_CREATOR));
		assertEquals(CoreHub.actUser.getId(), rem1.getDBConnection().getCache().get(rem1.getKey(Reminder.FLD_CREATOR)));
	}
	
	@Test
	public void testQueryWithPrefetchInvalidMapping() {
		try {
			new Query<Organisation>(Organisation.class, Organisation.TABLENAME, false, new String[] {Organisation.FLD_MEDIPORT_SUPPORT, "notExistingField"});
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
	}
	
	private class PersistentObjectImpl extends PersistentObject {
		
		@SuppressWarnings("unused")
		public String getTestGet(){
			return "test";
		}
		
		@Override
		public String getLabel(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected String getTableName(){
			return null;
		}
		
	}
}
