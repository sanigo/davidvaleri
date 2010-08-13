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

package valeri.blog.examples.karaf_wrap;

import java.net.URL;
import java.util.Arrays;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple class that demonstrates that the wrapped bundles are indeed loaded and working.
 *
 * @author David Valeri
 * @since 1.0
 */
public class Example {
    
    private static final Log LOG = LogFactory.getLog(Example.class); 
    
    private Thread worker;
    private volatile boolean run = true;
    
    
    public void start() {
        Runnable runnable = new Runnable() {
            
            public void run() {
                
                try {
                    
                    while(run) {
                    
                        URL url = new URL("http://davidvaleri.wordpress.com");
                                                
                        LOG.info("\n--------------------------------------------" + "\n"
                                + "Protocol: " + BeanUtils.getSimpleProperty(url, "protocol") + "\n"
                                + "Host: " + BeanUtils.getSimpleProperty(url, "host") + "\n"
                                + "Path: " + BeanUtils.getSimpleProperty(url, "path") + "\n"
                                + "--------------------------------------------");
                        
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {
                            LOG.debug("Interrupted", e);
                        }
                    }
                }
                catch (Exception e) {
                    LOG.error("Thread died due to exception.", e);
                }
            }
        };
        
        this.worker = new Thread(runnable);
        this.worker.setName(this.getClass().getName());
        this.worker.start();
    }
    
    public void stop() {
        this.run = false;
        this.worker.interrupt();
    }
}
