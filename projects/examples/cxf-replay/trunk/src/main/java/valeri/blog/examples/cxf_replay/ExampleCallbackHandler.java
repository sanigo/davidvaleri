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


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;


/**
 * Simple callback handler that uses a {@code Map} to hold the pairs
 * of aliases and credentials for callback requests from the WSS4j system.
 *
 * @author David Valeri
 */
public class ExampleCallbackHandler implements CallbackHandler {

    /**
     * Map of aliases to passwords for signature keys.
     */
    private Map<String, String> signatureCredentials = 
        new ConcurrentHashMap<String, String>();
    
    /**
     * Returns a live copy of the alias to password {@code Map} for signature
     * keys. The returned {@code Map} is suitable for runtime configuration.
     * 
     * @return a live copy of the alias to password {@code Map} for signature
     *         keys
     */
    public Map<String, String> getSignatureCredentials() {
        return this.signatureCredentials;
    }

    /**
     * Sets the contents of the alias to password {@code Map} for signature
     * keys. Clears the contents if {@code signatureCredentials} is {@code null}.
     * The internal state of this instance is not backed by the contents of
     * the provided argument. Use the value returned by
     * {@link #getSignatureCredentials()} to alter the internal state of this
     * instance.
     * 
     * @param signatureCredentials
     *            the contents to put in the alias to password {@code Map} for
     *            signature keys
     */
    public void setSignatureCredentials(Map<String, String> signatureCredentials)
    {
        this.signatureCredentials.clear();

        if (signatureCredentials != null) {
            this.signatureCredentials.putAll(signatureCredentials);
        }
    }

    /**
     * Provides the requested information for the given callback. This
     * implementation supports the {@link WSPasswordCallback} callback type
     * only. Any other callback type will result in an
     * {@link UnsupportedCallbackException}.
     * 
     * @param callbacks
     *            the {@code Callback}s to process
     * 
     * @throws UnsupportedCallbackException
     *             if an unsupported {@code Callback} type is provided in
     *             {@code Callbacks} or if this handler cannot handle the
     *             request
     * @throws IOException
     *             if there is an error processing the request
     * 
     * @see #handleSignature(WSPasswordCallback)
     * @see #handleUserNameTokenUnknown(WSPasswordCallback)
     */
    public final void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException
    {
        for (Callback callback : callbacks) {
            if (!(callback instanceof WSPasswordCallback)) {
                throw new UnsupportedCallbackException(callback, 
                        "Unsupported callback type: " + 
                        callback != null ? 
                                callback.getClass().getName() : "null");
            }

            WSPasswordCallback pc = (WSPasswordCallback) callback;
            switch (pc.getUsage()) {
                // Case for signing key password.
                case WSPasswordCallback.SIGNATURE:
                    this.handleSignature(pc);
                    break;
                default:
                    throw new UnsupportedCallbackException(callback, 
                            "Unsupported callback usage: " + 
                            pc.getUsage());
            }
        }
    }

    /**
     * Handle a request for the password to access the private key for the
     * provided alias ({@link WSPasswordCallback#getIdentifier()}). Refer to
     * {@link WSPasswordCallback} and {@link WSPasswordCallback#SIGNATURE} for
     * more details.  The caller expects the the implementation to set
     * the password field of the callback 
     * ({@link WSPasswordCallback#setPassword(String)).
     * <p/>
     * This implementation returns the matching password from the
     * {@link #signatureCredentials signature credentials map}.
     * 
     * @param pc
     * 
     * @throws IOException
     *             if there is an error processing the request
     * @throws UnsupportedCallbackException
     *             if the alias does not have credentials in the map
     */
    protected void handleSignature(WSPasswordCallback pc) throws IOException,
            UnsupportedCallbackException
    {
        final String password = this.signatureCredentials.get(pc
                .getIdentifier());

        if (password == null) {
            throw new UnsupportedCallbackException(pc, 
                    "No credentials for alias: " + pc.getIdentifier());
        }

        pc.setPassword(password);
    }
}

