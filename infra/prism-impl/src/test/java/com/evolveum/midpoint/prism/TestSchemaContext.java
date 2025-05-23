package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.foo.RoleType;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by Dominik.
 */
public class TestSchemaContext extends AbstractPrismTest {

    Item<?, ?> objectItem;
    QName userType, roleType;

    @BeforeSuite
    public void setupDebug() throws SchemaException, SAXException, IOException {
        PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
        PrismObject<RoleType> roleProxy = getPrismContext().parseObject(new File(PrismInternalTestUtil.COMMON_DIR_XML, "role-proxy.xml"));
        userType = new QName(PrismInternalTestUtil.NS_FOO, "UserType");
        roleType = new QName(PrismInternalTestUtil.NS_FOO, "RoleType");
        objectItem = roleProxy.findItem(ItemPath.create(new QName("authorization"), 1L, new QName("object")));
    }

    @Test
    public void typeContextResolverTest() {
        if (objectItem.find(ItemPath.create(1L, new QName("parent"))) instanceof Item<?, ?> filterItem) {
            PrismValue filterPrismValue = filterItem.getAnyValue();
            assertEquals(filterPrismValue.getSchemaContext().getItemDefinition().getTypeName(), roleType);
        }

        assertEquals(objectItem.getParent().getSchemaContext().getItemDefinition().getTypeName(), roleType);
    }

    @Test
    public void typePropertyContextResolverTest() {
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
