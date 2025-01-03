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


package org.pentaho.platform.plugin.services.importexport.exportManifest;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileExtraMetaData;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.services.importexport.ExportManifestRepositoryException;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.CustomProperty;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.EntityAcl;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.EntityExtraMetaData;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.EntityExtraMetaDataEntry;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.EntityMetaData;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestEntityDto;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestProperty;
import org.pentaho.platform.repository.usersettings.UserSettingService;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxy;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This Object represents the information stored in the ExportManifest for one file or folder. The
 * <code>ExportManifest</code> object contains a Hashmap of these objects keyed by the path.
 *
 * @author tkafalas
 */
public class ExportManifestEntity {
  private ExportManifestEntityDto rawExportManifestEntity;
  private EntityMetaData entityMetaData;
  private EntityExtraMetaData entityExtraMetaData;
  private EntityAcl entityAcl;
  private List<CustomProperty> customProperties;

  protected ExportManifestEntity() {
    rawExportManifestEntity = new ExportManifestEntityDto();
  }

  protected ExportManifestEntity( String rootFolder, RepositoryFile repositoryFile,
                                  RepositoryFileAcl repositoryFileAcl )
    throws ExportManifestFormatException {
    this();
    ExportManifestProperty rawExportManifestProperty = new ExportManifestProperty();
    createEntityMetaData( rootFolder, repositoryFile );
    createEntityExtraMetaData( repositoryFile );
    createEntityAcl( repositoryFileAcl );
    rawExportManifestProperty.setEntityMetaData( entityMetaData );
    rawExportManifestProperty.setEntityExtraMetaData( entityExtraMetaData );
    rawExportManifestProperty.setEntityAcl( entityAcl );
  }

  protected ExportManifestEntity( File file, String userId, String projectId, Boolean isFolder, Boolean isHidden,
      Boolean isSchedulable ) {
    this();
    ExportManifestProperty rawExportManifestProperty = new ExportManifestProperty();
    createEntityMetaData( file, userId, projectId, isFolder, isHidden, isSchedulable );
    createEntityAcl( userId );
    rawExportManifestProperty.setEntityMetaData( entityMetaData );
    rawExportManifestProperty.setEntityExtraMetaData( entityExtraMetaData );
    rawExportManifestProperty.setEntityAcl( entityAcl );
  }

  private void createEntityExtraMetaData( RepositoryFile repositoryFile ) {
    entityExtraMetaData = new EntityExtraMetaData();

    if ( repositoryFile instanceof RepositoryFileProxy ) {
      RepositoryFileProxy repositoryFileProxy = (RepositoryFileProxy) repositoryFile;
      try {
        for ( Map.Entry<String, Serializable> entry : repositoryFileProxy.getMetadata().entrySet() ) {
          if ( isExtraMetaDataKey( entry.getKey() ) ) {
            entityExtraMetaData
              .addMetadata( new EntityExtraMetaDataEntry( entry.getKey(), String.valueOf( entry.getValue() ) ) );
          }
        }
      } catch ( Exception e ) {
        throw new ExportManifestRepositoryException( "Error while trying get metadata from repository file" );
      }
    }

  }

  private boolean isExtraMetaDataKey( String key ) {
    return
      !key.equals( RepositoryFile.SCHEDULABLE_KEY )
        && !key.equals( RepositoryFile.HIDDEN_KEY )
        && !key.equals( IUnifiedRepository.SYSTEM_FOLDER )
        && !key.equals( ITenantManager.TENANT_ROOT )
        && !key.equals( ITenantManager.TENANT_ENABLED )
        && !key.startsWith( UserSettingService.SETTING_PREFIX );
  }

  private void createEntityAcl( String userId ) {
    entityAcl = new EntityAcl();
    entityAcl.setEntriesInheriting( true );
  }

