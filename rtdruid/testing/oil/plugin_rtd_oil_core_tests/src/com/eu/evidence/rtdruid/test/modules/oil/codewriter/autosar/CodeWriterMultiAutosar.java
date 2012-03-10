package com.eu.evidence.rtdruid.test.modules.oil.codewriter.autosar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Test;

import com.eu.evidence.rtdruid.desk.RTDFactory;
import com.eu.evidence.rtdruid.internal.modules.oil.exceptions.OilCodeWriterException;
import com.eu.evidence.rtdruid.internal.modules.oil.reader.OilReader;
import com.eu.evidence.rtdruid.io.IRTDMultiFileImporter;
import com.eu.evidence.rtdruid.io.MultiSourceImporterFactory;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilWriterBuffer;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.CommonUtils;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.RtosFactory;
import com.eu.evidence.rtdruid.test.modules.oil.codewriter.AbstractCodeWriterTest;
import com.eu.evidence.rtdruid.vartree.ITreeInterface;
import com.eu.evidence.rtdruid.vartree.IVarTree;
import com.eu.evidence.rtdruid.vartree.data.ObjectWithID;
import com.eu.evidence.rtdruid.vartree.data.init.Vt2StringUtilities;

public class CodeWriterMultiAutosar extends AbstractCodeWriterTest {

	@Test
	public void test() throws IOException {

		String text = "CPU system {\n" +
				"	OS EE {\n" +
				"		EE_OPT = \"DEBUG\";\n" +
				"		EE_OPT = \"__USE_BUTTONS__\";\n" +
				"		EE_OPT = \"__E200ZX_EXECUTE_FROM_RAM__\";\n" +
				"		EE_OPT = \"__STATIC_ISR_TABLE__\";\n" +
				"		CFLAGS = \"-tPPC5554EF:simple\";\n" +
				"		CFLAGS = \"-I.. -I../include -DUSE_QUOTE_INCLUDES\";\n" +
				"		LDFLAGS = \"-tPPC5554EF:simple -lc -lm -m6\";\n" +
				"		ASFLAGS = \"\";\n" +
				"		CPU_DATA = PPCE200ZX {\n" +
				"			MODEL = E200Z7;\n" +
				"			APP_SRC = \"./Autosar_Stubs.c\";\n" +
				"			APP_SRC = \"./bsw_init.c\";\n" +
				"			APP_SRC = \"./main.c\";\n" +
				"			APP_SRC = \"./src/Appl_Dcm.c\";\n" +
				"			APP_SRC = \"./src/BswM.c\";\n" +
				"			APP_SRC = \"./src/BswM_LCfg.c\";\n" +
				"			APP_SRC = \"./src/Can.c\";\n" +
				"			APP_SRC = \"./src/CanIf.c\";\n" +
				"			APP_SRC = \"./src/CanIf_Lcfg.c\";\n" +
				"			APP_SRC = \"./src/CanIf_PBcfg.c\";\n" +
				"			APP_SRC = \"./src/Can_Irq.c\";\n" +
				"			APP_SRC = \"./src/Can_Lcfg.c\";\n" +
				"			APP_SRC = \"./src/Can_PBcfg.c\";\n" +
				"			APP_SRC = \"./src/CanSM.c\";\n" +
				"			APP_SRC = \"./src/CanSM_Lcfg.c\";\n" +
				"			APP_SRC = \"./src/CanSM_PBcfg.c\";\n" +
				"			APP_SRC = \"./src/CanTp.c\";\n" +
				"			MULTI_STACK = TRUE {\n" +
				"				IRQ_STACK = TRUE {\n" +
				"					SYS_SIZE = 128;\n" +
				"				};\n" +
				"			};\n" +
				"		};\n" +
				"		MCU_DATA = PPCE200ZX {\n" +
				"			MODEL = MPC5674F;\n" +
				"		};\n" +
				"		\n" +
				"		KERNEL_TYPE = ECC1;\n" +
				"	};\n" +
				"};\n";
		
		File dir = new File("D:\\work\\autosar_test");
		File[] files = dir.listFiles();
		ArrayList<String> types = new ArrayList<String>();
		ArrayList<InputStream> inputs = new ArrayList<InputStream>();
		for (File f: files) {
			if (f.getName().endsWith(".arxml")) {
				types.add("arxml");
				inputs.add(new FileInputStream(f));
			}
		}
		
		IRTDMultiFileImporter importer = MultiSourceImporterFactory.getFactory().getImporterFor(types.toArray(new String[types.size()]));
		assertNotNull(importer);
		IVarTree vt = RTDFactory.newVarTree();
		ObjectWithID root = importer.load((InputStream[]) inputs.toArray(new InputStream[inputs.size()]), types.toArray(new String[types.size()]), null);
		assertNotNull(root);
		vt.setRoot(root);
		
		System.out.println("\t------------OIL------------\n\n");
		System.out.println(Vt2StringUtilities.writeString(vt, "oil"));
		System.out.println("\t------------FINE OIL------------\n\n");
		
		
		(new OilReader()).load(new ByteArrayInputStream(text.getBytes()), vt);

		System.out.println("\t------------ERTD------------\n\n");
		System.out.println(Vt2StringUtilities.explodeOilVar(Vt2StringUtilities.writeString(vt, "ertd")));
		System.out.println("\t------------FINE ERTD------------\n\n");

		System.out.println("\t------------ALTRO OIL------------\n\n");
		String OIL = Vt2StringUtilities.writeString(vt, "oil");
		System.out.println(OIL);
		System.out.println("\t------------FINE ALTRO OIL------------\n\n");

		
		// -------------- search rtos ----------------
		ITreeInterface ti = vt.newTreeInterface();

		String[] prefix = CommonUtils.getAllRtos(ti);
		assertEquals(1, prefix.length);

		// --------------- write -----------------

		IOilWriterBuffer[] buffers = null;
		try {
			buffers = RtosFactory.INSTANCE.write(vt, prefix);
		} catch (OilCodeWriterException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException("Write fail: " + e.getMessage(), e);
		}

		final int expectedSize = 1;
		assertNotNull(buffers);
		assertEquals(expectedSize, buffers.length);

	    DefaultTestResult result = commonWriterTest(OIL, 1);
		for (int i=0; i<expectedSize; i++) {
			assertEquals(buffers[i].toString(), result.buffers[i].toString());
		}
	}
		
}
