/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.auth;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
public interface OAuthServiceClient {

    Promise<Void> invalidateToken(String oauthProvider);
    /**
     * Gets OAuth token for user.
     *
     * @param oauthProvider
     *         OAuth provider name
     * @return OAuthToken
     */
    Promise<OAuthToken> getToken(String oauthProvider);


    /**
     * Gets list of installed OAuth authenticators.
     *
     * @return list of installed OAuth authenticators
     */
    Promise<List<OAuthAuthenticatorDescriptor>> getRegisteredAuthenticators();

}
