package com.evolveum.midpoint.prism;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

import java.io.File;
import java.io.IOException;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.AndFilter;
import com.evolveum.midpoint.prism.query.EqualsFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrFilter;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.prism.query.SubstringFilter;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_2.PolyStringType;

public class TestObjectQuery {
	
	@BeforeSuite
	public void setupDebug() throws SchemaException, SAXException, IOException {
		PrettyPrinter.setDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
		PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
	}
	
	@Test
	public void testMatchAndFilter() throws Exception{
		PrismObject user = PrismTestUtil.parseObject(new File("src/test/resources/common/user-jack.xml"));
		ObjectFilter filter = AndFilter.createAnd(EqualsFilter.createEqual(null,
				user.findItem(UserType.F_GIVEN_NAME).getDefinition(), "Jack"), SubstringFilter
				.createSubstring(null, user.findItem(UserType.F_FULL_NAME).getDefinition(), "arr"));
		boolean match = ObjectQuery.match(user, filter);
		AssertJUnit.assertTrue("filter does not match object", match);
	}
	
	
	@Test
	public void testMatchOrFilter() throws Exception{
		PrismObject user = PrismTestUtil.parseObject(new File("src/test/resources/common/user-jack.xml"));
		ObjectFilter filter = OrFilter.createOr(EqualsFilter.createEqual(null,
				user.findItem(UserType.F_GIVEN_NAME).getDefinition(), "Jack"), EqualsFilter.createEqual(null,
						user.findItem(UserType.F_GIVEN_NAME).getDefinition(), "Jackie"));

		boolean match = ObjectQuery.match(user, filter);
		AssertJUnit.assertTrue("filter does not match object", match);
	}
	
	@Test
	public void testDontMatchEqualFilter() throws Exception{
		PrismObject user = PrismTestUtil.parseObject(new File("src/test/resources/common/user-jack.xml"));
		ObjectFilter filter = EqualsFilter.createEqual(null,
						user.findItem(UserType.F_GIVEN_NAME).getDefinition(), "Jackie");

		boolean match = ObjectQuery.match(user, filter);
		AssertJUnit.assertFalse("filter matches object, but it should not", match);
	}
	
	@Test
	public void testComplexMatch() throws Exception{
		PrismObject user = PrismTestUtil.parseObject(new File("src/test/resources/common/user-jack.xml"));
		ObjectFilter filter = AndFilter.createAnd(EqualsFilter.createEqual(null,
				user.findItem(UserType.F_FAMILY_NAME).getDefinition(), "Sparrow"), SubstringFilter
				.createSubstring(null, user.findItem(UserType.F_FULL_NAME).getDefinition(), "arr"), OrFilter.createOr(EqualsFilter.createEqual(null,
						user.findItem(UserType.F_GIVEN_NAME).getDefinition(), "Jack"), EqualsFilter.createEqual(null,
								user.findItem(UserType.F_GIVEN_NAME).getDefinition(), "Jackie")));

		boolean match = ObjectQuery.match(user, filter);
		AssertJUnit.assertTrue("filter does not match object", match);
	}
	
	@Test
	public void testPolystringMatchEqualFilter() throws Exception{
		PrismObject user = PrismTestUtil.parseObject(new File("src/test/resources/common/user-jack.xml"));
		PolyString name = new PolyString("jack", "jack");
		ObjectFilter filter = EqualsFilter.createEqual(null,
						user.findItem(UserType.F_NAME).getDefinition(), name);

		boolean match = ObjectQuery.match(user, filter);
		AssertJUnit.assertTrue("filter does not match object", match);
	}
	

}
