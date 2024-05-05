package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.foo.RoleType;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import static com.evolveum.midpoint.prism.PrismInternalTestUtil.DEFAULT_NAMESPACE_PREFIX;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by Dominik.
 */
public class TestSchemaContext extends AbstractPrismTest {

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrettyPrinter.addDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
    }


    @Test
    public void typePropertyContextResolverTest() throws SchemaException, IOException {
        QName userType = new QName(PrismInternalTestUtil.NS_FOO, "UserType");
        QName roleType = new QName(PrismInternalTestUtil.NS_FOO, "RoleType");

        PrismObject<RoleType> roleProxy = getPrismContext().parseObject(new File(PrismInternalTestUtil.COMMON_DIR_XML, "role-proxy.xml"));
        Item<?, ?> objectItem = roleProxy.findItem(ItemPath.create(new QName("authorization"), 1L, new QName("object")));
        PrismValue objectPrismValue = objectItem.getAnyValue();

        assertEquals(objectPrismValue.getSchemaContext().getItemDefinition().getTypeName(), userType);

        if (objectItem.find(ItemPath.create(1L, new QName("filter"))) instanceof Item<?, ?> filterItem) {
            PrismValue filterPrismValue = filterItem.getAnyValue();
            assertEquals(filterPrismValue.getSchemaContext().getItemDefinition().getTypeName(), userType);
        }

        if (objectItem.find(ItemPath.create(1L, new QName("parent"))) instanceof Item<?, ?> parentItem) {
            PrismValue parentPrismValue = parentItem.getAnyValue();
            assertEquals(parentPrismValue.getSchemaContext().getItemDefinition().getTypeName(), roleType);

            if (parentItem.find(ItemPath.create("filter")) instanceof Item<?, ?> filterParentItem) {
                PrismValue filterParentPrismValue = filterParentItem.getAnyValue();
                assertEquals(filterParentPrismValue.getSchemaContext().getItemDefinition().getTypeName(), roleType);
            }
        }
    }
}