  private void createEntityMetaData( File file, String userId, String projectId, Boolean isFolder, Boolean isHidden,
      Boolean isSchedulable ) {

    entityMetaData = new EntityMetaData();
    entityMetaData.setCreatedBy( userId );
    entityMetaData.setCreatedDate( XmlGregorianCalendarConverter.asXMLGregorianCalendar( new Date() ) );
    entityMetaData.setDescription( "Project folder for AgileBi Project named: " + projectId );
    entityMetaData.setHidden( isHidden );
    entityMetaData.setSchedulable( isSchedulable );
    entityMetaData.setIsFolder( isFolder );
    entityMetaData.setLocale( LocaleHelper.getLocale().toString() );
    entityMetaData.setName( file.getName() );
    entityMetaData.setPath( StringUtils.replaceChars( file.getPath(), "/\\", "//" ) );
    entityMetaData.setTitle( file.getName() );
    setPath( StringUtils.replaceChars( file.getPath(), "/\\", "//" ) );
  }

  private void createEntityMetaData( String rootFolder, RepositoryFile repositoryFile )
    throws ExportManifestFormatException {

    entityMetaData = new EntityMetaData();
    entityMetaData.setCreatedBy( repositoryFile.getCreatorId() );
    entityMetaData.setCreatedDate( XmlGregorianCalendarConverter.asXMLGregorianCalendar( repositoryFile
        .getCreatedDate() ) );
    entityMetaData.setDescription( repositoryFile.getDescription() );
    entityMetaData.setHidden( repositoryFile.isHidden() );
    entityMetaData.setSchedulable( repositoryFile.isSchedulable() );
    entityMetaData.setIsFolder( repositoryFile.isFolder() );
    entityMetaData.setLocale( LocaleHelper.getLocale().toString() );
    entityMetaData.setName( repositoryFile.getName() );

    /*
     * before testing, strip trailing slash from rootFolder if it exists but only for string comparison with
     * repositoryFile path. This is just a convenience to not fail if someone adds a trailing slash to the repo path
     * when exporting from the command line
     */
    String testRootFolder = rootFolder;
    if ( testRootFolder.endsWith( "/" ) ) {
      testRootFolder = testRootFolder.substring( 0, testRootFolder.length() - 1 );
    }

    if ( !repositoryFile.getPath().startsWith( testRootFolder ) ) {
      throw new ExportManifestFormatException( "File path does not start with rootFolder" );
    }

    String adjustedPath = repositoryFile.getPath().substring( rootFolder.length() );
    entityMetaData.setPath( adjustedPath );
    entityMetaData.setTitle( repositoryFile.getTitle() );
    setPath( adjustedPath );
  }

  private void createEntityAcl( RepositoryFileAcl repositoryFileAcl ) {
    DefaultTenantedPrincipleNameResolver nameResolver = new DefaultTenantedPrincipleNameResolver();

    if ( repositoryFileAcl == null ) {
      return;
    }
    entityAcl = new EntityAcl();
    entityAcl.setEntriesInheriting( repositoryFileAcl.isEntriesInheriting() );
    entityAcl.setOwner( nameResolver.getPrincipleName( repositoryFileAcl.getOwner().getName() ) );
    entityAcl.setOwnerType( repositoryFileAcl.getOwner().getType().name() );
    List<EntityAcl.Aces> aces = entityAcl.getAces();
    aces.clear();

    for ( RepositoryFileAce repositoryFileAce : repositoryFileAcl.getAces() ) {
      EntityAcl.Aces ace = new EntityAcl.Aces();
      ace.setRecipient( nameResolver.getPrincipleName( repositoryFileAce.getSid().getName() ) );
      ace.setRecipientType( repositoryFileAce.getSid().getType().name() );
      List<String> permissions = ace.getPermissions();
      for ( RepositoryFilePermission permission : repositoryFileAce.getPermissions() ) {
        permissions.add( permission.toString() );
      }
      aces.add( ace );
    }
  }

  /**
   * Helper method for importing. Returns a FileRepository object for the the ExportManifestEntity. Will return null if
   * there is no EntityMetaData present although this should not happen if the manifest is present at all.
   *
   * @return RepositoryFile
   */
  public RepositoryFile getRepositoryFile() {
    EntityMetaData emd = getEntityMetaData();
    if ( entityMetaData == null ) {
      return null;
    }
    return new RepositoryFile( null, emd.getName(), emd.isIsFolder(), emd.isHidden(), emd.isSchedulable(), false,
        null, emd.getPath(),
        XmlGregorianCalendarConverter.asDate( emd.getCreatedDate() ), null, false, null, null, null, "en-US", emd
        .getTitle(), emd.getDescription(), null, null, 0, emd.getOwner(), null
    );
  }

