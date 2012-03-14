/*
 * Created on 8-giu-2004
 *
 * $Id: WorkerOilConfWriter.java,v 1.6 2008/01/25 15:58:04 durin Exp $
 */
package com.eu.evidence.rtdruid.internal.modules.oil.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.eu.evidence.rtdruid.desk.RTDFactory;
import com.eu.evidence.rtdruid.desk.RtdruidLog;
import com.eu.evidence.rtdruid.internal.modules.oil.exceptions.OilCodeWriterException;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.IWritersKeywords;
import com.eu.evidence.rtdruid.io.MultiSourceImporterFactory;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilWriterBuffer;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.RtosFactory;
import com.eu.evidence.rtdruid.vartree.ITreeInterface;
import com.eu.evidence.rtdruid.vartree.IVarTree;
import com.eu.evidence.rtdruid.vartree.data.ObjectWithID;
import com.eu.evidence.rtdruid.vartree.data.init.IVTResource;
import com.eu.evidence.rtdruid.vartree.data.init.RTD_XMI_Factory;
import com.eu.evidence.rtdruid.vartree.tools.Search;

/**
 * An Ant Task that perform Code Configuration of a system described by an Oil
 * File.
 * 
 * @author Nicola Serreli
 * 
 */
public class WorkerOilConfWriter {

	/** The default left part of an output. See {@link #myLog myLog}. */
	private final static String LEFT = "OIL WRITER";

	/** The input file (oil) */
	protected ArrayList<String> inputFiles;

	/** The directory where store all outputs */
	protected String outputDir = null;

	/** One or more options for Code Generator */
	protected HashMap<String, Object> options = new HashMap<String, Object>();

	/** The default separator for current OS */
	static final String dirSep = java.lang.System.getProperty("file.separator");

	protected Logger logger;

	public WorkerOilConfWriter(Logger logger) {
		this.logger = logger;
		this.inputFiles = new ArrayList<String>();
	}

	/**
	 * Add a new option of the writer
	 * 
	 * @param option
	 *            the ID of the option
	 * @param value
	 *            the current value
	 * 
	 */
	public void addOption(String option, String value) {
		options.put(option, value);
	}

	/**
	 * Add a new option of the writer
	 * 
	 * @param option
	 *            the ID of the option
	 * @param value
	 *            the current value
	 * 
	 */
	public void addOptions(HashMap<String, ? extends Object> values) {
		options.putAll(values);
	}

	/**
	 * Specifies the input file (if there was already specified one or more
	 * input file, only this one is considered)
	 * 
	 * @param fileName
	 *            contains the name of Oil file
	 */
	public void setInputfile(String fileName) {
		inputFiles.clear();
		if (fileName != null) {
			inputFiles.add(fileName);
		}
	}

	/**
	 * Add the specified file in the input list
	 * 
	 * @param fileName
	 *            contains the name of Oil file
	 */
	public void addInputfile(String fileName) {
		if (fileName != null && !inputFiles.contains(fileName)) {
			inputFiles.add(fileName);
		}
	}

	/**
	 * Specifies the absolute path of directory that will contains the output
	 * 
	 * @param value
	 *            the output directory name
	 */
	public void setOutputdir(String value) {
		outputDir = value;
		if (outputDir == null || outputDir.length() == 0) {
			outputDir = "";
			// } else if (!outputDir.endsWith(dirSep)) {
			// outputDir += dirSep;
		}

	}

