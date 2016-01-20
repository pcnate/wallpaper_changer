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
import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import com.sun.jna.Library;
import com.sun.jna.Native;
//import com.sun.jna.platform.win32.WinDef.HWND;
//import com.sun.jna.platform.win32.WinDef.PVOID;
import com.sun.jna.win32.W32APIOptions;

/**
 * Class for changing wallpaper on a timer with a system tray icon, context menu and settings dialog.
 * 
 * @author Nathan Baker <pcnate@gmail.com>;
 * @author Sean Russell
 *
 */
public class Wallpaper {
    
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
	
	/** pause the change timer */
	private static boolean sleep = false;
	
	/** default location to look for images */
    private static File defaultFolder = new File("C:/Users/Public/Pictures/Sample Pictures");
    private static File folder = new File("C:/Users/Public/Pictures/Sample Pictures");
	//private static File folder = new File("M:/wallpaper");

    /**
     *
     */
    public static interface User32 extends Library {
    	User32 INSTANCE = (User32) Native.loadLibrary("user32",User32.class,W32APIOptions.DEFAULT_OPTIONS);
    	boolean SystemParametersInfo (int one, int two, String s ,int three);
    }
  
    /**
     * scan for folders that can be used as the defaults
     */
    public static void scanFolders() {
    	
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
    	System.out.println( "adding folder: "+folder.toString() );
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

    	File[] listOfFiles = folder.listFiles();

    	for( int i = 0; i < listOfFiles.length; i++ ) {
    		if( listOfFiles[i].toString().contains(".jpg") ) {
    			list.add( listOfFiles[i].toString() );
    		}
    	}

    	Random randomizer = new Random();
    	String randomPic = list.get( randomizer.nextInt( list.size() ) );
    	setWallpaper( randomPic );
    }

    /**
     * set the passed file as the desktop background
     *  
     * @param file	file to be assigned as background
     */
    public static void setWallpaper( String file ) {
    	User32.INSTANCE.SystemParametersInfo(0x0014, 0, file , 1);
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
	    Image image = Toolkit.getDefaultToolkit().getImage(logoOneurl);

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

	        	JOptionPane.showConfirmDialog(null, comboOptions, "Enter switch rate (in minutes)", JOptionPane.OK_CANCEL_OPTION);
	            selectedOption = comboOptions.getSelectedIndex();

	        }
	    });
	    trayPopupMenu.add(action);

	    MenuItem pause = new MenuItem("pause");
	    pause.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            sleep = !sleep;
	        }
	    });
	    trayPopupMenu.add(pause);

	    //2nd menuitem of popupmenu
	    MenuItem close = new MenuItem("Close");
	    close.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            System.exit(0);
	        }
	    });
	    trayPopupMenu.add(close);

	    //setting tray icon
	    TrayIcon trayIcon = new TrayIcon(image, "Changer", trayPopupMenu);
	    //adjust to default size as per system recommendation
	    trayIcon.setImageAutoSize(true);

	    trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
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
