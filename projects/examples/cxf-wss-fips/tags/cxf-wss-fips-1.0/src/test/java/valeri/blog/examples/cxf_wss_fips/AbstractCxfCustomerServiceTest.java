/*
 * Copyright 2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package valeri.blog.examples.cxf_wss_fips;


import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import com.example.customerservice.CustomerService;

import org.apache.cxf.testutil.common.ServerLauncher;
import org.apache.xml.security.algorithms.JCEMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import valeri.blog.examples.cxf_wss_fips.CustomerServiceRunner;


/**
 * Test that exercises the basic service API and attempts to replay duplicate
 * message IDs against the server.
 *
 * @author David Valeri
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractCxfCustomerServiceTest extends AbstractCustomerServiceTest {
    
    private static final String SERVICE_CONTEXT_RESOURCE_PATH =
        "classpath:valeri/blog/examples/cxf_wss_fips/service-context.xml";

    @Resource
    private CustomerService client;
    
    private static ServerLauncher LAUNCHER;
    
    public static void startServer() throws Exception {
        if (Boolean.valueOf(System.getProperty("startServer", "true"))) {
            
            AbstractCxfCustomerServiceTest.LAUNCHER = new ServerLauncher(
                    CustomerServiceRunner.class.getName(),
                    null,
                    new String[] {
                        AbstractCxfCustomerServiceTest.SERVICE_CONTEXT_RESOURCE_PATH,
                        "true"});
            
            assertTrue(AbstractCxfCustomerServiceTest.LAUNCHER.launchServer());
        }
    }
    
    public static void stopServer() {
        if (AbstractCxfCustomerServiceTest.LAUNCHER != null) {
            try {
                assertTrue(AbstractCxfCustomerServiceTest.LAUNCHER.stopServer());
            }
            catch (Exception e) {
                // ignore
            }
            
            AbstractCxfCustomerServiceTest.LAUNCHER = null;
        }
    }
    
    public static void setupPkcs11() {
		// Programmatic reconfigure of JCE and JSSE.  This would normally be carried out
        // using the ${java.home}/lib/security/java.security, using dynamic reconfiguration
        // reduces the amount of setup required for this example.
        SecurityUtil.enablePkcs11Jsse("./target/pkcs11/nss-client.cfg"); 
        // End JCE and JSSE reconfiguration.
        
        // Reconfigure WSS4J's algorithm URI mapping utility to retrieve algorithm
        // implementations from the PKCS11 provider.
        JCEMapper.setProviderId("SunPKCS11-nss-client");
        // End WSS4J reconfiguration.
	}
    
    @Test
    @DirtiesContext
    public void testApi() throws Exception {
        super.testApi();
    }

    @Override
    protected CustomerService getClient() {
        return this.client;
    }
}