	/**
	 * Returns the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * This method loads the specified oil file.
	 * 
	 * @return the VarTree that contains the loaded file.
	 * 
	 * @throws OilWorkerException
	 */
	protected IVarTree load() throws OilWorkerException {
		IVarTree vt = (IVarTree) RTDFactory.get(IVarTree.class);

		try {

			// prepare
			ArrayList<MultiSourceImporterFactory.LoadHelper> helpers = new ArrayList<MultiSourceImporterFactory.LoadHelper>();
			ArrayList<String> simpleLoad = new ArrayList<String>();
			for (String inputFile : inputFiles) {

				InputStream input;
				try {
					input = new FileInputStream(inputFile);
				} catch (FileNotFoundException e) {
					throw new OilWorkerException(e.getMessage());
				}

				boolean found = false;
				// check already existing helpers
				for (MultiSourceImporterFactory.LoadHelper helper : helpers) {
					if (helper.add(inputFile, input)) {
						found = true;
						break;
					}
				}

				if (!found) {
					// check a new helper
					MultiSourceImporterFactory.LoadHelper helper = new MultiSourceImporterFactory.LoadHelper();
					if (helper.add(inputFile, input)) {
						found = true;
						helpers.add(helper);
					}
				}

				if (!found) {
					// ok. it should be handled as single file
					simpleLoad.add(inputFile);
				}
			}

			// load
			for (MultiSourceImporterFactory.LoadHelper helper : helpers) {

				for (String s: helper.getNames()) {
					myLog("LOAD", s);
				}
				
				try {
					com.eu.evidence.rtdruid.vartree.data.System root = helper.load();
					mergeInput(vt, root);
				} catch (Exception e) {
					throw new OilWorkerException(e.getMessage());
				}

			}
			
			// load
			for (String inputFile : simpleLoad) {

				myLog("LOAD", inputFile);
				// load and parse the input file
				IVTResource res = (IVTResource) new RTD_XMI_Factory()
						.createResource(URI.createFileURI(inputFile));
				try {
					res.load(new FileInputStream(inputFile), null);
					mergeInput(vt, res);
				} catch (Exception e) {
					throw new OilWorkerException("Unable to load \""
							+ inputFile + "\" caused by :" + e.getMessage(), e);
				}
			}

		} catch (RuntimeException e) {
			// e.printStackTrace();
			throw new OilWorkerException(e.getMessage());
		}

		return vt;
	}

	protected void mergeInput(IVarTree vt, IVTResource res) {
		EList<EObject> objList = res.getContents();
		if (objList.size() > 0) {
			IVarTree vtt = RTDFactory.newVarTree();
			vtt.setRoot(res);
//			
//			System.out.println("\t------------OI1L------------\n\n");
//			System.out.println(Vt2StringUtilities.explodeOilVar(Vt2StringUtilities.writeString(vtt, "ertd")));
//			System.out.println("\t------------FINE OI1L------------\n\n");

			
			mergeInput(vt, ((com.eu.evidence.rtdruid.vartree.data.System) objList.get(0)));
		}
	}
	
	protected void mergeInput(IVarTree vt, com.eu.evidence.rtdruid.vartree.data.System root) {
		// get the old root
		EList<Resource> resList = vt.getResourceSet().getResources();
		if (resList.size() == 0) {
			resList.add((new RTD_XMI_Factory()).createResource());
		}
		EList<EObject> objList = resList.get(0).getContents();
		if (objList.size() == 0) {
			// set the new root
			objList.add(root);
		} else {
			// merge old and new root
			((ObjectWithID) objList.get(0)).merge(root, null, false);
		}
//		System.out.println("\t------------ERTD------------\n\n");
//		System.out.println(Vt2StringUtilities.explodeOilVar(Vt2StringUtilities.writeString(vt, "ertd")));
//		System.out.println("\t------------FINE ERTD------------\n\n");
	}

	/**
	 * Stores all buffers inside the specified directory.
	 * 
	 * @throws OilWorkerException
	 *             if there is one or more errors during store operation
	 * 
	 */
	protected void save(IOilWriterBuffer[] buffers) throws OilWorkerException {

		if (!outputDir.endsWith(dirSep)) {
			outputDir += dirSep;
		}
		for (int bi = 0; bi < buffers.length; bi++) {
			String[] keys = buffers[bi].getKeys();
			for (int ki = 0; ki < keys.length; ki++) {

				// get the output file name for current Buffer
				String fileName = buffers[bi].getFileName(keys[ki]);
				String filePath = buffers[bi].getFilePath(keys[ki]);

				if (!filePath.endsWith(java.lang.System.getProperty("file.separator"))) {
					filePath += java.lang.System.getProperty("file.separator");
				}
				myLog("WRITE", outputDir + filePath + fileName);
				try {
					// build the directory
					File dirs = new File(outputDir + filePath);
					dirs.mkdirs();

					// write the file
					FileOutputStream file = new FileOutputStream(outputDir
							+ filePath + fileName);
					file.write(buffers[bi].get(keys[ki]).toString().getBytes());
					file.close();
				} catch (IOException e) {
					// e.printStackTrace();
					throw new OilWorkerException(e.getMessage());
				}
			}
		}
	}

