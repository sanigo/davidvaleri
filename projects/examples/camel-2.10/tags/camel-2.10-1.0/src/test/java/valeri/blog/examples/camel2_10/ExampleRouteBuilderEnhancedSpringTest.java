/*
 * Copyright 2012 the original author or authors.
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
package valeri.blog.examples.camel2_10;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:valeri/blog/examples/camel2_10/ExampleSpringContext.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@MockEndpoints("bean:exampleBean")
public class ExampleRouteBuilderEnhancedSpringTest {
    
    @Autowired
    protected CamelContext context;

    @Produce(uri = "direct:input")
    protected ProducerTemplate inputProducerTemplate;
    
    @EndpointInject(uri = "mock:bean:exampleBean")
    protected MockEndpoint mockBeanExampleBean;
    
    @Test
    public void testRoute() throws Exception {
        mockBeanExampleBean.expectedBodiesReceived("Hello Bob");
        
        inputProducerTemplate.sendBody("Bob");
        
        MockEndpoint.assertIsSatisfied(context);
    }
}
