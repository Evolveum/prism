package com.evolveum.midpoint.prism.crypto;

import javax.crypto.Cipher;

import org.apache.xml.security.encryption.XMLCipher;
import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.types_2.ProtectedStringType;

public class TestProtector {
	
	public static final String KEYSTORE_PATH = "src/test/resources/keystore.jceks";
	public static final String KEYSTORE_PASSWORD = "changeit";
	
	private PrismContext prismContext;
	
	private static transient Trace LOGGER = TraceManager.getTrace(TestProtector.class);
	
//	@BeforeSuite
//	public void setup() throws SchemaException, SAXException, IOException {
//		PrettyPrinter.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
//		PrismTestUtil.resetPrismContext(MidPointPrismContextFactory.FACTORY);
		
//		prismContext = PrismTestUtil.createInitializedPrismContext();

//	}
	
	
	private static Protector createProtector(String xmlCipher){
		AESProtector protector = new AESProtector();
//		protector.setPrismContext(prismContext);
		protector.setKeyStorePassword(KEYSTORE_PASSWORD);
		protector.setKeyStorePath(KEYSTORE_PATH);
		protector.setEncryptionAlgorithm(xmlCipher);
		protector.init();
		return protector;
	}
	
	
  @Test
  public void testProtectorKeyStore() throws Exception{
	  
	  String value = "someValue";
	
	  Protector protector256 = createProtector("AES/CBC/PKCS5Padding");
	  ProtectedStringType pdt = new ProtectedStringType();
	  pdt.setClearValue(value);
	  protector256.encrypt(pdt);
	  
	  
	  Protector protector128 = createProtector(XMLCipher.AES_128);
	  protector128.decrypt(pdt);
	 
  }
}
