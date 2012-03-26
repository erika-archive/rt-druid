/*
 * Created on Jul 12, 2005
 *
 * $Id: OneResourceSelectionDialog.java,v 1.1 2005/09/28 15:22:29 durin Exp $
 */
package com.eu.evidence.rtdruid.ui.common;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * TODO Commentare && Tradurre
 * 
 * @author Nicola Serreli
 */
public class OneResourceSelectionDialog extends MultipleResourceSelectionDialog {

	protected ElementTreeSelectionDialog dialog;

	/**
	 * Constructs an instance of <code>OneResourceSelectionDialog</code>.
	 * 
	 * @param parent
	 *            The parent shell for the dialog
	 * @param labelProvider
	 *            the label provider to render the entries
	 * @param contentProvider
	 *            the content provider to evaluate the tree structure
	 */
	public OneResourceSelectionDialog(Shell shell, String title,
			String message, IContainer root, ISelectionStatusValidator validator) {
		super(shell, title, message, root, validator);

		dialog.setAllowMultiple(false);
	}
}