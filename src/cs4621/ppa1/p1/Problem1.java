package cs4621.ppa1.p1;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import layout.TableLayout;
import cs4621.framework.Camera;
import cs4621.framework.CameraController;
import cs4621.framework.GLSceneDrawer;
import cs4621.framework.PickingController;
import cs4621.framework.PickingEventListener;
import cs4621.ppa1.manip.Manip;
import cs4621.ppa1.manip.RotateManip;
import cs4621.ppa1.manip.ScaleManip;
import cs4621.ppa1.manip.TranslateManip;
import cs4621.ppa1.material.GLPhongMaterial;
import cs4621.ppa1.scene.GLLightManager;
import cs4621.ppa1.scene.LightNode;
import cs4621.ppa1.scene.MeshNode;
import cs4621.ppa1.scene.Scene;
import cs4621.ppa1.scene.SceneNode;
import cs4621.ppa1.scene.TransformationNode;
import cs4621.ppa1.shape.Cube;
import cs4621.ppa1.shape.Cylinder;
import cs4621.ppa1.shape.Mesh;
import cs4621.ppa1.shape.Sphere;
import cs4621.ppa1.shape.Teapot;
import cs4621.ppa1.shape.Torus;
import cs4621.ppa1.ui.BasicAction;
import cs4621.ppa1.ui.GLPhongMaterialSettingPanel;
import cs4621.ppa1.ui.LightSettingPanel;
import cs4621.ppa1.ui.NameSettingPanel;
import cs4621.ppa1.ui.OneFourViewPanel;
import cs4621.ppa1.ui.PopupListener;
import cs4621.ppa1.ui.SliderPanel;
import cs4621.ppa1.ui.TransformSettingPanel;
import cs4621.ppa1.ui.TreeRenderer;

/**
 * Main window of Problem 3.
 *
 * This application can:
 * 1) Load, display, and save scene graph.
 * 2) Let the user manipulate scene graph by elements by entering text.
 *
 * @author pramook, arbree
 */
public class Problem1 extends JFrame implements GLSceneDrawer,
	ChangeListener, ActionListener, PickingEventListener, TreeSelectionListener