  /**
   * Helper method for importing. Returns a FileRepositoryAcl object for the the ExportManifestEntity. Will return null
   * if there is no EntityAcl present.
   *
   * @return RepositoryFile
   */
  public RepositoryFileAcl getRepositoryFileAcl() throws ExportManifestFormatException {
    RepositoryFileAcl repositoryFileAcl;
    EntityAcl entityAcl = getEntityAcl();
    if ( entityAcl == null ) {
      return null;
    }

    ArrayList<RepositoryFileAce> repositoryFileAces = new ArrayList<RepositoryFileAce>();
    RepositoryFileSid rfs;
    for ( EntityAcl.Aces ace : entityAcl.getAces() ) {
      rfs = getSid( ace.getRecipient(), ace.getRecipientType() );
      HashSet<RepositoryFilePermission> permissionSet = new HashSet<RepositoryFilePermission>();
      for ( String permission : ace.getPermissions() ) {
        permissionSet.add( getPermission( permission ) );
      }
      RepositoryFileAce repositoryFileAce = new RepositoryFileAce( rfs, EnumSet.copyOf( permissionSet ) );
      repositoryFileAces.add( repositoryFileAce );
    }

    repositoryFileAcl =
        new RepositoryFileAcl( "", getSid( entityAcl.getOwner(), entityAcl.getOwnerType() ), entityAcl
            .isEntriesInheriting(), repositoryFileAces );

    return repositoryFileAcl;
  }


  /**
   * Helper method for importing. Returns a FileRepositoryExtraMetaData object for the the ExportManifestEntity. Will return null
   * if there is no EntityExtraMetaData present.
   *
   * @return RepositoryFileExtraMetaData
   */
  public RepositoryFileExtraMetaData getRepositoryFileExtraMetaData() throws ExportManifestFormatException {
    RepositoryFileExtraMetaData repositoryFileExtraMetaData;
    EntityExtraMetaData currentEntityExtraMetaData = getEntityExtraMetaData();
    if ( currentEntityExtraMetaData == null ) {
      return null;
    }

    Map<String, Serializable> extraMetaDataMap = new HashMap<>();
    for ( EntityExtraMetaDataEntry entry : currentEntityExtraMetaData.getMetadata() ) {
      extraMetaDataMap.put( entry.getName(), entry.getValue() );
    }

    repositoryFileExtraMetaData = new RepositoryFileExtraMetaData.Builder( "", extraMetaDataMap ).build();

    return repositoryFileExtraMetaData;
  }


  /**
   * Creates a RepositoryFileSid object from the serialized values adding error handling for enum values that may not
   * exist
   *
   * @param name
   * @param type
   * @return
   * @throws ExportManifestFormatException
   */
  private RepositoryFileSid getSid( String name, String type ) throws ExportManifestFormatException {
    RepositoryFileSid.Type typevalue;
    try {
      typevalue = RepositoryFileSid.Type.valueOf( type );
    } catch ( IllegalArgumentException e ) {
      throw new ExportManifestFormatException( Messages.getInstance().getString(
          "ExportManifestFormatException.invalidRepositoryFileSidType", type ), e );
    }
    return new RepositoryFileSid( name, typevalue );
  }

  /**
   * Assigns Enum Permission value from string representation adding error handling if the string is invalid
   *
   * @param stringValue
   * @return integer value
   * @throws ExportManifestFormatException
   */
  private RepositoryFilePermission getPermission( String stringValue ) throws ExportManifestFormatException {
    RepositoryFilePermission value;
    try {
      value = RepositoryFilePermission.valueOf( stringValue );
    } catch ( IllegalArgumentException e ) {
      throw new ExportManifestFormatException( Messages.getInstance().getString(
          "ExportManifestFormatException.invalidPermissionType", stringValue ), e );
    }
    return value;
  }