	/**
	 * Start works. Check specified parameters, load file, compute buffers and
	 * write all outputs
	 * 
	 * @throws OilWorkerException
	 *             if there is an error.
	 */
	public void execute() throws OilWorkerException {
		// ------------- make all checks ----------------
		if (inputFiles.isEmpty()) {
			throw new OilWorkerException("Required an input file");
		}
		if (outputDir == null) {
			throw new OilWorkerException("Required an output dir");
		}

		logger.log(""); // empty line

		{
			String tmp = outputDir;
			char c = dirSep.charAt(0);
			int pos = outputDir.lastIndexOf(c);
			int pos2 = 0;

			if (pos == -1) {
				// do nothing
			} else if (pos == (outputDir.length() - 1)) {
				pos2 = tmp.lastIndexOf(c, pos - 1);

				if (pos2 == -1) {
					tmp = tmp.substring(0, pos);
				} else {
					tmp = tmp.substring(pos2 + 1, pos);
				}
			} else {
				tmp = tmp.substring(pos);
			}

			options.put(IWritersKeywords.WRITER_OUTPUT_DIR_SET, Boolean.TRUE);
			options.put(IWritersKeywords.WRITER_FS_PATH_OUTPUT_DIR, outputDir);
			options.put(IWritersKeywords.WRITER_WS_OUTPUT_projectbase_DIR, "..");
			options.put(IWritersKeywords.WRITER_LAST_OUTPUT_DIR, tmp);
		}

		// --------------- Load data ------------------

		final IVarTree vt = load();
		final ITreeInterface ti = vt.newTreeInterface();

		// Prepare to store all rtos
		ArrayList<String> rtosPath = new ArrayList<String>();
		{
			String[] tmp = Search.allRtos(ti);
			if (tmp == null) {
				myLog(LEFT, "No Ecu found");
				return;
			}
			rtosPath.addAll(Arrays.asList(tmp));
		}

		// myLog("ELENCO", ""+rtosPath);
		if (rtosPath.size() == 0) {
			myLog(LEFT, "No RT-OS found");
			return;
		}

		// --------------- Set nios' app_dir and project variables -----------

		vt.getProperties().put(IWritersKeywords.VTProperty_OilProjectName,
				"myProject");

		{
			String stmp = outputDir;
			// stmp = stmp.replace('\\','/');
			if (stmp.endsWith("/") || stmp.endsWith("\\")) {
				stmp = stmp.substring(0, stmp.length() - 1);
			}
			vt.getProperties().put(IWritersKeywords.VTProperty_OilProjectPath,
					stmp);

		}

		// --------------- Compute -------------------

		myLog("WORKING", "Prepare configuration's files");
		IOilWriterBuffer[] buffers = null;
		try {
			buffers = RtosFactory.INSTANCE.write(vt,
					(String[]) rtosPath.toArray(new String[rtosPath.size()]),
					options);
		} catch (OilCodeWriterException e) {
			RtdruidLog.showDebug(e);
			throw new OilWorkerException(e.getMessage());
		}
		myLog("      ", "Start to save");

		// ---------------- Store files -------------------
		save(buffers);
	}

	/**
	 * This method shows a message.
	 * 
	 * The output looks like :<br>
	 * [LEFT &nbsp;&nbsp;&nbsp;RIGTH]
	 * 
	 * @param left
	 *            what write in the left side
	 * @param rigth
	 *            what write in the rigth side
	 */
	protected void myLog(String left, String right) {
		final int LEFT_SIZE = 10;

		logger.log("["
				+ left
				+ ("                 ".substring(
						0,
						(left.length() > LEFT_SIZE - 1 ? 1 : LEFT_SIZE
								- left.length()))) + right + "]");
	}

}