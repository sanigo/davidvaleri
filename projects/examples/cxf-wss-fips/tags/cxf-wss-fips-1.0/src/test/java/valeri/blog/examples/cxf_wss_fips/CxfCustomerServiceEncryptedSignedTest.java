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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests messages using encryption.
 *
 * @author David Valeri
 */
@ContextConfiguration
public class CxfCustomerServiceEncryptedSignedTest extends AbstractCxfCustomerServiceTest 
{
	@BeforeClass
	public static void setup() throws Exception {
		CxfCustomerServiceEncryptedSignedTest.startServer();
	}
	
	@AfterClass
	public static void teardown() {
		CxfCustomerServiceEncryptedSignedTest.stopServer();
	}
}
