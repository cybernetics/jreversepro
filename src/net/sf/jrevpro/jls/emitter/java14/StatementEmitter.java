/**
 *  @(#) StatementEmitter.java
 *
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
package net.sf.jrevpro.jls.emitter.java14;

import net.sf.jrevpro.ast.block.Block;
import net.sf.jrevpro.ast.block.Statement;
import net.sf.jrevpro.jls.JLSConstants;
import net.sf.jrevpro.jls.emitter.BlockEmitter;
import net.sf.jrevpro.jls.emitter.EmitterTarget;

/**
 * @author akkumar
 * 
 */
public class StatementEmitter extends BlockEmitter {

	@Override
	protected void emitBlockBeginCode(EmitterTarget target, Block _block) {

	}

	@Override
	protected void emitBlockEndCode(EmitterTarget target, Block _block) {

	}

	@Override
	protected void emitCurrentCode(EmitterTarget target, Block _block) {
		Statement stmt = (Statement) _block;
		target.append(stmt.getExpression().getJLSCode()
				+ JLSConstants.END_OF_STATEMENT);
		target.append("\n");
	}

}
