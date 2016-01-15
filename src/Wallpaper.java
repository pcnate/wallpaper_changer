import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.sun.jna.Library;
import com.sun.jna.Native;
//import com.sun.jna.platform.win32.WinDef.HWND;
//import com.sun.jna.platform.win32.WinDef.PVOID;
import com.sun.jna.win32.W32APIOptions;

public class Wallpaper {
	private static int x = 30;
	private static int count = 1;
	private static boolean b = true;
	private static int lastChangedMinute = -1;
	private static String[] comboStrings = { "Every minute", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "hour" };
	private static int selectedOption = 0;
	private static java.net.URL logoOneurl = Wallpaper.class.getResource("images/bulb.gif");
	private static File folder = new File("C:/Users/Public/Pictures/Sample Pictures");

  public static interface User32 extends Library {
    User32 INSTANCE = (User32) Native.loadLibrary("user32",User32.class,W32APIOptions.DEFAULT_OPTIONS);
    boolean SystemParametersInfo (int one, int two, String s ,int three);
  }

  public static void pictures() {
	Date dt = new Date();
	  
	@SuppressWarnings("deprecation")
	int minute = dt.getMinutes();
	
	lastChangedMinute = minute;
 
    ArrayList<String> list = new ArrayList<String>();
//    File folder = new File("M:/wallpaper");

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

  public static void setWallpaper( String file ) {
    User32.INSTANCE.SystemParametersInfo(0x0014, 0, file , 1);
  }

  public static void main(String[] args) {
	  selectedOption = Arrays.asList( comboStrings ).indexOf("15 minutes");

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
	    MenuItem action = new MenuItem("Speed");
	    action.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	JComboBox comboOptions = new JComboBox( comboStrings );
	        	comboOptions.setSelectedIndex( selectedOption );

	        	JOptionPane.showConfirmDialog(null, comboOptions, "Enter switch rate (in minutes)", JOptionPane.OK_CANCEL_OPTION);
	            selectedOption = comboOptions.getSelectedIndex();
	            
	        }
	    });     
	    trayPopupMenu.add(action);

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

	    try{
	        systemTray.add(trayIcon);
	    }catch(AWTException awtException){
	        awtException.printStackTrace();
	    }
	    run();
	    System.out.println("end of main");

  }
  
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
  
  public static void run() {
	  pictures();
	  	
	    while( b ) {

	      try {
        	Thread.sleep(1000);
        	if( checkTime() ) {
        		pictures();
        	}

	      } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      }

	    }
  }

}
