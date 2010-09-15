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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;
import com.example.customerservice.CustomerType;
import com.example.customerservice.NoSuchCustomerException;

import org.junit.Test;


/**
 * Base class for tests that interact with {@link CustomerService}
 * implementations.
 * 
 * @author David Valeri
 */
public abstract class AbstractCustomerServiceTest {

    @Test
    public void testApi() {
        final CustomerService client = this.getClient();

        final Customer customer1 = new Customer();
        customer1.setCustomerId(3280810);
        customer1.setName("Phillip J. Fry");
        customer1.setNumOrders(3);
        customer1.setRevenue(0.13);
        customer1.setType(CustomerType.BUSINESS);

        client.updateCustomer(customer1);

        final Customer customer2 = new Customer();
        customer2.setCustomerId(3280811);
        customer2.setName("Phillip J. Fry");
        customer2.setNumOrders(4);
        customer2.setRevenue(0.14);
        customer2.setType(CustomerType.PRIVATE);

        client.updateCustomer(customer2);

        try {
            List<Customer> customers = client
                    .getCustomersByName("Phillip J. Fry");

            assertNotNull(customers);
            assertEquals(2, customers.size());
        }
        catch (NoSuchCustomerException e) {
            fail();
        }

        try {
            client.getCustomersByName("Jon Doe");
            fail();
        }
        catch (NoSuchCustomerException e) {
            // Expected
        }
    }

    /**
     * Return the client to execute the test against.
     */
    protected abstract CustomerService getClient();
}
