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
package net.sf.jrevpro.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.MetalLookAndFeel;

import net.sf.jrevpro.JReverseProContext;
import net.sf.jrevpro.JReverseProContext.OutputType;
import net.sf.jrevpro.jls.JLSConstants;
import net.sf.jrevpro.jvm.JVMConstants;
import net.sf.jrevpro.jvm.TypeInferrer;
import net.sf.jrevpro.parser.ClassParserException;
import net.sf.jrevpro.reflect.ClassInfo;
import net.sf.jrevpro.reflect.Field;
import net.sf.jrevpro.reflect.Import;
import net.sf.jrevpro.reflect.Method;

/**
 * Entry point for swing-based GUI
 * 
 * @author Karthik Kumar
 */
@SuppressWarnings("serial")
public class GUIMain extends JFrame implements ActionListener, WindowListener,
		GuiConstants {

	/**
	 * No-argument constructor.
	 */
	public GUIMain(JReverseProContext _context) {
		super(TITLE);

		mPropertyFile = System.getProperty("user.home")
				+ System.getProperty("file.separator") + PROP_FILE;

		pnlEditor = new ClassEditPanel();
		mPnlStatusBar = new StatusPanel();

		pnlEditor.setPreferredSize(new Dimension(500, 200));
		mPnlStatusBar.setPreferredSize(new Dimension(500, 20));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pnlEditor, BorderLayout.CENTER);
		getContentPane().add(mPnlStatusBar, BorderLayout.SOUTH);

		mMbrGen = new MainMenu(this);
		setJMenuBar(mMbrGen);

		mCurDir = CURRENT_DIRECTORY;

		mMbrGen.OnViewCPool.setEnabled(false);

		context = _context;
		initAppState();
		addListeners();
	}

	/**
	 * Method containing handlers for events generated by MenuItems.
	 * 
	 * @param aEvent
	 *            Event generated by GUI.
	 */
	public void actionPerformed(ActionEvent aEvent) {
		if (aEvent.getSource() == mMbrGen.OnFileOpen) {
			openFile();
		} else if (aEvent.getSource() == mMbrGen.OnFileSave) {
			saveFile();
		} else if (aEvent.getSource() == mMbrGen.OnFileExit) {
			appClose();
		} else if (aEvent.getSource() == mMbrGen.OnViewCPool) {
			viewPool();
		} else if (aEvent.getSource() == mMbrGen.OnOptFont) {
			showFontDialog();
		} else if (aEvent.getSource() == mMbrGen.OnHelpAbout) {
			showAbout();
		} else if (aEvent.getSource() == mMbrGen.OnEditCut) {
			cutText();
		} else if (aEvent.getSource() == mMbrGen.OnEditCopy) {
			copyText();
		}
	}

	/**
	 * Method to open a file.
	 */
	public synchronized void openFile() {
		CustomFileChooser chooser = new CustomFileChooser(mCurDir,
				"Class Files", ".class", "Open File");
		if (chooser.showChooser(this, "Decompile File") == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			mCurDir = f.getAbsolutePath();
			try {
				reverseEngineer(f);
				formatTitle(f.getAbsolutePath());
			} catch (Exception _ex) {
				(new DlgError(this, f.toString(), _ex)).setVisible(true);
			}
		}
	}

	/**
	 * Method to reverse engineer a file.
	 * 
	 * @param aFile
	 *            Class file to be reverse engineered.
	 * @throws ClassParserException
	 *             Thrown if class file not in proper format.
	 * @throws IOException
	 *             Thrown if error occured in reading class file.
	 * @throws RevEngineException
	 *             Thrown if error occured in reverse engineering file.
	 */
	private synchronized void reverseEngineer(File aFile)
			throws ClassParserException, IOException {
		mClassInfo = context.loadResource(aFile.getAbsolutePath());

		// Extract code.
		// TODO: Select Disassembler / Decompiler based on a variable.
		String code = context.print(OutputType.DECOMPILER, mClassInfo);

		// Output the code
		pnlEditor.writeCode(code);
		mMbrGen.OnViewCPool.setEnabled(true);
		createTree(mClassInfo, mCurrentClass);
	}

	/**
	 * Method invoked while saving to a file.
	 */
	public void saveFile() {
		CustomFileChooser chooser = new CustomFileChooser(mCurDir,
				"Java Source Files", ".java", "Save File");
		if (chooser.showChooser(this, "Save File") == JFileChooser.APPROVE_OPTION) {
			pnlEditor.writeToFile(this, chooser.getSelectedFile());
		}
	}

	/**
	 * Method invoked while closing a file.
	 */
	public void appClose() {
		if (ConfirmCloseDialog.confirmExit(this)) {
			saveProperties();
			System.exit(0);
		}
	}

	/**
	 * Method invoked while text is cut.
	 */
	public void cutText() {
		// System.out.println("Text Cut");
	}

	/**
	 * Method invoked while text is copied.
	 */
	public void copyText() {
		// System.out.println("Text Copied");
	}

	/**
	 * Method invoked while ConstantPool is viewed.
	 */
	public void viewPool() {
		DlgConstantPool dlgPool = new DlgConstantPool(this, mCurrentClass,
				mClassInfo.getConstantPool());
		dlgPool.setVisible(true);
		dlgPool = null;
	}

	/**
	 * Method invoked while System Font is being viewed.
	 */
	public void showFontDialog() {
		DlgFont dlg = new DlgFont(this, "System Fonts");
		if (dlg.showFontDialog() == DlgFont.SELECTED) {
			pnlEditor.setEditorFont(dlg.getChosenFont());
		}
	}

	/**
	 * Method invoked to show the About dialog box.
	 */
	public void showAbout() {
		new DlgAbout(this, "About");
	}

	/**
	 * Method invoked to initialize the GUI parameters.
	 */
	private void initAppState() {
		// Set Default Theme.
		MetalLookAndFeel.setCurrentTheme(new MyFavTheme());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		try {
			Properties pp = new Properties();
			FileInputStream fis = new FileInputStream(mPropertyFile);
			pp.load(fis);
			fis.close();
			int x = Integer.parseInt(pp.getProperty(XPOS));
			int y = Integer.parseInt(pp.getProperty(YPOS));
			int width = Integer.parseInt(pp.getProperty(XSIZE));
			int height = Integer.parseInt(pp.getProperty(YSIZE));
			mMbrGen.OnLookFeel.setAppLookAndFeel(pp.getProperty(L_AND_F));

			mMbrGen.setFlag(pp.getProperty(DECOMPILE_FLAG));
			pnlEditor.setEditorFont(new Font(pp.getProperty(FONT), Font.PLAIN,
					DlgFont.OPTIMUM_SIZE));

			setLocation(x, y);
			setSize(width, height);
		} catch (FileNotFoundException fnfe) {
			setLocation(0, 0);
			setSize(800, 550);
			pnlEditor.setEditorFont(new Font(ClassEditPanel.DEFAULT_FONT,
					Font.PLAIN, DlgFont.OPTIMUM_SIZE));
			mMbrGen.OnLookFeel.setDefaultLookAndFeel();
			System.err.println("Failed to load property file");
		} catch (IOException ioe) {
			System.err.println("Exception while closing a property file ");
		}
	}

	/**
	 * Method to add property listeners to MenuItems and the MainFrame.
	 */
	private void addListeners() {
		mMbrGen.OnFileOpen.addActionListener(this);
		mMbrGen.OnFileSave.addActionListener(this);
		mMbrGen.OnFileExit.addActionListener(this);

		mMbrGen.OnEditCut.addActionListener(this);
		mMbrGen.OnEditCopy.addActionListener(this);
		mMbrGen.OnViewCPool.addActionListener(this);
		mMbrGen.OnOptFont.addActionListener(this);

		mMbrGen.OnHelpAbout.addActionListener(this);
		addWindowListener(this);
	}

	/**
	 * Formats the title from the string Rhs and sets the title of the Frame.
	 * 
	 * @param aFileName
	 *            Full Path name to the class being reverse engineered.
	 * 
	 */
	private void formatTitle(String aFileName) {
		int dotIndex = aFileName.indexOf(".");
		if (dotIndex != -1) {
			String className = aFileName.substring(0, dotIndex);
			setTitle(TITLE + " - " + className);
		}
	}

	/**
	 * Save the state of the GUI as a properties file.
	 * 
	 */
	private void saveProperties() {
		try {
			Properties pp = new Properties();
			pp.setProperty(DECOMPILE_FLAG, new Boolean(mMbrGen.OnDecompiler
					.isSelected()).toString());
			pp.setProperty(XPOS, new Integer(getLocation().x).toString());
			pp.setProperty(YPOS, new Integer(getLocation().y).toString());
			pp.setProperty(XSIZE, new Integer(getSize().width).toString());
			pp.setProperty(YSIZE, new Integer(getSize().height).toString());
			pp.setProperty(L_AND_F, mMbrGen.OnLookFeel.getAppLookAndFeel());
			pp.setProperty(FONT, pnlEditor.getEditorFont().getFamily());

			FileOutputStream fos = new FileOutputStream(mPropertyFile);
			pp.store(fos, PROP_HEADING);
			fos.close();
		} catch (Exception _ex) {
			System.err.println("Failed to write Properties" + mPropertyFile);
			System.err.println(_ex);
		}
	}

	/**
	 * WindowClosing event handler.
	 * 
	 * @param aEvent
	 *            Event generated.
	 */
	public void windowClosing(WindowEvent aEvent) {
		appClose();
	}

	/**
	 * WindowClosing event handler.
	 * 
	 * @param aEvent
	 *            Event generated.
	 */
	public void windowClosed(WindowEvent aEvent) {
	}

	/**
	 * WindowClosing event handler.
	 * 
	 * @param aEvent
	 *            Event generated.
	 */
	public void windowActivated(WindowEvent aEvent) {
	}

	/**
	 * WindowClosing event handler.
	 * 
	 * @param aEvent
	 *            Event generated.
	 */
	public void windowDeactivated(WindowEvent aEvent) {
	}

	/**
	 * WindowClosing event handler.
	 * 
	 * @param aEvent
	 *            Event generated.
	 */
	public void windowIconified(WindowEvent aEvent) {
	}

	/**
	 * WindowClosing event handler.
	 * 
	 * @param aEvent
	 *            Event generated.
	 */
	public void windowDeiconified(WindowEvent aEvent) {
	}

	/**
	 * WindowClosing event handler.
	 * 
	 * @param aEvent
	 *            Event generated.
	 */
	public void windowOpened(WindowEvent aEvent) {
	}

	/**
	 * Creates the JTree of the LHS Panel ssociated with the class.
	 * 
	 * @param aClassInfo
	 *            Information about the class.
	 * @param aCurrentClass
	 *            Name of the current class being reverse engineered.
	 * 
	 */
	private void createTree(ClassInfo aClassInfo, String aCurrentClass) {
		Import mImports = aClassInfo.getConstantPool().getImportedClasses();
		int dotIndex = aCurrentClass.indexOf(".");
		if (dotIndex != -1) {
			aCurrentClass = aCurrentClass.substring(0, dotIndex);
		}

		List<Field> listFields = aClassInfo.getFields();
		List<Method> listMethods = aClassInfo.getMethods();
		List<String> result = new ArrayList<String>();

		for (Field field : listFields) {
			StringBuilder sb = new StringBuilder("");
			String datatype = Import.getClassName(TypeInferrer.getJLSType(field
					.getDatatype(), false));

			sb.append(field.getQualifier());
			sb.append(" " + datatype);
			sb.append(" " + field.getName());
			result.add(sb.toString());
		}

		for (Method method : listMethods) {
			StringBuilder sb = new StringBuilder("");

			String returnType = Import.getClassName(TypeInferrer.getJLSType(
					method.getReturnType(), false));

			String name = method.getName();

			if (name.compareTo(JVMConstants.CLINIT) == 0) {
				sb.append(JLSConstants.STATIC);
			} else if (name.compareTo(JVMConstants.INIT) == 0) {
				sb.append(method.getQualifier());
				sb.append(aCurrentClass);
				sb.append(writeArgs(method.getArgList(), mImports));
			} else {
				sb.append(method.getQualifier());
				sb.append(returnType);
				sb.append(" " + name);
				sb.append(writeArgs(method.getArgList(), mImports));
			}
			result.add(sb.toString());
		}
		pnlEditor.createModel(this, aCurrentClass, result);
	}

	/**
	 * Returns the argument in Java language Class Format.
	 * 
	 * @param aArgs
	 *            List of arguments of class names in JVM class format.
	 * @param aImports
	 *            List of imported classes.
	 * @return A Concatenated list of classes separated by a comma.
	 */
	private StringBuilder writeArgs(List<String> aArgs, Import aImports) {
		StringBuilder result = new StringBuilder("(");
		boolean first = true;
		for (String str : aArgs) {
			if (first) {
				first = false;
			} else {
				result.append(" ,");
			}
			String argType = Import.getClassName(TypeInferrer.getJLSType(str,
					false));
			result.append(argType);
		}
		result.append(")");
		return result;
	}

	/**
	 * Application Context shared between command line and GUI.
	 */
	private JReverseProContext context;

	/**
	 * Represents the MenuBar.
	 */
	private MainMenu mMbrGen;

	/**
	 * Panel containing the Reverse engineered code.
	 */
	private ClassEditPanel pnlEditor;

	/**
	 * Panel containing the status bar.
	 */
	private StatusPanel mPnlStatusBar;

	/**
	 * Path to class currently reverse engineered.
	 */
	private String mCurrentClass;

	/**
	 * JClassInfo class currently reverse engineered.
	 */
	private ClassInfo mClassInfo;

	/**
	 * Name of the property file.
	 */
	private String mPropertyFile;

	/**
	 * Default Directory of a file open/save dialog.
	 */
	private String mCurDir;

	private static final String CURRENT_DIRECTORY = ".";
}
