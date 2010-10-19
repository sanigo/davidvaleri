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


import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Simple launcher for the server.
 *
 * @author David Valeri
 */
public class CustomerServiceRunner {
    
    private ClassPathXmlApplicationContext context = null;

    /**
     * Starts a new instance of the customer service.
     */
    public static void main(String[] args) throws Exception {
        
        // Programmatic reconfigure of JCE and JSSE.  This would normally be carried out
        // using the ${java.home}/lib/security/java.security, using dynamic reconfiguration
        // reduces the amount of setup required for this example.
        SecurityUtil.enablePkcs11Jsse("./target/pkcs11/nss-server.cfg");
        // End JCE and JSSE reconfiguration.
        
        CustomerServiceRunner runner = new CustomerServiceRunner(args[0]);
        
        boolean usingLauncher = false;
        
        if (args.length > 1) {
            usingLauncher = Boolean.valueOf(args[1]);
        }
        
        runner.start();
        
        if (usingLauncher) {
            // Signal for server launcher.
            System.out.println("server ready");
            
            // Wait for signal from server launcher.
            StringBuilder serverInput = new StringBuilder(); 
            for (int ch = System.in.read(); ch != -1; ch = System.in.read()) {
                serverInput.append((char)ch);
                String s = serverInput.toString();
                System.out.println("Read: " + s);
                if (s.contains("q\n")) {
                    break;
                } 
            }
            
            runner.stop();
            // Signals for server launcher.
            System.out.println("server passed");
            System.out.println("server stopped");
        }
        else {
            System.out.println("Loaded Service.");
            System.out.println("Press Ctrl-c to stop the service.");
        }
    }
    
    public CustomerServiceRunner(String locations) {
        this.context = new ClassPathXmlApplicationContext();
        this.context.setConfigLocation(locations);
    }
    
    public void start() {
        if (!this.context.isActive()) {
            this.context.registerShutdownHook();
            this.context.refresh();
        }
    }
    
    public void stop() {
        this.context.close();
    }
}
