import java.util.ArrayList;

import javax.swing.JCheckBox;

public class IncludedDirectories {
	private JCheckBox box;
	private boolean active;
	
	IncludedDirectories(){
		this.box = new JCheckBox();
		this.active = true;
	}
	
	IncludedDirectories(JCheckBox box, boolean b) {
		this.box = box;
		this.active = b;
	}

	public JCheckBox getBox() {
		return box;
	}

	public void setBox(JCheckBox box) {
		this.box = box;
	}

	public boolean isB() {
		return active;
	}

	public void setB(boolean b) {
		this.active = b;
	}
	
}
