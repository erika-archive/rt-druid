package com.eu.evidence.rtdruid.test.modules.oil.codewriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.w3c.dom.Document;

import com.eu.evidence.rtdruid.desk.RTDFactory;
import com.eu.evidence.rtdruid.internal.modules.oil.exceptions.OilCodeWriterException;
import com.eu.evidence.rtdruid.internal.modules.oil.reader.OilReader;
import com.eu.evidence.rtdruid.io.MultiSourceImporterFactory;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilWriterBuffer;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.CommonUtils;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.RtosFactory;
import com.eu.evidence.rtdruid.vartree.ITreeInterface;
import com.eu.evidence.rtdruid.vartree.IVarTree;
import com.eu.evidence.rtdruid.vartree.data.ObjectWithID;
import com.eu.evidence.rtdruid.vartree.data.init.IVTResource;
import com.eu.evidence.rtdruid.vartree.data.init.RTD_XMI_Factory;

/**
 * This class can be use to simplify and standardize a Code Writer test
 * 
 * @author Nicola Serreli
 *
 */
public class AbstractCodeWriterTest extends TestCase {
	protected String basePath = "e:/tests_161/";

	/**
	 * A small class used to retur the IVarTree and computed Buffers
	 * 
	 * @author Nicola Serreli
	 */
	protected class DefaultTestResult {
		public final IVarTree vt;
		public final IOilWriterBuffer[] buffers;
		public DefaultTestResult(IVarTree vt, IOilWriterBuffer[] buffers) {
			this.vt = vt;
			this.buffers = buffers;
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("\n\n************\n TEST " + getClass().getName() + " - " + getName() + "\n************\n\n");
	}

	/**
	 * This is a standard method that can be used to read an oil file and write
	 * the configuration on standard output
	 * 
	 * @param oil_text
	 *            the oil configuration
	 * @param expected_cpu
	 *            the expected number of cpu
	 *            
	 * @return both the loaded IVarTree and computed Buffers
	 */
	public DefaultTestResult commonWriterTest(String oil_text, int expected_cpu) {
		IVarTree vt = (IVarTree) RTDFactory.get(IVarTree.class);
		(new OilReader()).load(new ByteArrayInputStream(oil_text.getBytes()), vt);

		// -------------- search rtos ----------------
		ITreeInterface ti = vt.newTreeInterface();

		String[] prefix = CommonUtils.getAllRtos(ti);
		//assertEquals(expected_cpu, prefix.length);

		// --------------- write -----------------

		IOilWriterBuffer[] buffers = null;
		try {
			buffers = RtosFactory.INSTANCE.write(vt, prefix);
		} catch (OilCodeWriterException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException("Write fail: " + e.getMessage(), e);
		}

		assertTrue(buffers != null);
		assertEquals(expected_cpu, buffers.length);
		for (int i=0; i<expected_cpu; i++)
			System.out.println("buff " + i + ":\n" + (buffers[i]).toString());
		writeTestResultToFile(buffers);
		
		return new DefaultTestResult(vt, buffers);
	}

