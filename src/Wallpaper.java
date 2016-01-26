import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

/**
 * Class for changing wallpaper on a timer with a system tray icon, context menu and settings dialog.
 * 
 * @author Nathan Baker <pcnate@gmail.com>;
 * @author Sean Russell <SeanChristopherRussell@gmail.com>
 *
 */
public class Wallpaper {
	/* tray icon name */
	public static TrayIcon trayIcon;
    
    /** flag to determine if should continue the main program loop */
	private static boolean b = true;
	
	/** the last minute that the wallpaper was changed */
	private static int lastChangedMinute = -1;
	
	/** string options in the settings dialog */
	private static String[] comboStrings = { "Every minute", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "hour" };
	
	/** index of the selected string */
	private static int selectedOption = 0;
	
	/** */
	private static java.net.URL logoOneurl = Wallpaper.class.getResource("images/bulb.gif");
	private static java.net.URL logoOneurl2 = Wallpaper.class.getResource("images/");
	private static Image image = Toolkit.getDefaultToolkit().getImage(logoOneurl);
	private static Image image2 = Toolkit.getDefaultToolkit().getImage(logoOneurl2);
	
	/** pause the change timer */
	private static boolean sleep = false;
	
	/** toggle tray icon image */
	private static boolean hide = false;
	
	/** default location to look for images */
    private static File defaultFolder = new File("C:/Users/Public/Pictures/Sample Pictures");
    
    /** list of folders to scan for images */
    public static List<File> folders = new ArrayList<File>();

    /**
     * set the passed file as the desktop background
     *  
     * @param file	file to be assigned as background
     */
    public static void setWallpaper( String file ) {
    	User32.INSTANCE.SystemParametersInfo(20, 0, file , 1);
    }

    /**
     *
     */
    public static interface User32 extends Library {
//    	User32 INSTANCE = (User32) Native.loadLibrary("user32",User32.class,W32APIOptions.DEFAULT_OPTIONS);
//    	boolean SystemParametersInfo (int one, int two, String s ,int three);
    	//from MSDN article
        long SPI_SETDESKWALLPAPER = 20;
        long SPIF_UPDATEINIFILE = 0x01;
        long SPIF_SENDWININICHANGE = 0x02;

        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, new HashMap<Object, Object>() {
            {
                put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
            }
        });

