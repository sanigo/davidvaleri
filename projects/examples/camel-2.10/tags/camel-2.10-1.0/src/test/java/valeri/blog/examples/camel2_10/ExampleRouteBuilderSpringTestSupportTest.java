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

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ExampleRouteBuilderSpringTestSupportTest extends CamelSpringTestSupport {
    
    @Produce(uri = "direct:input")
    protected ProducerTemplate inputProducerTemplate;
    
    @EndpointInject(uri = "mock:bean:exampleBean")
    protected MockEndpoint mockBeanExampleBean;
    
    @Test
    public void testRoute() throws Exception {
        mockBeanExampleBean.expectedBodiesReceived("Hello Bob");
        
        inputProducerTemplate.sendBody("Bob");
        
        assertMockEndpointsSatisfied();
    }
    
    @Override
    public String isMockEndpoints() {
        return "bean:exampleBean";
    }
    
    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("classpath:valeri/blog/examples/camel2_10/ExampleSpringContext.xml");
    }
}
