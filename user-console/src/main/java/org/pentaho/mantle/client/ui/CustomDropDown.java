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


package org.pentaho.mantle.client.ui;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;
import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;

public class CustomDropDown extends HorizontalPanel implements HasText {

  private static final String STYLE = "custom-dropdown";
  private final PopupPanel popup = new PopupPanel( true, true ) {
    private FocusPanel pageBackground = null;

    public void show() {
      // show glass pane
      super.show();
      if ( pageBackground == null ) {
        pageBackground = new FocusPanel() {
          public void onBrowserEvent( Event event ) {
            int type = event.getTypeInt();
            switch ( type ) {
              case Event.ONKEYDOWN: {
                if ( (char) event.getKeyCode() == KeyCodes.KEY_ESCAPE ) {
                  event.stopPropagation();
                  popup.hide();
                }
                return;
              }
            }
            super.onBrowserEvent( event );
          };
        };
        pageBackground.addClickHandler( new ClickHandler() {
          public void onClick( ClickEvent event ) {
            popup.hide();
            pageBackground.setVisible( false );
            pageBackground.getElement().getStyle().setDisplay( Display.NONE );
          }
        } );
        RootPanel.get().add( pageBackground, 0, 0 );
      }
      super.center();
      pageBackground.setSize( "100%", Window.getClientHeight() + Window.getScrollTop() + "px" ); //$NON-NLS-1$ //$NON-NLS-2$
      pageBackground.setVisible( true );
      pageBackground.getElement().getStyle().setDisplay( Display.BLOCK );

      // hide <embeds>
      // TODO: migrate to GlassPane Listener
      FrameUtils.toggleEmbedVisibility( false );

      // Notify listeners that we're showing a dialog (hide PDFs, flash).
      GlassPane.getInstance().show();
    }

    public void hide( boolean autoClosed ) {
      if ( !isShowing() ) {
        return;
      }

      super.hide( autoClosed );
      pageBackground.setVisible( false );
      GlassPane.getInstance().hide();
    }

    protected void onPreviewNativeEvent( final NativePreviewEvent event ) {
      // Switch on the event type
      int type = event.getTypeInt();
      switch ( type ) {
        case Event.ONKEYDOWN: {
          Event nativeEvent = Event.as( event.getNativeEvent() );
          if ( (char) nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE ) {
            event.cancel();
            hide();
            CustomDropDown.this.getElement().focus();
          } else if ( nativeEvent.getKeyCode() == KeyCodes.KEY_TAB ) {
            hide();
            if ( nativeEvent.getShiftKey() ) {
              ElementUtils.tabPrevious( CustomDropDown.this.getElement() );
            } else {
              ElementUtils.tabNext( CustomDropDown.this.getElement() );
            }
            nativeEvent.preventDefault();
          }
        }
        break;
      }
    };
  };

  private MenuBar menuBar;
  private Command command;
  private boolean enabled = true;
  private boolean pressed = false;
  private Label label = new Label( "", false );

  public enum MODE {
    MAJOR, MINOR
  }

  public CustomDropDown( String labelText, MenuBar menuBar, MODE mode ) {
    Roles.getButtonRole().set( this.getElement() );
    Roles.getButtonRole().setTabindexExtraAttribute( this.getElement(), 0 );
    Roles.getButtonRole().setAriaHaspopupProperty( this.getElement(), true );
    this.menuBar = menuBar;

    sinkEvents( Event.ONCLICK | Event.MOUSEEVENTS | Event.KEYEVENTS );

    setText( labelText );
    label.setStyleName( "custom-dropdown-label" );
    // label.addMouseListener(this);
    add( label );
    Label dropDownArrow = new Label();
    add( dropDownArrow );
    setCellWidth( dropDownArrow, "100%" );
    dropDownArrow.getElement().getParentElement().addClassName( "custom-dropdown-arrow" );

    // prevent double-click from selecting text
    ElementUtils.preventTextSelection( getElement() );
    ElementUtils.preventTextSelection( label.getElement() );

    popup.setStyleName( "custom-dropdown-popup" );
    if ( MODE.MAJOR.equals( mode ) ) {
      popup.getElement().setId( "customDropdownPopupMajor" );
      popup.setStyleDependentName( "major", true );
      dropDownArrow.getElement().getParentElement().addClassName( "custom-dropdown-arrow-major" );
    } else {
      popup.getElement().setId( "customDropdownPopupMinor" );
      dropDownArrow.getElement().getParentElement().addClassName( "custom-dropdown-arrow-minor" );
    }
    popup.addCloseHandler( new CloseHandler<PopupPanel>() {
      public void onClose( CloseEvent<PopupPanel> event ) {
        pressed = false;
        if ( enabled ) {
          removeStyleDependentName( "pressed" );
          removeStyleDependentName( "hover" );
        }
      }
    } );

    setStyleName( STYLE );
  }

  public void onBrowserEvent( Event event ) {
    super.onBrowserEvent( event );
    if ( ( ( event.getTypeInt() & Event.ONCLICK ) == Event.ONCLICK )
            || event.getKeyCode() == KeyCodes.KEY_ENTER ) {
      if ( enabled && !pressed ) {
        pressed = true;
        addStyleDependentName( "pressed" );
        removeStyleDependentName( "hover" );
        popup.setWidget( menuBar );

        int popupWidth = getOffsetWidth() - 2;
        popup.setWidth( popupWidth + "px" );

        popup.setPopupPositionAndShow( new PositionCallback() {
          public void setPosition( int offsetWidth, int offsetHeight ) {
            popup.setPopupPosition( getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight() - 1 );
          }
        } );
        menuBar.focus();
      }
    } else if ( ( event.getTypeInt() & Event.ONMOUSEOVER ) == Event.ONMOUSEOVER ) {
      if ( enabled ) {
        addStyleDependentName( "hover" );
      }
    } else if ( ( event.getTypeInt() & Event.ONMOUSEOUT ) == Event.ONMOUSEOUT ) {
      if ( enabled && !pressed ) {
        removeStyleDependentName( "pressed" );
        removeStyleDependentName( "hover" );
      }
    } else if ( ( event.getTypeInt() & Event.ONMOUSEUP ) == Event.ONMOUSEUP ) {
      if ( enabled ) {
        removeStyleDependentName( "pressed" );
        if ( command != null ) {
          try {
            command.execute();
          } catch ( Exception e ) {
            // don't fail because some idiot you are calling fails
          }
        }
      }
    }
  }

  public String getText() {
    return label.getText();
  }

  public void setText( String text ) {
    label.setText( text );
  }

  public MenuBar getMenuBar() {
    return menuBar;
  }

  protected void setMenuBar( MenuBar menuBar ) {
    this.menuBar = menuBar;
  }

  public void hidePopup() {
    if ( popup.isShowing() ) {
      popup.hide();
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
    if ( enabled ) {
      removeStyleDependentName( "disabled" );
    } else {
      addStyleDependentName( "disabled" );
    }
  }

}
