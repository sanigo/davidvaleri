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
package valeri.blog.examples.cxf_replay;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;
import com.example.customerservice.CustomerType;
import com.example.customerservice.NoSuchCustomerException;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.Names;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Test that exercises the basic service API and attempts to replay duplicate
 * message IDs against the server.
 *
 * @author David Valeri
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ReplayTest extends AbstractCustomerServiceTest {
    
    private static final String SERVICE_CONTEXT_RESOURCE_PATH =
        "classpath:valeri/blog/examples/cxf_replay/service-context.xml";

    @Resource
    private CustomerService client;
    
    @Resource
    private Bus cxf;
    
    private static CustomerServiceRunner RUNNER;
    
    @BeforeClass
    public static void startServer() {
        ReplayTest.RUNNER = new CustomerServiceRunner(
                ReplayTest.SERVICE_CONTEXT_RESOURCE_PATH);
        ReplayTest.RUNNER.start();
    }
    
    @AfterClass
    public static void stopServer() {
        if (ReplayTest.RUNNER != null) {
            try {
                ReplayTest.RUNNER.stop();
            }
            catch (Exception e) {
                // ignore
            }
            
            ReplayTest.RUNNER = null;
        }
    }
    
    /**
     * Because we are messing with the configuration of the service during tests
     * here, we want to set {link DirtiesContext} on each test method to ensure
     * the next test method gets a clean configuration from Spring.  As the bus
     * that is created by default is not bound to the Spring context's lifecycle,
     * we need to ensure that it gets shutdown between tests.  
     */
    @After
    public void teardown() {
        if (this.cxf != null) {
            try {
                this.cxf.shutdown(true);
            }
            catch (Exception e) {
                // ignore
            }
        }
    }
    
    @Test
    @DirtiesContext
    public void testReplay() throws NoSuchCustomerException {
        
        // Ensure that there is at least one known customer in play.
        final Customer customer1 = new Customer();
        customer1.setCustomerId(3280810);
        customer1.setName("Phillip J. Fry");
        customer1.setNumOrders(3);
        customer1.setRevenue(0.13);
        customer1.setType(CustomerType.BUSINESS);

        client.updateCustomer(customer1);
        
        // Reconfigure WS-A headers for subsequent requests.
        final AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
        ((BindingProvider) this.getClient()).getRequestContext()
                .put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, maps);
        
        final String messageID = ContextUtils.generateUUID();
        maps.setMessageID(ContextUtils.getAttributedURI(messageID));
        
        // Make requests with the duplicate ID.
        try {
            client.getCustomersByName("Phillip J. Fry");
        }
        catch (SOAPFaultException e) {
            // This one should succeed 
            fail();
        }
        
        try {
            // This one should fail
            client.getCustomersByName("Phillip J. Fry");
            fail();
        }
        catch (SOAPFaultException e) {
            QName faultCode = e.getFault().getFaultCodeAsQName();
            
            assertEquals("Addressing related fault does not use correct WS-A namespace.",
                    Names.WSA_NAMESPACE_NAME, faultCode.getNamespaceURI());
            assertEquals("Addressing related fault does not use correct WS-A fault code.",
                    Names.DUPLICATE_MESSAGE_ID_NAME, faultCode.getLocalPart());
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
