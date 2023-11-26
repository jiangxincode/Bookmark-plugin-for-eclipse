package bookmark.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.Path;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import bookmark.constant.Constant;
import bookmark.utils.PersistantUtils;
import bookmark.utils.ValidationUtils;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.InternalClassFileEditorInput;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class BookmarkView extends ViewPart {



	private TreeViewer viewer;

	private Action addFolderAction;
	private Action addBookmarkAction;
	private Action addAllBookmarkAction;
	private Action deleteAction;
	private Action renameAction;
	private Action doubleClickAction;

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent) parent).hasChildren();
			return false;
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_FILE;
			if (obj instanceof TreeParent)
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			// if need to change customize image
			// return new Image(null, new FileInputStream("images/file.gif"));
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public BookmarkView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		// get data from store or else do initialization
		TreeParent invisibleRoot = PersistantUtils.loadPersistantData();

		// set data source
		viewer.setInput(invisibleRoot);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "bookmark.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {

				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()) {
					TreeObject target = (TreeObject) selection.getFirstElement();
					if (target instanceof TreeParent) {
						manager.add(addBookmarkAction);
						manager.add(addFolderAction);
						manager.add(deleteAction);
						manager.add(renameAction);
						manager.add(addAllBookmarkAction);
					} else {
						manager.add(deleteAction);
					}
				} else {
					manager.add(addBookmarkAction);
					manager.add(addFolderAction);
					manager.add(addAllBookmarkAction);
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {

	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.addBookmarkAction);
		manager.add(this.addFolderAction);
		manager.add(this.deleteAction);
	}

	@SuppressWarnings("deprecation")
	private void makeActions() {

		// remove selected folder or bookmark
		this.deleteAction = new Action() {
			public void run() {
				// get invisibleRoot
				TreeParent invisibleRoot = (TreeParent) viewer.getInput();

				// get selection
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj == null) {
					showMessage("No selection in Bookmark View.");
				} else {
					TreeObject target = (TreeObject) obj;
					// confirm dialog
					String title = "Confirm";
					String question = "Do you really want to delelte this whole node?";
					boolean answer = MessageDialog.openConfirm(null, title, question);
					if (answer) {
						invisibleRoot.removeSelectedChild(target);
					}
					// keep expand situation
					Object[] expandedElements = viewer.getExpandedElements();
					TreePath[] expandedTreePaths = viewer.getExpandedTreePaths();

					// update data source
					viewer.setInput(invisibleRoot);

					viewer.setExpandedElements(expandedElements);
					viewer.setExpandedTreePaths(expandedTreePaths);

					// save to persistent
					PersistantUtils.savePersistantData(invisibleRoot);
				}
			}
		};
		this.deleteAction.setText("Delete");
		this.deleteAction.setToolTipText("Delete selected folder or bookmark.");
		this.deleteAction.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));

		// use user input to add parent
		this.addFolderAction = new Action() {
			public void run() {
				String parentName;
				// create an input dialog to get user input
				String dialogTitle = "Input";
				String dialogMessage = "Please enter folder name:";
				String initialValue = "";
				InputDialog dlg = new InputDialog(null, dialogTitle, dialogMessage, initialValue,
						ValidationUtils.getIInputValidatorInstance());
				dlg.open();
				if (dlg.getReturnCode() != Window.OK) {
					return;
				} else {
					parentName = dlg.getValue();
				}

				// new a folder
				TreeParent newParent = new TreeParent(parentName);
				// get invisible root
				TreeParent invisibleRoot = (TreeParent) viewer.getInput();

				// get selection
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj == null) {
					// no selection, default to add to the invisibleRoot
					invisibleRoot.addChild(newParent);
				} else {
					invisibleRoot.addChild((TreeObject) obj, newParent);
				}

				// keep expand situation
				Object[] expandedElements = viewer.getExpandedElements();
				TreePath[] expandedTreePaths = viewer.getExpandedTreePaths();

				// update data source
				viewer.setInput(invisibleRoot);

				viewer.setExpandedElements(expandedElements);
				viewer.setExpandedTreePaths(expandedTreePaths);

				// save to persistent
				PersistantUtils.savePersistantData(invisibleRoot);
			}
		};
		this.addFolderAction.setText("Add folder here");
		this.addFolderAction.setToolTipText("Add folder here");
		this.addFolderAction.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));

		// add book mark to selected parent
		this.addBookmarkAction = new Action() {
			public void run() {
				// get active editor info
				String relativePath = "";
				String projectName = "";

				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IEditorPart editor = page.getActiveEditor();

				if (editor != null) {
					IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
					IFile file = input.getFile();
					relativePath = file.getProjectRelativePath().toOSString();
					projectName = file.getProject().getName();
				} else {
					// check selection from package explorer
					ISelectionService service = getSite().getWorkbenchWindow().getSelectionService();
					IStructuredSelection packageExploerSelection = (IStructuredSelection) service
							.getSelection("org.eclipse.jdt.ui.PackageExplorer");
					if (packageExploerSelection != null) {
						Object obj = packageExploerSelection.getFirstElement();
						if (obj == null) {
							showMessage("No selection in package explorer");
							return;
						} else {
							// get file info for selection from package explorer
							IResource resource = ((ICompilationUnit) obj).getResource();

							if (resource.getType() == IResource.FILE) {
								IFile ifile = (IFile) resource;
								relativePath = ifile.getProjectRelativePath().toOSString();
								projectName = ifile.getProject().getName();
							}
						}
					} else {
						showMessage("No active editor or selection in package explorer");
						return;
					}
				}

				// create leaf with file info
				TreeObject child = new TreeObject(relativePath, projectName);

				// get invisibleRoot
				TreeParent invisibleRoot = (TreeParent) viewer.getInput();

				// get selection
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj == null) {
					// default to insert invisibleRoot
					invisibleRoot.addChild(child);
				} else {
					TreeObject targetParent = (TreeObject) obj;
					invisibleRoot.addChild(targetParent, child);
				}

				// keep expand situation
				Object[] expandedElements = viewer.getExpandedElements();
				TreePath[] expandedTreePaths = viewer.getExpandedTreePaths();

				// update data source
				viewer.setInput(invisibleRoot);

				viewer.setExpandedElements(expandedElements);
				viewer.setExpandedTreePaths(expandedTreePaths);

				// save to persistent
				PersistantUtils.savePersistantData(invisibleRoot);
			}
		};
		this.addBookmarkAction.setText("Add bookmark here");
		this.addBookmarkAction.setToolTipText("Add bookmark here");
		this.addBookmarkAction.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_BKMRK_TSK));

		addAllBookmarkAction = new Action() {
			public void run() {
				// get active editor info
				String relativePath = "";
				String projectName = "";

				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IEditorPart[] editors = page.getEditors();
				if (editors != null) {
					for (int i = 0; i < editors.length; i++) {
						IEditorPart editor = editors[i];

						IEditorInput iEditorInput = editor.getEditorInput();

						TreeObject child = null;

						if(iEditorInput instanceof IFileEditorInput)
						{
							IFileEditorInput input = (IFileEditorInput) iEditorInput;
							IFile file = input.getFile();
							relativePath = file.getProjectRelativePath().toOSString();
							projectName = file.getProject().getName();
							child = new TreeObject(relativePath, projectName);
						} else if(iEditorInput instanceof IClassFileEditorInput)
						{
							IClassFileEditorInput input = (IClassFileEditorInput) iEditorInput;
							IClassFile file = input.getClassFile();
							child = new TreeObject(file);
						}

						// get invisibleRoot
						TreeParent invisibleRoot = (TreeParent) viewer.getInput();

						// get selection
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if (obj == null) {
							// default to insert invisibleRoot
							invisibleRoot.addChild(child);
						} else {
							TreeObject targetParent = (TreeObject) obj;
							invisibleRoot.addChild(targetParent, child);
						}

						// keep expand situation
						Object[] expandedElements = viewer.getExpandedElements();
						TreePath[] expandedTreePaths = viewer.getExpandedTreePaths();

						// update data source
						viewer.setInput(invisibleRoot);

						viewer.setExpandedElements(expandedElements);
						viewer.setExpandedTreePaths(expandedTreePaths);

						// save to persistent
						PersistantUtils.savePersistantData(invisibleRoot);
					}
				} else {
					showMessage("No active editor");
					return;
				}

			}
		};
		this.addAllBookmarkAction.setText("Add opened files here");
		this.addAllBookmarkAction.setToolTipText("Add opened files here");
		this.addAllBookmarkAction.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_BKMRK_TSK));

		// rename the node
		renameAction = new Action() {
			public void run() {

				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				if (obj != null) {
					TreeObject treeObject = (TreeObject) obj;
					if (treeObject.flag == Constant.PARENT) {

						String parentName = treeObject.getName();
						// create an input dialog to get user input
						String dialogTitle = "Input";
						String dialogMessage = "Please enter folder name:";
						InputDialog dlg = new InputDialog(null, dialogTitle, dialogMessage, parentName,
								ValidationUtils.getIInputValidatorInstance());
						dlg.open();
						if (dlg.getReturnCode() != Window.OK) {
							return;
						} else {
							parentName = dlg.getValue();
						}

						treeObject.setName(parentName);
					}
				}

				TreeParent invisibleRoot = (TreeParent) viewer.getInput();
				viewer.setInput(invisibleRoot);
				PersistantUtils.savePersistantData(invisibleRoot);
			}
		};

		this.renameAction.setText("Rename");
		this.renameAction.setToolTipText("Rename the folder.");

		// double click action to open file
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				if (obj != null) {
					TreeObject treeObject = (TreeObject) obj;
					if (treeObject.flag == 1) {
						// expand and collapse folder when double click
						if (viewer.getExpandedState(treeObject)) {
							viewer.collapseToLevel(treeObject, 1);
						} else {
							viewer.expandToLevel(treeObject, 1);
						}
						return;
					}

					if(treeObject.getiClassFile() == null) {
						String relativePath = treeObject.getName();
						IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
						IProject project = workspaceRoot.getProject(treeObject.getProjectName());
						IFile file1 = project.getFile((new Path(relativePath)));
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = window.getActivePage();
						IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
								.getDefaultEditor(file1.getName());

						// if no right editor to find, use default text editor
						try {
							if (desc == null) {
								page.openEditor(new FileEditorInput(file1), "org.eclipse.ui.DefaultTextEditor");
							} else {
								page.openEditor(new FileEditorInput(file1), desc.getId());
							}
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					} else {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = window.getActivePage();
						try {
							page.openEditor(new InternalClassFileEditorInput(treeObject.getiClassFile()), "org.eclipse.ui.DefaultTextEditor");
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "Bookmark View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}


}