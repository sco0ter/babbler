/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp;

import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Schudt
 */
public class FaceBookSaslClient implements SaslClient {

    private final String apiKey;

    private final String accessToken;

    public FaceBookSaslClient(String apiKey, String accessToken) throws SaslException {
        if (apiKey == null || accessToken == null) {
            throw new SaslException("PLAIN: authorization ID and password must be specified");
        }
        this.apiKey = apiKey;
        this.accessToken = accessToken;
    }

    @Override
    public String getMechanismName() {
        return "X-FACEBOOK-PLATFORM";
    }

    @Override
    public boolean hasInitialResponse() {
        return false;
    }

    private Map<String, String> getQueryMap(String query) {
        Map<String, String> map = new HashMap<String, String>();
        String[] params = query.split("\\&");

        for (String param : params) {
            String[] fields = param.split("=", 2);
            map.put(fields[0], (fields.length > 1 ? fields[1] : null));
        }
        return map;
    }

    @Override
    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {

        Map<String, String> parameters = getQueryMap(new String(challenge));

        String version = "1.0";
        String nonce = parameters.get("nonce");
        String method = parameters.get("method");
        long callId = 0;
        try {
            String composedResponse = "api_key=" + URLEncoder.encode(apiKey, "utf-8")
                    + "&call_id=" + callId
                    + "&method=" + URLEncoder.encode(method, "utf-8")
                    + "&nonce=" + URLEncoder.encode(nonce, "utf-8")
                    + "&access_token=" + URLEncoder.encode(accessToken, "utf-8")
                    + "&v=" + URLEncoder.encode(version, "utf-8");

            return composedResponse.getBytes("utf-8");

        } catch (Exception e) {
            throw new SaslException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
        return new byte[0];
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        return new byte[0];
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        return null;
    }

    @Override
    public void dispose() throws SaslException {

    }
}
