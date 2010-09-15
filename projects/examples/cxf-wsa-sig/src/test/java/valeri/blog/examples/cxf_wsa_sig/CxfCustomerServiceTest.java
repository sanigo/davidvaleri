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
package valeri.blog.examples.cxf_wsa_sig;


import javax.annotation.Resource;

import com.example.customerservice.CustomerService;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import valeri.blog.examples.cxf_wsa_sig.CustomerServiceRunner;


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
        "classpath:valeri/blog/examples/cxf_wsa_sig/service-context.xml";

    @Resource
    private CustomerService client;
    
    @Resource
    private Bus cxf;
    
    private static CustomerServiceRunner RUNNER;
    
    @BeforeClass
    public static void startServer() {
        if (Boolean.valueOf(System.getProperty("startServer", "true"))) {
            CxfCustomerServiceTest.RUNNER = new CustomerServiceRunner(
                    CxfCustomerServiceTest.SERVICE_CONTEXT_RESOURCE_PATH);
            CxfCustomerServiceTest.RUNNER.start();
        }
    }
    
    @AfterClass
    public static void stopServer() {
        if (CxfCustomerServiceTest.RUNNER != null) {
            try {
                CxfCustomerServiceTest.RUNNER.stop();
            }
            catch (Exception e) {
                // ignore
            }
            
            CxfCustomerServiceTest.RUNNER = null;
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
    public void testApi() {
        super.testApi();
    }

    @Override
    protected CustomerService getClient() {
        return this.client;
    }
}