  /**
   * Builds an ExportManifestEntityDto for use by the ExportManifest Package.
   *
   * @return
   */
  ExportManifestEntityDto getExportManifestEntityDto() {
    // Property list is not kept in sync. Create it now
    List<ExportManifestProperty> rawProperties = rawExportManifestEntity.getExportManifestProperty();
    rawProperties.clear();
    if ( entityMetaData != null ) {
      ExportManifestProperty exportManifestProperty = new ExportManifestProperty();
      exportManifestProperty.setEntityMetaData( entityMetaData );
      rawProperties.add( exportManifestProperty );
    }
    if ( entityExtraMetaData != null ) {
      ExportManifestProperty exportManifestProperty = new ExportManifestProperty();
      exportManifestProperty.setEntityExtraMetaData( entityExtraMetaData );
      rawProperties.add( exportManifestProperty );
    }

    if ( entityAcl != null ) {
      ExportManifestProperty exportManifestProperty = new ExportManifestProperty();
      exportManifestProperty.setEntityAcl( entityAcl );
      rawProperties.add( exportManifestProperty );
    }

    if ( customProperties != null && customProperties.size() > 0 ) {
      ExportManifestProperty exportManifestProperty = new ExportManifestProperty();
      exportManifestProperty.getCustomProperty().addAll( customProperties );
      rawProperties.add( exportManifestProperty );
    }
    return rawExportManifestEntity;
  }

  /**
   * Create this object from the Jaxb bound version of the object.
   *
   * @param exportManifestEntity
   */
  public ExportManifestEntity( ExportManifestEntityDto exportManifestEntity ) {
    this.rawExportManifestEntity = exportManifestEntity;
    for ( ExportManifestProperty exportManifestProperty : exportManifestEntity.getExportManifestProperty() ) {
      if ( exportManifestProperty.getEntityMetaData() != null ) {
        entityMetaData = exportManifestProperty.getEntityMetaData();
      } else if ( exportManifestProperty.getEntityExtraMetaData() != null ) {
        entityExtraMetaData = exportManifestProperty.getEntityExtraMetaData();
      } else if ( exportManifestProperty.getEntityAcl() != null ) {
        entityAcl = exportManifestProperty.getEntityAcl();
      } else if ( exportManifestProperty.getCustomProperty() != null
          && exportManifestProperty.getCustomProperty().size() > 0 ) {
        customProperties = exportManifestProperty.getCustomProperty();
      }
    }
  }

  public boolean isValid() {
    if ( entityMetaData == null ) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * @return the entityMetaData
   */
  public EntityMetaData getEntityMetaData() {
    return entityMetaData;
  }

  /**
   * @param entityMetaData the entityMetaData to set
   */
  public void setEntityMetaData( EntityMetaData entityMetaData ) {
    this.entityMetaData = entityMetaData;
  }

  /**
   * @return the entityExtraMetaData
   */
  public EntityExtraMetaData getEntityExtraMetaData() {
    return entityExtraMetaData;
  }

  /**
   * @param entityExtraMetaData the entityExtraMetaData to set
   */
  public void setExtraEntityMetaData( EntityExtraMetaData entityExtraMetaData ) {
    this.entityExtraMetaData = entityExtraMetaData;
  }


  /**
   * @return the entityAcl
   */
  public EntityAcl getEntityAcl() {
    return entityAcl;
  }

  /**
   * @param entityAcl the entityAcl to set
   */
  public void setEntityAcl( EntityAcl entityAcl ) {
    this.entityAcl = entityAcl;
  }

  /**
   * @return the customProperty
   */
  public List<CustomProperty> getCustomProperties() {
    return customProperties;
  }

  /**
   * @param customProperties the customProperty to set
   */
  public void setCustomProperties( List<CustomProperty> customProperties ) {
    this.customProperties = customProperties;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return rawExportManifestEntity.getPath();
  }

  /**
   * @param path the path to set
   */
  public void setPath( String path ) {
    rawExportManifestEntity.setPath( path );
  }

}
