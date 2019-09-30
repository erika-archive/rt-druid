package com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise;

import com.eu.evidence.rtdruid.internal.modules.oil.keywords.IWritersKeywords;
import com.eu.evidence.rtdruid.modules.oil.erikaenterprise.interfaces.IMacrosForSharedData;

public class MacrosForMPC5777C_SharedData implements IMacrosForSharedData {

	final String indent1 = IWritersKeywords.INDENT;

	private class SharedDataWithPragma implements IPragma {
		
//		private LinkedHashMap<String, StringBuffer> pragmaBuffers = new LinkedHashMap<String, StringBuffer>();
		
		@Override
		public String getPragmaSections() {
			StringBuffer answer = new StringBuffer();
//			for (StringBuffer buff: pragmaBuffers.values()) {
//				answer.append(buff);
//				answer.append("\n\n");
//			}
			return answer.toString();
		}
		
		@Override
		public IPragma getPragma() {
			return new SharedDataWithPragma();
		}
		
		private void addElement(String section, String section_name, String name) {
//	    	StringBuffer buff;
//	    	String sep;
//	    	if (pragmaBuffers.containsKey(section)) {
//	    		buff = pragmaBuffers.get(section);
//	    		sep = ", ";
//	    	} else {
//	    		buff = new StringBuffer();
//	    		pragmaBuffers.put(section, buff);
//	    		buff.append("#pragma section "+section.toUpperCase()+" \"."+section_name+"\" \"."+section_name+"\"\n" +
//	    					"#pragma use_section "+section.toUpperCase());
//	    		sep = " ";
//	    	}
//	    	
//	    	buff.append(sep + name);

		}
		
		
		@Override
		public String vectorRamUnitialized(String type, String vectorName, String array, String body) {
			return vectorRam(type, vectorName, array, body);
		}

		
		@Override
		public String vectorRam(String type, String vectorName, String array, String body) {
//			addElement("EE_SHARED_IDATA", "mcglobald", vectorName);
//			return type + vectorName+array +body;
			return type +vectorName+array+body;
		}

		@Override
		public String vectorRom(String type, String vectorName, String array, String body) {
//			addElement("EE_SHARED_IDATA", "mcglobald", vectorName);
//			return type + vectorName+array+body;
			return type +vectorName+array+body;
		}

		@Override
		public String constVectorRam(String type, String vectorName, String array, String body) {
			//addElement("EE_SHARED_CDATA", "mcglobalc", vectorName);
			//return vectorName+array;
			return type + vectorName+array+body;
		}

		@Override
		public String constVectorRom(String type, String vectorName, String array, String body) {
//			addElement("EE_SHARED_CDATA", "mcglobalc", vectorName);
//			return vectorName+array;
			return type +vectorName+array+body;
		}

		@Override
		public String valueRamUnitialized(String type, String valueName, String body) {
			return valueRam(type, valueName, body);
		}

		@Override
		public String valueRam(String type, String valueName, String body) {
//			addElement("EE_SHARED_IDATA", "mcglobald", valueName);
//			return type + valueName+body;
			return type +valueName+body;
		}

		@Override
		public String valueRom(String type, String valueName, String body) {
//			addElement("EE_SHARED_IDATA", "mcglobald", valueName);
//			return type + valueName+body;
			return type +valueName+body;
		}

		@Override
		public String constValueRam(String type, String valueName, String body) {
//			addElement("EE_SHARED_CDATA", "mcglobalc", valueName);
//			return type + valueName+body;
			return type +valueName+body;
		}

		@Override
		public String constValueRom(String type, String valueName, String body) {
//			addElement("EE_SHARED_CDATA", "mcglobalc", valueName);
//			return type + valueName+body;
			return type +valueName+body;
		}

		@Override
		public String getHeader() {
			return 
					"\n#if defined(EE_CURRENTCPU) && (EE_CURRENTCPU == 0)\n" +
					"#pragma section EE_DATA_BEGIN\n" +
					"#pragma section EE_SDATA_MASTER_BEGIN\n" +
					"#pragma section EE_CONST_BEGIN\n" +
					"#pragma section EE_SCONST_MASTER_BEGIN\n" +
					"#else\n" +
					"#pragma section EE_DATA_BEGIN\n" +
					"#pragma section EE_SDATA_SLAVE_BEGIN\n" +
					"#pragma section EE_CONST_BEGIN\n" +
					"#pragma section EE_SCONST_SLAVE_BEGIN\n" +
					"#endif\n\n";
		}

		@Override
		public String getFooter() {
			return 
					"\n\n#pragma section EE_DATA_END\n" +
					"#pragma section EE_SDATA_END\n" +
					"#pragma section EE_CONST_END\n" +
					"#pragma section EE_SCONST_END\n";
		}

	}
	
	@Override
	public IPragma getPragma() {
		return new SharedDataWithPragma();
	}
	
	@Override
	public String vectorRamUnitialized(String type, String vectorName, String array, String body) {
		return vectorRam(type, vectorName, array, body);
	}
	
	@Override
	public String vectorRam(String type, String vectorName, String array, String body) {
		return type + "EE_SHARED_IDATA "+vectorName+array + body;
	}

	@Override
	public String vectorRom(String type, String vectorName, String array, String body) {
		return type + "EE_SHARED_IDATA "+vectorName+array + body;
	}

	@Override
	public String constVectorRam(String type, String vectorName, String array, String body) {
		return type + "EE_SHARED_CDATA "+vectorName+array + body;
	}

	@Override
	public String constVectorRom(String type, String vectorName, String array, String body) {
		return type + "EE_SHARED_CDATA "+vectorName+array + body;
	}

	@Override
	public String valueRamUnitialized(String type, String valueName, String body) {
		return valueRam(type, valueName, body);
	}
	
	@Override
	public String valueRam(String type, String valueName, String body) {
		return type + "EE_SHARED_IDATA "+valueName + body;
	}

	@Override
	public String valueRom(String type, String valueName, String body) {
		return type + "EE_SHARED_IDATA "+valueName + body;
	}

	@Override
	public String constValueRam(String type, String valueName, String body) {
		return type + "EE_SHARED_CDATA "+valueName + body;
	}

	@Override
	public String constValueRom(String type, String valueName, String body) {
		return type + "EE_SHARED_CDATA "+valueName + body;
	}

	@Override
	public String getHeader() {
		return "";
	}

	@Override
	public String getFooter() {
		return "";
	}
	
}
