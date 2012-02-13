package com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise;

import java.util.LinkedHashMap;

import com.eu.evidence.rtdruid.modules.oil.erikaenterprise.interfaces.IMacrosForSharedData;

public class MacrosForSharedData implements IMacrosForSharedData {
	
	private class SharedDataWithPragma implements IPragma {
		
		private LinkedHashMap<String, StringBuffer> pragmaBuffers = new LinkedHashMap<String, StringBuffer>();
		
		@Override
		public String getPragmaSections() {
			StringBuffer answer = new StringBuffer();
			for (StringBuffer buff: pragmaBuffers.values()) {
				answer.append(buff);
				answer.append("\n\n");
			}
			return answer.toString();
		}
		
		@Override
		public IPragma getPragma() {
			return new SharedDataWithPragma();
		}
		
		private void addElement(String section, String section_name, String name) {
	    	StringBuffer buff;
	    	String sep;
	    	if (pragmaBuffers.containsKey(section)) {
	    		buff = pragmaBuffers.get(section);
	    		sep = ", ";
	    	} else {
	    		buff = new StringBuffer();
	    		pragmaBuffers.put(section, buff);
	    		buff.append("#pragma section "+section.toUpperCase()+" \"."+section_name+"\" \"."+section_name+"\"\n" +
	    					"#pragma use_section "+section.toUpperCase());
	    		sep = " ";
	    	}
	    	
	    	buff.append(sep + name);

		}
		
		@Override
		public String vectorRam(String vectorName, String array) {
			addElement("EE_SHARED_IDATA", "mcglobald", vectorName);
			return vectorName+array;
		}

		@Override
		public String vectorRom(String vectorName, String array) {
			addElement("EE_SHARED_IDATA", "mcglobald", vectorName);
			return vectorName+array;
		}

		@Override
		public String constVectorRam(String vectorName, String array) {
			addElement("EE_SHARED_CDATA", "mcglobalc", vectorName);
			return vectorName+array;
		}

		@Override
		public String constVectorRom(String vectorName, String array) {
			addElement("EE_SHARED_CDATA", "mcglobalc", vectorName);
			return vectorName+array;
		}

		@Override
		public String valueRam(String valueName) {
			addElement("EE_SHARED_IDATA", "mcglobald", valueName);
			return valueName;
		}

		@Override
		public String valueRom(String valueName) {
			addElement("EE_SHARED_IDATA", "mcglobald", valueName);
			return valueName;
		}

		@Override
		public String constValueRam(String valueName) {
			addElement("EE_SHARED_CDATA", "mcglobalc", valueName);
			return valueName;
		}

		@Override
		public String constValueRom(String valueName) {
			addElement("EE_SHARED_CDATA", "mcglobalc", valueName);
			return valueName;
		}

	}
	
	@Override
	public IPragma getPragma() {
		return new SharedDataWithPragma();
	}
	
	@Override
	public String vectorRam(String vectorName, String array) {
		return "EE_SHARED_IDATA "+vectorName+array;
	}

	@Override
	public String vectorRom(String vectorName, String array) {
		return "EE_SHARED_IDATA "+vectorName+array;
	}

	@Override
	public String constVectorRam(String vectorName, String array) {
		return "EE_SHARED_CDATA "+vectorName+array;
	}

	@Override
	public String constVectorRom(String vectorName, String array) {
		return "EE_SHARED_CDATA "+vectorName+array;
	}

	@Override
	public String valueRam(String valueName) {
		return "EE_SHARED_IDATA "+valueName;
	}

	@Override
	public String valueRom(String valueName) {
		return "EE_SHARED_IDATA "+valueName;
	}

	@Override
	public String constValueRam(String valueName) {
		return "EE_SHARED_CDATA "+valueName;
	}

	@Override
	public String constValueRom(String valueName) {
		return "EE_SHARED_CDATA "+valueName;
	}
	
}