	/**
	 * This is a standard method that can be used to read an oil file and write
	 * the configuration on standard output
	 * 
	 * @param oil_text
	 *            the oil configuration
	 * @param expected_cpu
	 *            the expected number of cpu
	 *            
	 * @return both the loaded IVarTree and computed Buffers
	 * @throws IOException 
	 */
	public DefaultTestResult commonWriterTest(String[] input_text, String[] input_type, int expected_cpu) throws IOException {
		IVarTree vt = (IVarTree) RTDFactory.get(IVarTree.class);
		assertEquals(input_text.length, input_type.length);
		
		{ //  ----------- LOAD ----------- 

			// prepare
			ArrayList<MultiSourceImporterFactory.LoadHelper> helpers = new ArrayList<MultiSourceImporterFactory.LoadHelper>();
			Set<String> simpleLoad = new HashSet<String>();
			for (int i=0; i<input_type.length; i++) {

				InputStream input = new ByteArrayInputStream(input_text[i].getBytes());

				boolean found = false;
				// check already existing helpers
				for (MultiSourceImporterFactory.LoadHelper helper : helpers) {
					if (helper.add(input_type[i], input)) {
						found = true;
						break;
					}
				}

				if (!found) {
					// check a new helper
					MultiSourceImporterFactory.LoadHelper helper = new MultiSourceImporterFactory.LoadHelper();
					if (helper.add(input_type[i], input)) {
						found = true;
						helpers.add(helper);
					}
				}

				if (!found) {
					// ok. it should be handled as single file
					simpleLoad.add(input_text[i]);
				}
			}

			// load
			for (MultiSourceImporterFactory.LoadHelper helper : helpers) {

				com.eu.evidence.rtdruid.vartree.data.System root = helper.load();
				mergeInput(vt, root);
			}
			
			// load
			for (int i=0; i<input_type.length; i++) {
				if (simpleLoad.contains(input_text[i])) {

					// load and parse the input file
					IVTResource res = (IVTResource) new RTD_XMI_Factory()
							.createResource(URI.createFileURI(input_type[i]));
					res.load(new ByteArrayInputStream(input_text[i].getBytes()), null);
					mergeInput(vt, res);
				}
			}
		}
		
		// -------------- search rtos ----------------
		ITreeInterface ti = vt.newTreeInterface();

		String[] prefix = CommonUtils.getAllRtos(ti);
//		assertTrue(prefix.length == expected_cpu);

		// --------------- write -----------------

		IOilWriterBuffer[] buffers = null;
		try {
			buffers = RtosFactory.INSTANCE.write(vt, prefix);
		} catch (OilCodeWriterException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException("Write fail: " + e.getMessage(), e);
		}

		assertTrue(buffers != null);
		for (int i=0; i<buffers.length; i++)
			System.out.println("buff " + i + ":\n" + (buffers[i]).toString());

		writeTestResultToFile(buffers);
		assertEquals(expected_cpu, buffers.length);

		return new DefaultTestResult(vt, buffers);
	}
	
	

	protected void mergeInput(IVarTree vt, IVTResource res) {
		EList<EObject> objList = res.getContents();
		if (objList.size() > 0) {
			IVarTree vtt = RTDFactory.newVarTree();
			vtt.setRoot(res);
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
	}
	
    public static String oilToXmltext(InputStream input) {
        Document xml = (new OilReader()).loadAsXml(input, null, null);
        return xmlToText(xml);
    }

    public static String xmlToText(Document doc) {
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// Transform the source XML
			transformer.transform(new DOMSource(doc), new StreamResult(output));

		} catch (TransformerConfigurationException tce) {
			// Error generated by the parser
			System.out.println("* Transformer Factory error");
			System.out.println("  " + tce.getMessage());

			// Use the contained exception, if any
			Throwable x = tce;
			if (tce.getException() != null)
				x = tce.getException();
			x.printStackTrace();

		} catch (TransformerException te) {
			// Error generated by the parser
			System.out.println("* Transformation error");
			System.out.println("  " + te.getMessage());

			// Use the contained exception, if any
			Throwable x = te;
			if (te.getException() != null)
				x = te.getException();
			x.printStackTrace();

		}

		return output.toString();
    }


    /**
     * This method writes all provided test results to a file corresponding to current test 
     * 
     * @param buffers
     */
    protected void writeTestResultToFile(IOilWriterBuffer[] buffers) {
    	String path = basePath + (basePath.length()>0 && !basePath.endsWith(File.separator) ? File.separator : "")+getClass().getName() + "-" + getName();
    	writeTestResultToFile(buffers, path);
    }
    /**
     * This method writes all provided test results to a generic file 
     * 
     * @param buffers
     */
    protected void writeTestResultToFile(IOilWriterBuffer[] buffers, String filePath) {
		try {
	    	File f = new File(filePath);
	    	FileOutputStream out = new FileOutputStream(f);
	    	write(buffers, out);
	    	out.flush();
	    	out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
	
	/**
	 * Write all buffers to standard output or to the given stream
	 * 
	 * @param buffers
	 */
	public void write(IOilWriterBuffer[] buffers, OutputStream out) {
		try {
			for (int i=0; i<buffers.length; i++) {
				out.write(("buff " + i + ":\n").getBytes());
				out.write((buffers[i]).toString().getBytes());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