{
	private static final long serialVersionUID = 1L;

	//Menu commands
	private static final String SAVE_AS_MENU_TEXT = "Save As";
	private static final String OPEN_MENU_TEXT = "Open";
	private static final String EXIT_MENU_TEXT = "Exit";
	private static final String CLEAR_SELECTED_TEXT = "Clear selection";
	private static final String GROUP_MENU_TEXT = "Group selected";
	private static final String REPARENT_MENU_TEXT = "Reparent selected";
	private static final String DELETE_MENU_TEXT = "Delete selected";
	private static final String PICK_MENU_TEXT = "Select";
	private static final String ROTATE_MENU_TEXT = "Rotate selected";
	private static final String TRANSLATE_MENU_TEXT = "Translate selected";
	private static final String SCALE_MENU_TEXT = "Scale selected";
	private static final String ADD_LIGHT_MENU_TEXT = "Add Light";
	private static final String ADD_SPHERE_MENU_TEXT = "Add Sphere";
	private static final String ADD_CUBE_MENU_TEXT = "Add Cube";
	private static final String ADD_CYLINDER_MENU_TEXT = "Add Cylinder";
	private static final String ADD_TORUS_MENU_TEXT = "Add Torus";
	private static final String ADD_TEAPOT_MENU_TEXT = "Add Teapot";

	JSplitPane mainSplitPane;
	JSplitPane leftSplitPane;
	OneFourViewPanel oneFourViewPanel;
	SliderPanel sliderPanel;
	JFileChooser fileChooser;
	JTree treeView;
	GLPhongMaterialSettingPanel phongMaterialPanel;
	TransformSettingPanel transformSettingPanel;
	LightSettingPanel lightSettingPanel;
	NameSettingPanel nameSettingPanel;
	JPanel nodeSettingPanel;

	TranslateManip translateManip;
	RotateManip rotateManip;
	ScaleManip scaleManip;
	Manip currentManip;
	boolean showManip;
	boolean isManipulatingManip;

	Scene scene;

	boolean drawForPicking = false;
	SceneNode[] nodesToReparent = null;
	boolean isReparenting = false;

	public Problem1() {
		super("CS 4621/5621 Programming Assignment 1 / Problem 1");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent windowevent ) {
				terminate();
			}
		});

		initMainSplitPane();
		getContentPane().add(mainSplitPane, BorderLayout.CENTER);
		sliderPanel = new SliderPanel(this, -1.5f, -0.25f, 0.133f, true, 1000, "0.000");
		getContentPane().add(sliderPanel, BorderLayout.EAST);

		scene = new Scene();
		treeView.setModel(scene.getTreeModel());

		initActionsAndMenus();

		initManip();

		fileChooser = new JFileChooser(new File("data"));
	}

	public static void main(String[] args)
	{
		new Problem1().run();
	}

	public void run()
	{
		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
		oneFourViewPanel.startAnimation();
	}

	/**
	 * Maps all GUI actions to listeners in this object and builds the menu
	 */
	protected void initActionsAndMenus()
	{
		//Create all the actions
		BasicAction group = new BasicAction(GROUP_MENU_TEXT, this);
		BasicAction reparent = new BasicAction(REPARENT_MENU_TEXT, this);
		BasicAction delete = new BasicAction(DELETE_MENU_TEXT, this);
		BasicAction clear = new BasicAction(CLEAR_SELECTED_TEXT, this);

		BasicAction addLight = new BasicAction(ADD_LIGHT_MENU_TEXT, this);
		BasicAction addSphere = new BasicAction(ADD_SPHERE_MENU_TEXT, this);
		BasicAction addCube = new BasicAction(ADD_CUBE_MENU_TEXT, this);
		BasicAction addCylinder = new BasicAction(ADD_CYLINDER_MENU_TEXT, this);
		BasicAction addTorus = new BasicAction(ADD_TORUS_MENU_TEXT, this);
		BasicAction addTeapot = new BasicAction(ADD_TEAPOT_MENU_TEXT, this);

		BasicAction saveAs = new BasicAction(SAVE_AS_MENU_TEXT, this);
		BasicAction open = new BasicAction(OPEN_MENU_TEXT, this);
		BasicAction exit = new BasicAction(EXIT_MENU_TEXT, this);

		BasicAction pickTool = new BasicAction(PICK_MENU_TEXT, this);
		BasicAction rotateTool = new BasicAction(ROTATE_MENU_TEXT, this);
		BasicAction translateTool = new BasicAction(TRANSLATE_MENU_TEXT, this);
		BasicAction scaleTool = new BasicAction(SCALE_MENU_TEXT, this);

		//Set shortcut keys
		group.setAcceleratorKey(KeyEvent.VK_G, KeyEvent.CTRL_MASK);
		reparent.setAcceleratorKey(KeyEvent.VK_R, KeyEvent.CTRL_MASK);
		delete.setAcceleratorKey(KeyEvent.VK_DELETE, 0);

		pickTool.setAcceleratorKey(KeyEvent.VK_Q, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);
		translateTool.setAcceleratorKey(KeyEvent.VK_W, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);
		rotateTool.setAcceleratorKey(KeyEvent.VK_E, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);
		scaleTool.setAcceleratorKey(KeyEvent.VK_R, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_DOWN_MASK);

		saveAs.setAcceleratorKey(KeyEvent.VK_A, KeyEvent.CTRL_MASK);
		open.setAcceleratorKey(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
		exit.setAcceleratorKey(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);

		//Create the menu
		JMenuBar bar = new JMenuBar();
		JMenu menu;

		menu = new JMenu("File");
		menu.setMnemonic('F');
		menu.add(new JMenuItem(open));
		menu.add(new JMenuItem(saveAs));
		menu.addSeparator();
		menu.add(new JMenuItem(exit));
		bar.add(menu);

		menu = new JMenu("Edit");
		menu.setMnemonic('E');
		menu.add(new JMenuItem(group));
		menu.add(new JMenuItem(reparent));
		menu.add(new JMenuItem(delete));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(pickTool));
		menu.add(new JMenuItem(translateTool));
		menu.add(new JMenuItem(rotateTool));
		menu.add(new JMenuItem(scaleTool));
		bar.add(menu);

		menu = new JMenu("Scene");
		menu.setMnemonic('S');
		menu.add(new JMenuItem(addLight));
		menu.add(new JMenuItem(addSphere));
		menu.add(new JMenuItem(addCube));
		menu.add(new JMenuItem(addCylinder));
		menu.add(new JMenuItem(addTorus));
		menu.add(new JMenuItem(addTeapot));
		bar.add(menu);

		setJMenuBar(bar);

		JPopupMenu p = new JPopupMenu();
		p.add(new JMenuItem(group));
		p.add(new JMenuItem(reparent));
		p.add(new JMenuItem(delete));
		p.add(new JMenuItem(clear));
		p.addSeparator();
		p.add(new JMenuItem(addLight));
		p.add(new JMenuItem(addSphere));
		p.add(new JMenuItem(addCube));
		p.add(new JMenuItem(addCylinder));
		p.add(new JMenuItem(addTorus));
		p.add(new JMenuItem(addTeapot));

		treeView.addMouseListener(new PopupListener(p));
		treeView.addTreeSelectionListener(this);
	}

	private void initMainSplitPane()
	{
		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
		initLeftSplitPane();
		mainSplitPane.setLeftComponent(leftSplitPane);

		oneFourViewPanel = new OneFourViewPanel(this);
		oneFourViewPanel.addPickingEventListener(this);
		mainSplitPane.setRightComponent(oneFourViewPanel);

		mainSplitPane.resetToPreferredSizes();
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setResizeWeight(0.2);
	}

	private void initLeftSplitPane()
	{
		leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
		initTreeView();
		leftSplitPane.setTopComponent(treeView);

		nodeSettingPanel = new JPanel();
		nodeSettingPanel.setLayout(new TableLayout(new double[][] {
			{
				TableLayout.FILL
			},
			{
				TableLayout.MINIMUM,
				TableLayout.MINIMUM,
				TableLayout.MINIMUM
			}
		}));
		leftSplitPane.setBottomComponent(nodeSettingPanel);
		
		nameSettingPanel = new NameSettingPanel();
		nodeSettingPanel.add(nameSettingPanel, "0,0,0,0");
		nameSettingPanel.setVisible(false);

		transformSettingPanel = new TransformSettingPanel();
		nodeSettingPanel.add(transformSettingPanel, "0,1,0,1");
		transformSettingPanel.setVisible(false);

		phongMaterialPanel = new GLPhongMaterialSettingPanel();
		nodeSettingPanel.add(phongMaterialPanel, "0,2,0,2");
		phongMaterialPanel.setVisible(false);

		lightSettingPanel = new LightSettingPanel();
		nodeSettingPanel.add(lightSettingPanel, "0,3,0,3");
		lightSettingPanel.setVisible(false);

		leftSplitPane.resetToPreferredSizes();
		leftSplitPane.setOneTouchExpandable(true);
		leftSplitPane.setResizeWeight(0.95);
	}

	private void initTreeView()
	{
		// Create the tree views
		treeView = new JTree();
		DefaultTreeCellRenderer renderer = new TreeRenderer();
		treeView.setCellRenderer(renderer);
		treeView.setEditable(true);
		treeView.setCellEditor(new DefaultTreeCellEditor(treeView, renderer));
		treeView.setShowsRootHandles(true);
		treeView.setRootVisible(true);

		KeyListener[] kls = treeView.getKeyListeners();
		for (int i=0; i<kls.length; i++)
			treeView.removeKeyListener(kls[i]);
	}

	private void initManip()
	{
		translateManip = new TranslateManip();
		rotateManip = new RotateManip();
		scaleManip = new ScaleManip();

		translateManip.addChangeListener(this);
		rotateManip.addChangeListener(this);
		scaleManip.addChangeListener(this);

		currentManip = null;
		showManip = false;
		isManipulatingManip = false;

		oneFourViewPanel.addPrioritizedObjectId(Manip.PICK_X);
		oneFourViewPanel.addPrioritizedObjectId(Manip.PICK_Y);
		oneFourViewPanel.addPrioritizedObjectId(Manip.PICK_Z);
		oneFourViewPanel.addPrioritizedObjectId(Manip.PICK_CENTER);
	}

	private float getTolerance()
	{
		return sliderPanel.getValue();
	}

	public void init(GLAutoDrawable drawable, CameraController cameraController) {
		final GL2 gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Set depth buffer.
		gl.glClearDepth(1.0f);
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glEnable(GL2.GL_DEPTH_TEST);

		// Set blending mode.
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL2.GL_BLEND);

		// Forces OpenGL to normalize transformed normals to be of
		// unit length before using the normals in OpenGL's lighting equations.
		gl.glEnable(GL2.GL_NORMALIZE);

		// Cull back faces.
		gl.glDisable(GL2.GL_CULL_FACE);

		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

		rebuildMeshes();
		oneFourViewPanel.startAnimation();
	}


	public void draw(GLAutoDrawable drawable, CameraController cameraController)
	{
		final GL2 gl = drawable.getGL().getGL2();

		Camera camera = cameraController.getCamera();

		if (drawForPicking)
		{
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
			GLLightManager.teardownLighting(gl);
			scene.renderForPicking(gl);

			if (currentManip != null && showManip)
				currentManip.renderConstantSize(gl, camera, true);
		}
		else
		{
			gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE);
			gl.glEnable(GL2.GL_COLOR_MATERIAL);

			if (oneFourViewPanel.isWireframeMode())
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			else
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

			if (oneFourViewPanel.isLightingMode())
				scene.setupLighting(gl);
			else
				gl.glDisable(GL2.GL_LIGHTING);

			scene.render(gl);

			if (currentManip != null && showManip)
				currentManip.renderConstantSize(gl, camera, false);

			if (oneFourViewPanel.isLightingMode())
				GLLightManager.teardownLighting(gl);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sliderPanel.getSlider())
		{
			rebuildMeshes();
		}
		else if (e.getSource() == currentManip)
		{
			TransformationNode node = currentManip.getTransformationNode();
			transformSettingPanel.setTransformationNode(node);
			transformSettingPanel.repaint();
		}
	}

	protected void rebuildMeshes()
	{
		scene.rebuildMeshes(getTolerance());
	}

	public void terminate()
	{
		oneFourViewPanel.stopAnimation();
		dispose();
		System.exit(0);
	}

	protected void refresh()
	{
		oneFourViewPanel.repaint();
	}

	/**
	 * Save the current tree to a file.
	 */
	protected void saveTreeAs()
	{
		//Pick a file
		int choice = fileChooser.showSaveDialog(this);
		if (choice != JFileChooser.APPROVE_OPTION)
		{
			refresh();
			return;
		}
		String filename = fileChooser.getSelectedFile().getAbsolutePath();

		//Write the tree out
		try
		{
			scene.save(filename);
		}
		catch (IOException ioe)
		{
			showExceptionDialog(ioe);
		}

		refresh();
	}

	/**
	 * Displays an exception in a window
	 * @param e
	 */
	protected void showExceptionDialog(Exception e)
	{
		String str = "The following exception was thrown: " + e.toString() + ".\n\n" + "Would you like to see the stack trace?";
		int choice = JOptionPane.showConfirmDialog(this, str, "Exception Thrown", JOptionPane.YES_NO_OPTION);

		if (choice == JOptionPane.YES_OPTION) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a tree stored in a file
	 */
	protected void openTree()
	{
		//Select a file
		int choice = fileChooser.showOpenDialog(this);
		if (choice != JFileChooser.APPROVE_OPTION)
		{
			refresh();
			return;
		}
		String filename = fileChooser.getSelectedFile().getAbsolutePath();

		//Load the tree
		try
		{
			scene.load(filename);
			rebuildMeshes();
		}
		catch (Exception e) {
			showExceptionDialog(e);
		}

		//Update the window
		refresh();
	}

	/**
	 * Add a new shape to the tree
	 */
	protected void addNewShape(Class<? extends Mesh> c)
	{
		TreePath path = treeView.getSelectionPath();

		//Add the node
		try
		{
			scene.addNewShape(path, c);
			rebuildMeshes();
			refresh();
		}
		catch (Exception e) {
			showExceptionDialog(e);
		}
	}

	protected SceneNode[] getSelection()
	{
		TreePath[] paths = treeView.getSelectionPaths();
		if (paths == null) {
			return new SceneNode[] {};
		}
		SceneNode[] ts = new SceneNode[paths.length];
		for (int i = 0; i < paths.length; i++) {
			ts[i] = (SceneNode) paths[i].getLastPathComponent();
		}
		return ts;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == null) {
			return;
		}
		else if (cmd.equals(GROUP_MENU_TEXT)) {
			SceneNode groupNode = scene.groupNodes(getSelection(), "Group");
			treeView.expandPath(new TreePath(groupNode.getPath()));
			refresh();
		}
		else if (cmd.equals(CLEAR_SELECTED_TEXT)) {
			treeView.clearSelection();
		}
		else if (cmd.equals(REPARENT_MENU_TEXT)) {
			nodesToReparent = getSelection();
			isReparenting = true;
		}
		else if (cmd.equals(DELETE_MENU_TEXT)) {
			scene.deleteNodes(getSelection());
			refresh();
		}
		else if (cmd.equals(PICK_MENU_TEXT)) {
			currentManip = null;
			refresh();
		}
		else if (cmd.equals(TRANSLATE_MENU_TEXT)) {
			currentManip = translateManip;
			SceneNode ts[] = getSelection();
			showManip = ts.length == 1;
			if (showManip)
				translateManip.setTransformation((TransformationNode)ts[0]);
			refresh();
		}
		else if (cmd.equals(ROTATE_MENU_TEXT)) {
			currentManip = rotateManip;
			SceneNode ts[] = getSelection();
			showManip = ts.length == 1;
			if (showManip)
				rotateManip.setTransformation((TransformationNode)ts[0]);
			refresh();
		}
		else if (cmd.equals(SCALE_MENU_TEXT)) {
			currentManip = scaleManip;
			SceneNode ts[] = getSelection();
			showManip = ts.length == 1;
			if (showManip)
			{
				scaleManip.setTransformation((TransformationNode)ts[0]);
			}
			refresh();
		}
		else if (cmd.equals(ADD_LIGHT_MENU_TEXT)) {
			scene.addNewLight(treeView.getSelectionPath());
		}
		else if (cmd.equals(ADD_SPHERE_MENU_TEXT)) {
			addNewShape(Sphere.class);
		}
		else if (cmd.equals(ADD_CUBE_MENU_TEXT)) {
			addNewShape(Cube.class);
		}
		else if (cmd.equals(ADD_CYLINDER_MENU_TEXT)) {
			addNewShape(Cylinder.class);
		}
		else if (cmd.equals(ADD_TORUS_MENU_TEXT)) {
			addNewShape(Torus.class);
		}
		else if (cmd.equals(ADD_TEAPOT_MENU_TEXT)) {
			addNewShape(Teapot.class);
		}
		else if (cmd.equals(OPEN_MENU_TEXT)) {
			openTree();
		}
		else if (cmd.equals(SAVE_AS_MENU_TEXT)) {
			saveTreeAs();
		}
		else if (cmd.equals(EXIT_MENU_TEXT)) {
			terminate();
		}
	}

	@Override
	public void objectPicked(Object source, int objectId,
			Vector3f pickLocation, Vector2f mousePosition)
	{
		if (Manip.isManipId(objectId))
		{
			PickingController pickingController = (PickingController)source;
			CameraController cameraController = pickingController.getCameraController();
			currentManip.setPickedInfo(objectId, cameraController.getCamera(), cameraController.getLastMousePosition());
			isManipulatingManip = true;
		}

		SceneNode node = scene.searchForMeshId(objectId);
		if (node != null)
			treeView.setSelectionPath(new TreePath(node.getPath()));
	}

	@Override
	public void startPickingMode(Object source)
	{
		drawForPicking = true;
	}

	@Override
	public void stopPickingMode(Object source)
	{
		drawForPicking = false;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		SceneNode[] selection = getSelection();

		// Handle reparenting.
		if (isReparenting)
		{
			// Invalid number of new parents selected?
			if (selection.length != 1) return;
			SceneNode parent = selection[0];
			scene.reparent(nodesToReparent, parent);
			isReparenting = false;
		}

		showHideSettingPanels(selection);

		showManip = selection.length == 1;
		if (showManip && currentManip != null)
			currentManip.setTransformation((TransformationNode)selection[0]);

		/*
		else {
			SceneNode[] transformations = getLeafmostSelectedTransformations();
			if (transformations.length != 0) {
				if (currentManip != null) {
					currentManip.drawEnabled = true;
					currentManip.setTransformation((Transformation)transformations[0]);
				}
			}
			attributeHolder.remove(currentAttributesPanel);
			if (getSelection().length == 1) {
				SceneNode node = getSelection()[0];

				// The attr panel is stored as the node's user object
				currentAttributesPanel = (ItemAttributePanel)node.getUserObject();
				if(currentAttributesPanel == null)
					currentAttributesPanel = ItemAttributePanel.EMPTY_PANEL;
			}
			else {
				currentAttributesPanel = ItemAttributePanel.EMPTY_PANEL;
			}
			attributeHolder.add(currentAttributesPanel, BorderLayout.NORTH);

			currentAttributesPanel.invalidate();
			((ItemAttributePanel)currentAttributesPanel).refresh();
			leftWindow.revalidate();
			leftWindow.repaint();
		}
		*/
		refresh();
	}

	private void showHideSettingPanels(SceneNode[] selection)
	{
		if (selection.length == 1)
		{
			SceneNode node = selection[0];

			int visibleCount = 0;
			
			nameSettingPanel.setNode(node);
			nameSettingPanel.setVisible(true);
			
			nodeSettingPanel.add(nameSettingPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
			visibleCount += 1;

			if (node instanceof TransformationNode)
			{
				TransformationNode transformationNode = (TransformationNode)node;
				transformSettingPanel.setTransformationNode(transformationNode);
				transformSettingPanel.setVisible(true);

				nodeSettingPanel.add(transformSettingPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
				visibleCount += 1;
			}
			else
				transformSettingPanel.setVisible(false);

			if (node instanceof MeshNode)
			{
				MeshNode meshNode = (MeshNode)node;
				GLPhongMaterial material = (GLPhongMaterial)meshNode.getMaterial();
				phongMaterialPanel.setMaterial(material);
				phongMaterialPanel.setVisible(true);

				nodeSettingPanel.add(phongMaterialPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
				visibleCount += 1;
			}
			else
				phongMaterialPanel.setVisible(false);

			if (node instanceof LightNode)
			{
				LightNode lightNode = (LightNode)node;
				lightSettingPanel.setLightNode(lightNode);
				lightSettingPanel.setVisible(true);

				nodeSettingPanel.add(lightSettingPanel, "0,"+Integer.toString(visibleCount)+",0,"+Integer.toString(visibleCount));
				visibleCount += 1;
			}
			else
				lightSettingPanel.setVisible(false);
		}
		else
		{
			nameSettingPanel.setVisible(false);
			phongMaterialPanel.setVisible(false);
			transformSettingPanel.setVisible(false);
			lightSettingPanel.setVisible(false);
		}
	}

	@Override
	public void mousePressed(MouseEvent e, CameraController controller) {
		// NOP
	}

	@Override
	public void mouseReleased(MouseEvent e, CameraController controller) {
		if (currentManip != null && showManip && isManipulatingManip)
		{
			currentManip.released();
		}
		isManipulatingManip = false;
	}

	@Override
	public void mouseDragged(MouseEvent e, CameraController controller) {
		if (currentManip != null && showManip && isManipulatingManip)
		{
			currentManip.dragged(controller.getCurrentMousePosition(),
					controller.getMouseDelta());
		}
	}
}