        boolean SystemParametersInfo(
                int uiAction,
                int uiParam,
                String pvParam,
                int fWinIni
        );
    }
    
    /**
     * reset the stored folder listing
     */
    public static void resetFolders() {
    	folders.clear();
    }
  
    /**
     * scan for folders that can be used as the defaults
     */
    public static void scanFolders() {
    	
    	resetFolders();
    	
    	// default location
    	addFolder( defaultFolder );
    	
    	File[] drives;
    	FileSystemView fsv = FileSystemView.getFileSystemView();

    	// returns pathnames for files and directory
    	drives = File.listRoots();

    	// for each pathname in pathname array
    	for( File drive:drives ) {
		  
    		// we only want network drives and local disks
    		switch( fsv.getSystemTypeDescription( drive ) ) {
		  		case "Network Drive":
		  		case "Local Disk":
		  			checkForPicturesFolder( drive );
		  			break;
		  		default:
		  			break;
    		}
    	}
    }
  
    /**
     * check if there are any default folder names that should be checked for images
     * @param path	path to scan
     */
    public static void checkForPicturesFolder( File path ) {
	  
    	/** folders that may contain images */
    	String[] folders = { "wallpaper", "images", "backgrounds" };
	  
    	for( String folder:folders ) {
		  
    		/** whether folder exists in the path */
    		boolean exists = new File( path, folder ).exists();
    		if( exists ) {
    			addFolder( new File( path.toPath()+"//"+folder ) );
    		}
    	}
    }
  
    /**
     * add a folder to the list of folders
     * 
     * @param folder	folder path
     */
    public static void addFolder( File folder ) {
    	folders.add( folder );
    }

    /**
     * randomly select a picture
     */
    public static void pictures() {

    	scanFolders();
    	
    	Date dt = new Date();

    	sleep = false;

    	@SuppressWarnings("deprecation")
    	int minute = dt.getMinutes();

    	lastChangedMinute = minute;

    	ArrayList<String> list = new ArrayList<String>();

    	for( File folder:folders ) {
    		
    		File[] listOfFiles = folder.listFiles();

    		for( int i = 0; i < listOfFiles.length; i++ ) {
//	    		if( listOfFiles[i].toString().contains(".jpg") || listOfFiles[i].toString().contains(".png") ) {
	    		if( listOfFiles[i].toString().contains(".jpg") ) {
	    			list.add( listOfFiles[i].toString() );
	    		}
	    	}
    		
    	}

    	Random randomizer = new Random();
    	String randomPic = list.get( randomizer.nextInt( list.size() ) );
    	System.out.println( randomPic );
    	setWallpaper( randomPic );
    }

    /**
     * setup the GUI and start the main program loop
     * 
     * @param args	program arguments
     */
    public static void main(String[] args) {
    	selectedOption = Arrays.asList( comboStrings ).indexOf("Every minute");

    	//checking for support
	    if(!SystemTray.isSupported()){
	    	System.out.println("System tray is not supported !!! ");
	        return ;
	    }

	    SystemTray systemTray = SystemTray.getSystemTray();

	    //popupmenu
	    PopupMenu trayPopupMenu = new PopupMenu();

	    //2nd menuitem of popupmenu
	    MenuItem next = new MenuItem("Random");
	    next.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            pictures();
	        }
	    });
	    trayPopupMenu.add(next);
	    //1t menuitem for popupmenu
	    MenuItem action = new MenuItem("Settings");
	    action.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	JComboBox comboOptions = new JComboBox( comboStrings );
	        	comboOptions.setSelectedIndex( selectedOption );

	        	JOptionPane.showConfirmDialog(null, comboOptions, "Choose change rate", JOptionPane.OK_CANCEL_OPTION);
	            selectedOption = comboOptions.getSelectedIndex();

	        }
	    });
	    trayPopupMenu.add(action);
	    
	    final MenuItem folderList = new MenuItem("Included Folders");
	    folderList.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	ArrayList<JCheckBox> list = new ArrayList<JCheckBox>();
	        	for(int i = 0; i < folders.size(); i++) {
	        		JCheckBox newbie = new JCheckBox(folders.get(i).toString());
	        		list.add(newbie);
	        	}
	        	Object[] listOfFolders = list.toArray();
	        	JCheckBox box = new JCheckBox( "option" );
	        	JOptionPane.showConfirmDialog(null, listOfFolders, "Choose Included Folders", JOptionPane.OK_CANCEL_OPTION);
	        	
	        }
	    });
	    trayPopupMenu.add(folderList);

	    final MenuItem pause = new MenuItem("Pause");
	    pause.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            sleep = !sleep;
	            if( sleep ) {
	            	pause.setLabel("Resume");
		    		try {
						Thread.sleep(1000);
						if( checkTime() && !sleep ) {
							pictures();
						}
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            } else {
	            	pause.setLabel("Pause");
	            }
	        }
	    });
	    trayPopupMenu.add(pause);
	    
	    final MenuItem toggleIcon = new MenuItem("Hide Icon");
	    toggleIcon.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		hide = !hide;
	    		if(hide == true) {
	    			toggleIcon.setLabel("Show Icon");
	    			trayIcon.setImage(image2);
	    		}
	    		else {
	    			toggleIcon.setLabel("Hide Icon");
	    			trayIcon.setImage(image);
	    		}
	    	}
	    });
	    trayPopupMenu.add(toggleIcon);

	    //2nd menuitem of popupmenu
	    MenuItem close = new MenuItem("Close");
	    close.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            System.exit(0);
	        }
	    });
	    trayPopupMenu.add(close);

	    //setting tray icon
	    trayIcon = new TrayIcon(image, "Changer", trayPopupMenu);
	    //adjust to default size as per system recommendation
	    trayIcon.setImageAutoSize(true);

	    trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                	for(int i = 0; i < folders.size(); i++){
                		System.out.println(folders.get(i));
                	}
                	sleep = !sleep;
                	pictures();
                }
            }
        });

	    try{
	        systemTray.add(trayIcon);
	    }catch(AWTException awtException){
	        awtException.printStackTrace();
	    }
	    run();
	    System.out.println("end of main");

    }

    /**
     * check if we have switched to the next interval
     * 
     * @return 
     */
    public static boolean checkTime() {

    	Date dt = new Date();

    	@SuppressWarnings("deprecation")
    	int minutes = dt.getMinutes();

    	String selected = comboStrings[selectedOption];

    	int selectedInt = 0;

    	switch( selected ) {
	  		case "Every minute":
	  			selectedInt = 1;
	  			return minutes != lastChangedMinute;
	  		case "hour":
	  			selectedInt = 0;
	  			return minutes != lastChangedMinute && minutes == 0;
	  		default:
	  			String[] results = selected.split(" ");
	  			selectedInt = Integer.parseInt( results[0] );
	  			return minutes != lastChangedMinute && ( minutes % selectedInt ) == 0;
    	}
    }

  	/**
  	 * program loop
  	 */
  	public static void run() {
  		pictures();

	    while( b ) {

	    	try {
	    		Thread.sleep(1000);
	    		if( checkTime() && !sleep ) {
	    			pictures();
	    		}

	    	} catch (InterruptedException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}

	    }
  	}

}
