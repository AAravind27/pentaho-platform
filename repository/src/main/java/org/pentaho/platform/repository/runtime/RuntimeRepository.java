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


package org.pentaho.platform.repository.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;

import java.util.Collection;
import java.util.List;

public class RuntimeRepository extends PentahoBase implements IRuntimeRepository, IPentahoInitializer {

  /**
   * 
   */
  private static final long serialVersionUID = -6093228119094501691L;

  private static final boolean debug = PentahoSystem.debug;

  private static final Log log = LogFactory.getLog( RuntimeRepository.class );

  private static final ThreadLocal threadSession = new ThreadLocal();

  /**
   * @return Returns the userSession.
   */
  public static IPentahoSession getUserSession() {
    IPentahoSession userSession = (IPentahoSession) RuntimeRepository.threadSession.get();
    return userSession;
  }

  public RuntimeRepository() {

  }

  public List getMessages() {
    return null;
  }

  public void setSession( final IPentahoSession sess ) {
    RuntimeRepository.threadSession.set( sess );
    if ( sess != null ) {
      genLogIdFromSession( sess );
      HibernateUtil.beginTransaction();
    }
  }

  public void init( final IPentahoSession sess ) {
    this.setSession( sess );
  }

  /**
   * Loads an existing RuntimeElement
   * 
   * @param instId
   *          The instance Id
   * @return the RuntimeElement
   * @throws RepositoryException
   */
  public IRuntimeElement loadElementById( final String instId, final Collection allowableReadAttributeNames )
    throws RepositoryException {
    if ( RuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_LOAD_ELEMENT_BY_ID", instId ) ); //$NON-NLS-1$
    }
    Session session = HibernateUtil.getSession();
    try {
      RuntimeElement runtimeElement = (RuntimeElement) session.load( RuntimeElement.class, instId );
      runtimeElement.setAllowableAttributeNames( allowableReadAttributeNames );
      return runtimeElement;
    } catch ( HibernateException ex ) {
      error( Messages.getInstance().getErrorString( "RTREPO.ERROR_0001_LOAD_ELEMENT", instId ), ex ); //$NON-NLS-1$
      throw new RepositoryException(
          Messages.getInstance().getErrorString( "RTREPO.ERROR_0001_LOAD_ELEMENT", instId ), ex ); //$NON-NLS-1$
    }
  }

  /**
   * 
   * Creates a new RuntimeElement
   * 
   * @param parId
   *          Parent ID of this instance
   * @param parType
   *          Parent type of the instance
   * @return the created runtime element
   */
  public IRuntimeElement newRuntimeElement( final String parId, final String parType, boolean transientOnly ) {
    if ( RuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_NEW_ELEMENT_PARENT", parId, parType ) ); //$NON-NLS-1$
    }
    Session session = HibernateUtil.getSession();
    String instanceId = UUIDUtil.getUUIDAsString();
    if ( RuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_CREATE_INSTANCE", instanceId ) ); //$NON-NLS-1$
    }
    RuntimeElement re = new RuntimeElement( instanceId, parId, parType );
    if ( !transientOnly ) {
      try {
        session.save( re );
      } catch ( HibernateException ex ) {
        error( Messages.getInstance().getErrorString( "RTREPO.ERROR_0002_SAVING_ELEMENT" ), ex ); //$NON-NLS-1$
        throw new RepositoryException( Messages.getInstance().getErrorString( "RTREPO.ERROR_0002_SAVING_ELEMENT" ),
          ex );
      }
    }
    return re;
  }

  /**
   * 
   * Creates a new RuntimeElement
   * 
   * @param parId
   *          Parent Id of the runtime element
   * @param parType
   *          Parent type of the runtime element
   * @param solnId
   *          Solution Id of the element
   * @return The created runtime element
   */
  public IRuntimeElement newRuntimeElement( final String parId, final String parType, final String solnId,
      boolean transientOnly ) {
    if ( RuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_NEW_ELEMENT_PARENT_SOLN", parId, parType, solnId ) ); //$NON-NLS-1$
    }
    Session session = HibernateUtil.getSession();
    String instanceId = UUIDUtil.getUUIDAsString();
    if ( RuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_CREATE_INSTANCE", instanceId ) ); //$NON-NLS-1$
    }
    RuntimeElement re = new RuntimeElement( instanceId, parId, parType, solnId );
    if ( !transientOnly ) {
      try {
        session.save( re );
      } catch ( HibernateException ex ) {
        error( Messages.getInstance().getErrorString( "RTREPO.ERROR_0003_SAVING_ELEMENT" ), ex ); //$NON-NLS-1$
        throw new RepositoryException( Messages.getInstance().getErrorString( "RTREPO.ERROR_0003_SAVING_ELEMENT" ), ex ); //$NON-NLS-1$
      }
    }
    return re;
  }

  /* ILogger Needs */
  @Override
  public Log getLogger() {
    return RuntimeRepository.log;
  }

  public boolean usesHibernate() {
    return true;
  }

}
