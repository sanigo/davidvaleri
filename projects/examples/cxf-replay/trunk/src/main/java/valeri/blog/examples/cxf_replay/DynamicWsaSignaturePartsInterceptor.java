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


import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.handler.WSHandlerConstants;


/**
 * Inteceptor that can dynamically add signature coverage of WS-A headers
 * if they are present in the message.  This interceptor allows these
 * headers to always be signed when using traditional static WSS4J configuration
 * in CXF. 
 *
 * @author David Valeri
 */
public class DynamicWsaSignaturePartsInterceptor extends
        AbstractPhaseInterceptor<Message>
{

    public DynamicWsaSignaturePartsInterceptor() {
        super(Phase.PRE_PROTOCOL);
        this.addBefore(WSS4JOutInterceptor.class.getName());
    }

    public void handleMessage(Message message) throws Fault {
        
        if (MessageUtils.isOutbound(message)) {
            
            String signatureParts = null;
            WSS4JOutInterceptor wss4jOutInterceptor = this.findWss4JOutInterceptor(message);
            
            
            if (wss4jOutInterceptor != null && this.isSigning(wss4jOutInterceptor)) {
                signatureParts = this.getSignatureParts(wss4jOutInterceptor);
                
                AddressingProperties maps = 
                    ContextUtils.retrieveMAPs(message, false, true);

                if (maps != null) {
                    final StringBuilder builder = new StringBuilder();
                    
                    if (signatureParts != null) {
                        builder.append(signatureParts);
                    }
                    
                    if (maps.getAction() != null) {
                        this.appendSignaturePart(builder, 
                                "{Element}{http://www.w3.org/2005/08/addressing}Action");
                    }
                    
                    if (maps.getTo() != null) {
                        this.appendSignaturePart(builder, 
                                "{Element}{http://www.w3.org/2005/08/addressing}To");
                    }
                    
                    if (maps.getFaultTo() != null
                            && maps.getFaultTo().getAddress() != null
                            && maps.getFaultTo().getAddress().getValue() != null
                            && !maps.getFaultTo().getAddress().getValue()
                                .equals(maps.getReplyTo().getAddress().getValue())) {
                        this.appendSignaturePart(builder, 
                                "{Element}{http://www.w3.org/2005/08/addressing}FaultTo");
                    }
                    
                    if (maps.getReplyTo() != null) {
                        this.appendSignaturePart(builder, 
                                "{Element}{http://www.w3.org/2005/08/addressing}ReplyTo");
                    }
                    
                    if (maps.getRelatesTo() != null) {
                        this.appendSignaturePart(builder, 
                                "{Element}{http://www.w3.org/2005/08/addressing}RelatesTo");
                    }
                    
                    if (maps.getMessageID() != null) {
                        this.appendSignaturePart(builder, 
                                "{Element}{http://www.w3.org/2005/08/addressing}MessageID");
                    }
                    
                    if ((signatureParts != null && builder.length() > signatureParts.length()) 
                            || (signatureParts == null && builder.length() > 0))
                    {
                        this.setSignatureParts(message, wss4jOutInterceptor, builder.toString());
                    }
                }
            }
        }
    }
    
    private void appendSignaturePart(StringBuilder builder, String part) {
        if (builder.length() != 0) {
            builder.append("; ");
        }
        
        builder.append(part);
    }
    
    private boolean isSigning(WSS4JOutInterceptor interceptor) {
        final String action = (String) interceptor.getProperties().get(
                WSHandlerConstants.ACTION);
        
        return action != null && action.contains("Signature");
    }
    
    private String getSignatureParts(WSS4JOutInterceptor interceptor) {
        return (String) interceptor.getProperties().get(
                WSHandlerConstants.SIGNATURE_PARTS);
    }
    
    private void setSignatureParts(Message message, WSS4JOutInterceptor interceptor,
            String signatureParts)
    {
        Map<String, Object> properties = new HashMap<String, Object>(
                interceptor.getProperties());
        properties.put(WSHandlerConstants.SIGNATURE_PARTS, signatureParts);
        
        message.getInterceptorChain().remove(interceptor);
        message.getInterceptorChain().add(new WSS4JOutInterceptor(properties));
    }

    private WSS4JOutInterceptor findWss4JOutInterceptor(Message message) {
        
        WSS4JOutInterceptor wss4jOutInterceptor = null;
        
        ListIterator<Interceptor<? extends Message>> interceptors = 
                message.getInterceptorChain().getIterator();
        
        while (interceptors.hasNext() && wss4jOutInterceptor == null) {
            Interceptor<? extends Message> interceptor = interceptors.next();
            
            if (interceptor instanceof WSS4JOutInterceptor) {
                wss4jOutInterceptor = (WSS4JOutInterceptor) interceptor;
            }
        }
        
        return wss4jOutInterceptor;
    }
}
