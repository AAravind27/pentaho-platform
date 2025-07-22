/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

/**
 * The {@code DerivedActionAuthorizationDecision} class represents an authorization decision that is derived from
 * the authorization decision for another action.
 */
public class DerivedActionAuthorizationDecision extends ImpliedAuthorizationDecision {

  public DerivedActionAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                             @NonNull IAuthorizationDecision impliedFromDecision ) {
    super( request, impliedFromDecision );
  }

  /**
   * Gets the action that this decision is derived from.
   *
   * @return The action that this decision is derived from.
   */
  @NonNull
  public IAuthorizationAction getDerivedFromAction() {
    return getImpliedFromDecision().getRequest().getAction();
  }
}
