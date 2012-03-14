package com.eu.evidence.rtdruid.modules.oil.codewriter.erikaenterprise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.eu.evidence.rtdruid.desk.Messages;
import com.eu.evidence.rtdruid.internal.modules.oil.codewriter.erikaenterprise.ErikaEnterpriseWriter;
import com.eu.evidence.rtdruid.internal.modules.oil.exceptions.OilCodeWriterException;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.ISimpleGenResKeywords;
import com.eu.evidence.rtdruid.internal.modules.oil.keywords.IWritersKeywords;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilObjectList;
import com.eu.evidence.rtdruid.modules.oil.abstractions.IOilWriterBuffer;
import com.eu.evidence.rtdruid.modules.oil.abstractions.ISimpleGenRes;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.SectionWriter;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.comments.FileTypes;
import com.eu.evidence.rtdruid.modules.oil.codewriter.common.comments.ICommentWriter;
import com.eu.evidence.rtdruid.modules.oil.erikaenterprise.constants.IEEWriterKeywords;

public class SectionWriterIsr implements IEEWriterKeywords {
	
	protected Set<String> entries;
	protected final String hw_id;
	
	protected Boolean checked;
	
	protected final ErikaEnterpriseWriter parent;

	
	public SectionWriterIsr(ErikaEnterpriseWriter parent, String hw_id) {
		this.parent = parent;
		this.hw_id = hw_id;
		entries = null;
		checked = null;
	}

	public void setValidEntries(Set<String> entries) {
		if (entries == null) {
			this.entries = null;
		} else {
			this.entries = new LinkedHashSet<String>();
			this.entries.addAll(entries);
		}
	}

	public void setValidEntries(String[] entries) {
		if (entries == null) {
			this.entries = null;
		} else {
			this.entries = new LinkedHashSet<String>();
			this.entries.addAll(Arrays.asList(entries));
		}
	}
	
	protected boolean checkStaticIsrTable(int currentRtosId) {
		if (checked == null) {
			String[] existing = parent.extractEE_Opts(EE_OPT_USER_ONLY, currentRtosId);
			checked = Boolean.TRUE;
			for (String s: existing) {
				if ("EE_ISR_DYNAMIC_TABLE".equals(s)) {
					checked = Boolean.FALSE;
					break;
				}
			}
		}
		
		return checked.booleanValue();
	}


	
	public void writeIsr(int currentRtosId, IOilObjectList ool, IOilWriterBuffer oilWBuff) throws OilCodeWriterException {

		if (checkStaticIsrTable(currentRtosId)) {
			
			if (parent.checkKeyword(IWritersKeywords.KERNEL_MEMORY_PROTECTION)) {
				Messages.sendWarningNl("Static ISR Table is not tested to work with Memmory Protection");
			}
	
			List<ISimpleGenRes> isrList = ool.getList(IOilObjectList.ISR);
			if (isrList.size()>0) {
				final StringBuffer buffer = oilWBuff.get(FILE_EE_CFG_H);
				final ICommentWriter commentWriterH = SectionWriter.getCommentWriter(ool, FileTypes.H);
		
				buffer.append(commentWriterH.writerBanner("ISR definition")); // \n");
				for (ISimpleGenRes curr : isrList) {
					String category = curr.containsProperty(ISimpleGenResKeywords.ISR_CATEGORY) ? curr.getString(ISimpleGenResKeywords.ISR_CATEGORY) : "";
					
					if ("1".equals(category)) {
						// HW
						buffer.append(commentWriterH.writerSingleLineComment("Category 1: " + curr.getName()));
					} else if ("2".equals(category)) {
						String entry = curr.containsProperty(ISimpleGenResKeywords.ISR_ENTRY) ? curr.getString(ISimpleGenResKeywords.ISR_ENTRY) : "";
						String entry_id = "EE_"+hw_id+"_"+entry+"_ISR";
						String handler = curr.containsProperty(ISimpleGenResKeywords.ISR_HANDLER) ? curr.getString(ISimpleGenResKeywords.ISR_HANDLER) : curr.getName();
						String prio = curr.containsProperty(ISimpleGenResKeywords.ISR_PRIORITY) ? curr.getString(ISimpleGenResKeywords.ISR_PRIORITY) : "";
	
						if (entries != null) {
							if (!entries.contains(entry_id)) {
								throw new OilCodeWriterException("Found a not valid isr entry: " + entry_id + " (ISR " + curr.getName() + ")");
							}
						}


						ArrayList<String> tempStrings = new ArrayList<String>();
						tempStrings.add("#define " + entry_id + " " + handler+"\n");
						if (prio.length()>0) {
							tempStrings.add("#define " + entry_id+"_PRI" + " " + prio+"\n");
						}

						if (entry==null || entry.length()==0){
							Messages.sendWarningNl("Disabling static ISR define for " + curr.getName() +": missing the entry value.");
							for (int i=0; i<tempStrings.size(); i++) {
								tempStrings.set(i, 
										commentWriterH.writerSingleLineComment(tempStrings.get(i)));
							}
						}
						
						for (String s: tempStrings) {
							buffer.append(s);
						}
	
					} else if ("3".equals(category)) {
						// trap
						buffer.append(commentWriterH.writerSingleLineComment("Category 3: " + curr.getName()));
					} else {
						buffer.append(commentWriterH.writerSingleLineComment("Unknow ISR Category: " + curr.getName()));
					}
				}
			}
		}
	}
}
