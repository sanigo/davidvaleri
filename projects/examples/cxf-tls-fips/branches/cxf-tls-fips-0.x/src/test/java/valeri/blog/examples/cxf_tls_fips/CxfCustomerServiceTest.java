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
package valeri.blog.examples.cxf_tls_fips;


import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import com.example.customerservice.CustomerService;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.testutil.common.ServerLauncher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import valeri.blog.examples.cxf_tls_fips.CustomerServiceRunner;


/**
 * Test that exercises the basic service API and attempts to replay duplicate
 * message IDs against the server.
 *
 * @author David Valeri
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CxfCustomerServiceTest extends AbstractCustomerServiceTest {
    
    private static final String SERVICE_CONTEXT_RESOURCE_PATH =
        "classpath:valeri/blog/examples/cxf_tls_fips/service-context.xml";

    @Resource
    private CustomerService client;
    
    @Resource
    private Bus cxf;
    
    private static ServerLauncher LAUNCHER;
    
    @BeforeClass
    public static void startServer() throws Exception {
        if (Boolean.valueOf(System.getProperty("startServer", "true"))) {
            
            CxfCustomerServiceTest.LAUNCHER = new ServerLauncher(
                    CustomerServiceRunner.class.getName(),
                    null,
                    new String[] {
                        CxfCustomerServiceTest.SERVICE_CONTEXT_RESOURCE_PATH,
                        "true"});
            
            assertTrue(CxfCustomerServiceTest.LAUNCHER.launchServer());
        }
        
        // Programmatic reconfigure of JCE and JSSE.  This would normally be carried out
        // using the ${java.home}/lib/security/java.security, using dynamic reconfiguration
        // reduces the amount of setup required for this example.
        SecurityUtil.enablePkcs11Jsse("./target/pkcs11/nss-client.cfg"); 
        // End JCE and JSSE reconfiguration.
    }
    
    @AfterClass
    public static void stopServer() {
        if (CxfCustomerServiceTest.LAUNCHER != null) {
            try {
                assertTrue(CxfCustomerServiceTest.LAUNCHER.stopServer());
            }
            catch (Exception e) {
                // ignore
            }
            
            CxfCustomerServiceTest.LAUNCHER = null;
        }
    }
    
    /**
     * Because we are messing with the configuration of the service during tests
     * here, we want to set {@link DirtiesContext} on each test method to ensure
     * the next test method gets a clean configuration from Spring.  As the bus
     * that is created by default is not bound to the Spring context's lifecycle,
     * we need to ensure that it gets shutdown between tests.  
     */
    @After
    public void teardown() {
        if (this.cxf != null) {
            try {
                this.cxf.shutdown(true);
                BusFactory.setDefaultBus(null);
                BusFactory.setThreadDefaultBus(null);
            }
            catch (Exception e) {
                // ignore
            }
        }
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
