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

import java.security.Provider;
import java.security.Security;


/**
 * A utility class to provide for programmatic reconfiguration of JCE providers.
 *
 * @author David Valeri
 */
public final class SecurityUtil {
    
    /**
     * Hidden in utility class.
     */
    private SecurityUtil() {        
    }
    
    /**
     * Configures a PKCS11 based provider and replace the existing JSSE provider
     * with one configured against the newly added PKCS11 based provider.
     * 
     * @param configFile the file path to the PKCS11 configuration file
     */
    @SuppressWarnings("restriction")
    public static void enablePkcs11Jsse(String configFile) {
        
        Provider nss = new sun.security.pkcs11.SunPKCS11(configFile);
        Security.addProvider(nss);
        
        int sunJssePosition = -1;
        int currentIndex = 0;
        for (Provider provider : Security.getProviders()) {
            if ("SunJSSE".equals(provider.getName())) {
                sunJssePosition = currentIndex + 1;
                break;
            }
            
            currentIndex++;
        }
        
        Security.removeProvider("SunJSSE");
        
        Provider sunJsse = new com.sun.net.ssl.internal.ssl.Provider(nss);
        if (sunJssePosition == -1) {
            Security.addProvider(sunJsse);
        }
        else {
            Security.insertProviderAt(sunJsse, sunJssePosition);
        } 
    }
}
