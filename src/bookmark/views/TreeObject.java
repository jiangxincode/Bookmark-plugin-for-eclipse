package bookmark.views;

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IClassFile;

import bookmark.constant.Constant;

/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content
 * (like Task List, for example).
 */
public class TreeObject implements IAdaptable, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -8806312158202601104L;
	private String name;
	private TreeParent parent;
	protected int flag;
	private String projectName;
	private IClassFile iClassFile;

	public TreeObject(String name) {
		this.name = name;
		this.flag = Constant.CHILD;
		this.projectName = "";
	}

	public TreeObject(String name, String projectName) {
		this.name = name;
		this.flag = Constant.CHILD;
		this.projectName = projectName;
	}

	public TreeObject(IClassFile iClassFile) {
		this.iClassFile = iClassFile;
		this.flag = Constant.CHILD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}



	public IClassFile getiClassFile() {
		return iClassFile;
	}

	public void setiClassFile(IClassFile iClassFile) {
		this.iClassFile = iClassFile;
	}

	public String toString() {
		return getName();
	}

	/**
	 * Override equals method to use name to compare two TreeObject
	 */
	public boolean equals(Object object) {
		if ((object instanceof TreeObject) && ((TreeObject) object).getName() == this.getName()) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class key) {
		return null;
	}
}