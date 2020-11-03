/*
 * Copyright &copy; 2015 Cloudsoft Corporation Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.edf.jenkins.plugins.windows.winrm.client.auth.ntlm

import org.apache.http.Header
import org.apache.http.HttpRequest
import org.apache.http.auth.AuthenticationException
import org.apache.http.auth.Credentials
import org.apache.http.client.config.AuthSchemes
import org.apache.http.impl.auth.NTLMScheme
import org.apache.http.message.BasicHeader

/**
 * 
 * @author cloudsoft
 * @see https://github.com/cloudsoft/winrm4j
 *
 */
class ApacheSpnegoScheme extends NTLMScheme{

    @Override
    String getSchemeName() {
        return AuthSchemes.SPNEGO
    }

    @Override
    Header authenticate(Credentials credentials, HttpRequest request)
    throws AuthenticationException {
        Header hdr = super.authenticate(credentials, request)
        return new BasicHeader(hdr.getName(), hdr.getValue().replace("NTLM", getSchemeName()))
    }
}
