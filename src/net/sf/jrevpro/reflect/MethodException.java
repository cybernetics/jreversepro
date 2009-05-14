/**
 * JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2008 Karthik Kumar.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0 
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***/
package net.sf.jrevpro.reflect;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;

import net.sf.jrevpro.jvm.JVMConstants;

/**
 * <b>MethodException</b> is an abstraction of the exception table , that
 * represents the list of exceptions (represented by a table) thrown by the method.
 * 
 * @author Karthik Kumar.
 */
public class MethodException {

	/**
	 * Constructor.
	 * 
	 * @param rhsStart
	 *            StartPc
	 * @param rhsEnd
	 *            EndPc
	 * @param rhsHandler
	 *            HandlerPc of the first handler block for the above mentioned
	 *            code block.
	 * @param rhsType
	 *            Handler data type.
	 */
	public MethodException(int rhsStart, int rhsEnd, int rhsHandler,
			String rhsType) {
		startPc = rhsStart;
		endPc = rhsEnd;
		any = (rhsType.equals(JVMConstants.ANY));

		if (rhsHandler - rhsEnd == 1) {
			endPc = rhsHandler;
		}
		/**
		 * This minor adjustment of endPc with handler pc is necessary since in
		 * some cases the compiled code generated by javac and jikes are
		 * different. Jikes generates code such that the endPc of try . block is
		 * the beginning of the first handler beginning pc. Javac generates such
		 * that the endPc of try..block is 1 less than the one where the handler
		 * block begins.
		 * 
		 * Bug with javac/jikes/jreversepro ??? Any clues ??
		 */

		excCatchTable = new HashMap<Integer, String>();
		addCatchBlock(rhsHandler, rhsType);
	}

	/**
	 * Adds a new catch block to the code block { startpc, endpc }
	 * 
	 * @param rhsHandlerPc
	 *            Handler Pc
	 * @param rhsType
	 *            Handler data type.
	 */
	public void addCatchBlock(int rhsHandlerPc, String rhsType) {
		rhsType = (rhsType != null) ? rhsType : JVMConstants.ANY;
		excCatchTable.put(new Integer(rhsHandlerPc), rhsType);
	}

	/**
	 * @return Returns startpc of this code block.
	 */
	public int getStartPc() {
		return startPc;
	}

	/**
	 * @return Returns endpc of this code block.
	 */
	public int getEndPc() {
		return endPc;
	}

	/**
	 * @return Returns the Enumeration of handler types.
	 */
	public Enumeration<Map.Entry<Integer, String>> getHandlers() {
		return Collections.enumeration(excCatchTable.entrySet());
	}

	/**
	 * Given a pc, if an exceptiontable entry exists such that the the handler
	 * begins with this pc, then the handler type is returned. Else this returns
	 * null.
	 * 
	 * @param rhsHandlerPc
	 *            HandlerPc for which type is queried.
	 * @return Handler type of the exception handler, if one exists beginning
	 *         with rhsHandlerPc. null, otherwise.
	 */
	public String getExceptionClass(int rhsHandlerPc) {
		return excCatchTable.get(new Integer(rhsHandlerPc));
	}

	/**
	 * @param obj
	 *            Object to be compared with.
	 * @return if two JException objects are equal. false, otherwise.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof MethodException)) {
			return false;
		} else {
			return sameTryBlock((MethodException) obj);
		}
	}

	/**
	 * Checks if the new exception block passed as parameter has the same code
	 * block { startpc, endpc } as the current one.
	 * 
	 * @param exc
	 *            New Exception Block
	 * @return Returns true if the code blocks are same for both of them. false,
	 *         otherwise.
	 */
	public boolean sameTryBlock(MethodException exc) {
		return (startPc == exc.startPc && endPc == exc.endPc);
	}

	/**
	 * @return true. if at least one of the exception handlers is for ANY block.
	 *         false, otherwise.
	 */
	public boolean containsANYCatchBlock() {
		return excCatchTable.containsValue(JVMConstants.ANY);
	}

	/**
	 * @return Value of Any
	 */
	public boolean isAny() {
		return any;
	}

	/**
	 * @return Stringified form of the class.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		Enumeration<Integer> enum1 = Collections.enumeration(excCatchTable
				.keySet());
		Enumeration<String> enum2 = Collections.enumeration(excCatchTable
				.values());

		while (enum1.hasMoreElements()) {
			sb.append("\t\t" + startPc + "\t" + endPc);
			sb.append("\t" + enum1.nextElement());
			sb.append(" " + enum2.nextElement() + "\n");
		}
		return sb.toString();
	}

	/**
	 * Start Pc of the exception handler.
	 */
	private int startPc;

	/**
	 * End Pc of the exception handler
	 */
	private int endPc;

	/**
	 * Set if the exception handler is for ANY data type for the block - {
	 * startpc, endpc }
	 */
	private boolean any;

	/**
	 * Map - Key - Handler Pc beginning value -Handler datatype.
	 */
	Map<Integer, String> excCatchTable;

}
