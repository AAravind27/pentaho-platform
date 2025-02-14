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


package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths.IServerRepositoryPathsStrategy;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;

import java.text.MessageFormat;

/**
 * Default {@link IServerRepositoryPathsStrategy} implementation. Uses MessageFormat patterns.
 * 
 * @author mlowery
 */
public class DefaultServerRepositoryPathsStrategy implements IServerRepositoryPathsStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String FOLDER_ROOT = "pentaho"; //$NON-NLS-1$

  private static final String FOLDER_ETC = "etc"; //$NON-NLS-1$

  private static final String PATH_ROOT = RepositoryFile.SEPARATOR + FOLDER_ROOT;

  // ~ Instance fields
  // =================================================================================================

  private final String PATTERN_TENANT_HOME_PATH = "{0}" + ClientRepositoryPaths.getHomeFolderPath(); //$NON-NLS-1$

  private final String PATTERN_TENANT_PUBLIC_PATH = "{0}" + ClientRepositoryPaths.getPublicFolderPath();

  private final String PATTERN_TENANT_ETC_PATH = "{0}" + RepositoryFile.SEPARATOR + FOLDER_ETC; //$NON-NLS-1$

  // ~ Constructors
  // ====================================================================================================

  public DefaultServerRepositoryPathsStrategy() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public String getPentahoRootFolderPath() {
    return PATH_ROOT;
  }

  public String getTenantHomeFolderPath( final ITenant tenant ) {
    return MessageFormat.format( PATTERN_TENANT_HOME_PATH, ( tenant == null || tenant.getId() == null )
        ? JcrTenantUtils.getDefaultTenant().getRootFolderAbsolutePath() : tenant.getRootFolderAbsolutePath() );
  }

  public String getTenantPublicFolderPath( final ITenant tenant ) {
    return MessageFormat.format( PATTERN_TENANT_PUBLIC_PATH, ( tenant == null || tenant.getId() == null )
        ? JcrTenantUtils.getDefaultTenant().getRootFolderAbsolutePath() : tenant.getRootFolderAbsolutePath() );
  }

  public String getTenantRootFolderPath( final ITenant tenant ) {
    return ( tenant == null || tenant.getId() == null ) ? JcrTenantUtils.getDefaultTenant().getRootFolderAbsolutePath()
        : tenant.getRootFolderAbsolutePath();
  }

  public String getUserHomeFolderPath( ITenant tenant, final String username ) {
    return getTenantRootFolderPath( tenant ) + ClientRepositoryPaths.getUserHomeFolderPath( username );
  }

  public String getPentahoRootFolderName() {
    return FOLDER_ROOT;
  }

  public String getTenantHomeFolderName() {
    return ClientRepositoryPaths.getHomeFolderName();
  }

  public String getTenantPublicFolderName() {
    return ClientRepositoryPaths.getPublicFolderName();
  }

  public String getTenantEtcFolderName() {
    return FOLDER_ETC;
  }

  public String getTenantEtcFolderPath( final ITenant tenant ) {
    return MessageFormat.format( PATTERN_TENANT_ETC_PATH, ( tenant == null || tenant.getId() == null ) ? JcrTenantUtils
        .getDefaultTenant().getRootFolderAbsolutePath() : tenant.getRootFolderAbsolutePath() );
  }

  public String getTenantId( final String absPath ) {
    return absPath;
  }

}
