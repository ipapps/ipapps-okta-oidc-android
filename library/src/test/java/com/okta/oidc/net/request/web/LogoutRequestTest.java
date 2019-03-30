/*
 * Copyright (c) 2019, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License,
 * Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.okta.oidc.net.request.web;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.okta.oidc.OIDCAccount;
import com.okta.oidc.net.response.TokenResponse;
import com.okta.oidc.util.AsciiStringListUtil;
import com.okta.oidc.util.CodeVerifierUtil;
import com.okta.oidc.util.TestValues;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static com.okta.oidc.util.JsonStrings.TOKEN_RESPONSE;
import static com.okta.oidc.util.TestValues.CLIENT_ID;
import static com.okta.oidc.util.TestValues.CUSTOM_NONCE;
import static com.okta.oidc.util.TestValues.CUSTOM_STATE;
import static com.okta.oidc.util.TestValues.CUSTOM_URL;
import static com.okta.oidc.util.TestValues.EXPIRES_IN;
import static com.okta.oidc.util.TestValues.LOGIN_HINT;
import static com.okta.oidc.util.TestValues.PROMPT;
import static com.okta.oidc.util.TestValues.SCOPES;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27)
public class LogoutRequestTest {
    private LogoutRequest mRequest;
    private OIDCAccount mAccount;
    @Rule
    public ExpectedException mExpectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        mAccount = TestValues.getAccountWithUrl(CUSTOM_URL);
        mAccount.setProviderConfig(TestValues.getProviderConfiguration(CUSTOM_URL));
        mAccount.setTokenResponse(TokenResponse.RESTORE.restore(TOKEN_RESPONSE));
        mRequest = new LogoutRequest.Builder()
                .clientId(CLIENT_ID)
                .state(CUSTOM_STATE)
                .account(mAccount)
                .create();
    }

    @Test
    public void testBuilderFailEndpointMissing() {
        LogoutRequest.Builder builder = new LogoutRequest.Builder();
        mExpectedEx.expect(IllegalArgumentException.class);
        mExpectedEx.expectMessage("end_session_endpoint missing");
        builder.create();
    }

    @Test
    public void testBuilderFailClientIdMissing() {
        LogoutRequest.Builder builder = new LogoutRequest.Builder();
        builder.endSessionEndpoint(mAccount.getEndSessionRedirectUri().toString());
        mExpectedEx.expect(IllegalArgumentException.class);
        mExpectedEx.expectMessage("client_id missing");
        builder.create();
    }

    @Test
    public void testBuilderFailTokenMissing() {
        LogoutRequest.Builder builder = new LogoutRequest.Builder();
        builder.endSessionEndpoint(mAccount.getEndSessionRedirectUri().toString());
        builder.clientId(CLIENT_ID);
        mExpectedEx.expect(IllegalArgumentException.class);
        mExpectedEx.expectMessage("id_token_hint missing");
        builder.create();
    }

    @Test
    public void testBuilderFailRedirectMissing() {
        LogoutRequest.Builder builder = new LogoutRequest.Builder();
        builder.endSessionEndpoint(mAccount.getEndSessionRedirectUri().toString());
        builder.clientId(CLIENT_ID);
        builder.idTokenHint(mAccount.getIdToken());
        mExpectedEx.expect(IllegalArgumentException.class);
        mExpectedEx.expectMessage("post_logout_redirect_uri missing");
        builder.create();
    }

    @Test
    public void testBuilder() {
        LogoutRequest request = new LogoutRequest.Builder()
                .clientId(CLIENT_ID)
                .state(CUSTOM_STATE)
                .account(mAccount)
                .create();
        assertEquals(mRequest, request);
    }

    @Test
    public void getState() {
        assertEquals(mRequest.getState(), CUSTOM_STATE);
    }

    @Test
    public void toUri() {
        Uri uri = mRequest.toUri();
        assertEquals(uri.getQueryParameter("client_id"), mAccount.getClientId());
        assertEquals(uri.getQueryParameter("id_token_hint"), mAccount.getIdToken());
        assertEquals(uri.getQueryParameter("state"), CUSTOM_STATE);
        assertEquals(uri.getQueryParameter("post_logout_redirect_uri"),
                mAccount.getEndSessionRedirectUri().toString());
    }

    @Test
    public void getKey() {
        assertEquals(mRequest.getKey(), "WebRequest");
    }

    @Test
    public void persist() {
        String json = mRequest.persist();
        LogoutRequest.Parameters parameters = new Gson().fromJson(json, LogoutRequest.Parameters.class);
        LogoutRequest request = new LogoutRequest(parameters);
        assertEquals(request, mRequest);
    }

    @Test
    public void encrypt() {
        assertFalse(mRequest.encrypt());
    }
}