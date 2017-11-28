/*
 Violet - A program for editing UML diagrams.

 Copyright (C) 2007 Cay S. Horstmann (http://horstmann.com)
 Alexandre de Pellegrin (http://alexdp.free.fr);

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.horstmann.violet.workspace.sidebar.editortools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.horstmann.violet.framework.injection.resources.ResourceBundleInjector;
import com.horstmann.violet.framework.injection.resources.annotation.ResourceBundleBean;
import com.horstmann.violet.workspace.IWorkspace;
import com.horstmann.violet.workspace.editorpart.IEditorPart;
import com.horstmann.violet.workspace.editorpart.IEditorPartBehaviorManager;
import com.horstmann.violet.workspace.editorpart.behavior.CutCopyPasteBehavior;
import com.horstmann.violet.workspace.editorpart.behavior.UndoRedoCompoundBehavior;
import com.horstmann.violet.workspace.sidebar.ISideBarElement;
import com.horstmann.violet.workspace.sidebar.SideBar;

import com.horstmann.violet.product.diagram.abstracts.IGraph;
import com.horstmann.violet.product.diagram.abstracts.edge.IEdge;
import com.horstmann.violet.product.diagram.abstracts.node.INode;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.PieStyler.AnnotationType;


@ResourceBundleBean(resourceReference = SideBar.class)
public class EditorToolsPanel extends JPanel implements ISideBarElement
{

    public EditorToolsPanel()
    {
        super();
        ResourceBundleInjector.getInjector().inject(this);
        this.setUI(new EditorToolsPanelUI(this));
        this.bZoomIn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                workspace.getEditorPart().changeZoom(1);
            }
        });
        this.bZoomOut.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                workspace.getEditorPart().changeZoom(-1);
            }
        });
        this.bUndo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UndoRedoCompoundBehavior undoRedoBehavior = getUndoRedoBehavior();
                if (undoRedoBehavior != null) {
                    undoRedoBehavior.undo();
                }
            }
        });
        this.bRedo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UndoRedoCompoundBehavior undoRedoBehavior = getUndoRedoBehavior();
                if (undoRedoBehavior != null) {
                    undoRedoBehavior.redo();
                }
            }
        });
        this.bDelete.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                workspace.getEditorPart().removeSelected();
            }
        });
        this.bCut.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CutCopyPasteBehavior cutCopyPasteBehavior = getCutCopyPasteBehavior();
                if (cutCopyPasteBehavior != null) {
                    cutCopyPasteBehavior.cut();
                }
            }
        });
        this.bCopy.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CutCopyPasteBehavior cutCopyPasteBehavior = getCutCopyPasteBehavior();
                if (cutCopyPasteBehavior != null) {
                    cutCopyPasteBehavior.copy();
                }
            }
        });
        this.bPaste.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CutCopyPasteBehavior cutCopyPasteBehavior = getCutCopyPasteBehavior();
                if (cutCopyPasteBehavior != null) {
                    cutCopyPasteBehavior.paste();
                }
            }
        });
        this.tmp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                IGraph currentGraph = workspace.getGraphFile().getGraph();
                Collection<INode> nodes = currentGraph.getAllNodes();
                Collection<IEdge> edges = currentGraph.getAllEdges();
                String graphType = currentGraph.getClass().getSimpleName();

                int numberOfNodes = nodes.size();
                int numberOfEdges = edges.size();

				Random rand = new Random();

                // Bar chart
                JFrame.setDefaultLookAndFeelDecorated(true);
                JFrame frame = new JFrame("Bar Chart");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(350, 300);

                // Title
                String title = "Diagram Statistics";
                // Chart values vector
                List<Double> valueVector = new ArrayList<Double>();
                valueVector.add((double)numberOfNodes);
                valueVector.add((double)numberOfEdges);
                // Chart labels vector
                List<String> labelVector = new ArrayList<String>();
                labelVector.add("Number of Nodes");
                labelVector.add("Number of Edges");

                // Handle sequence diagrams and class diagrams differently
                if (graphType.equals("SequenceDiagramGraph")) {
                    List<Integer> outgoingMessages = new ArrayList<Integer>();
                    String warnMsg = "";
                    double averageMsgPerNode = 0;

                    for (INode aNode : nodes) {
                        // Only consider activation bar nodes
                        if (!aNode.getClass().getSimpleName().equals("ActivationBarNode")) {
                            continue;
                        }
                        
                        int numberOfOutgoingMessages = 0;
                            
                        for (IEdge anEdge : edges) {
                            INode start = anEdge.getStartNode();
                            if (aNode.equals(start)) {
                                numberOfOutgoingMessages++;
                            }
                        }
                        
                        if (numberOfOutgoingMessages > 5) {
                            warnMsg += "Number of outgoing messages on node " + aNode.getId() + " has exceeded the threshold outgoing messages of 5\n";
                        }
                            
                        outgoingMessages.add(numberOfOutgoingMessages);
                        averageMsgPerNode += numberOfOutgoingMessages;
                    }

                    if (warnMsg.length() != 0) {
                            JFrame warning = new JFrame();
                            JOptionPane.showMessageDialog(null, warnMsg, "WARNING: High number of outgoing messages from nodes", JOptionPane.INFORMATION_MESSAGE);
                    }

                    // Calculate average
                    averageMsgPerNode = averageMsgPerNode / outgoingMessages.size();

                    labelVector.add("Average Messages per Node");
                    valueVector.add(averageMsgPerNode);
                    labelVector.add("Number of activation nodes");
                    valueVector.add((double)outgoingMessages.size());

                } else if (graphType.equals("ClassDiagramGraph")) {
                    List<Integer> cbo = new ArrayList<Integer>();
                    List<String> nodeTypes  = new ArrayList<String>();
                    String warnMsg = "";
                    double averageCboPerNode = 0;
                    int numberOfPackageNodes = 0,
                        numberOfInterfaceNodes = 0,
                        numberOfBallAndSocketNodes = 0,
                        numberOfClassNodes = 0,
                        numberOfEnumNodes = 0;

                    for (INode aNode : nodes) {
                        int objCbo = 0;

                        if (aNode.getClass().getSimpleName().equals("PackageNode")) {
                            numberOfPackageNodes++;
                        } else if (aNode.getClass().getSimpleName().equals("InterfaceNode")) {
                            numberOfInterfaceNodes++;
                        } else if (aNode.getClass().getSimpleName().equals("BallAndSocketNode")) {
                            numberOfBallAndSocketNodes++;
                        } else if (aNode.getClass().getSimpleName().equals("ClassNode")) {
                            numberOfClassNodes++;
                        } else if (aNode.getClass().getSimpleName().equals("EnumNode")) {
                            numberOfEnumNodes++;
                        } else {
                            continue;
                        }
                        
                        for (IEdge anEdge : edges) {
                            INode start = anEdge.getStartNode();
                            INode end = anEdge.getEndNode();
                            if (aNode.equals(start) || aNode.equals(end)) {
                                objCbo++;
                            }
                        }

                        if (objCbo > 5) {
                            warnMsg += "CBO on node " + aNode.getId() + " has exceeded the threshold CBO of 5\n";
                        }
                    }

                    if (warnMsg.length() != 0) {
                            JFrame warning = new JFrame();
                            JOptionPane.showMessageDialog(null, warnMsg, "WARNING: High number of outgoing messages from nodes", JOptionPane.INFORMATION_MESSAGE);
                    }

                    // Calculate average
                    averageCboPerNode = averageCboPerNode / cbo.size();

                    labelVector.add("Average CBO per Node");
                    valueVector.add((double)averageCboPerNode);
                    labelVector.add("Number of ClassNodes");
                    valueVector.add((double)numberOfClassNodes);
                    labelVector.add("Number of PackageNodes");
                    valueVector.add((double)numberOfPackageNodes);
                    labelVector.add("Number of InterfaceNodes");
                    valueVector.add((double)numberOfInterfaceNodes);
                    labelVector.add("Number of EnumNodes");
                    valueVector.add((double)numberOfEnumNodes);
                    labelVector.add("Number of BallAndSocketNodes");
                    valueVector.add((double)numberOfBallAndSocketNodes);

					// Also display a pie chart for class diagrams
					String[] pieChartLabels =  new String[]{
						"Number of ClassNodes",
						"Number of PackageNodes",
						"Number of InterfaceNodes",
						"Number of EnumNodes",
						"Number of BallAndSocketNodes"
					};
					double[] pieChartValues =  new double[]{
						(double)numberOfClassNodes,
						(double)numberOfPackageNodes,
						(double)numberOfInterfaceNodes,
						(double)numberOfEnumNodes,
						(double)numberOfBallAndSocketNodes
					};
					Color[] pieColors = new Color[pieChartLabels.length];
					for (int i = 0; i < pieChartLabels.length; i++) {
						float r = rand.nextFloat();
						float g = rand.nextFloat();
						float b = rand.nextFloat();

						pieColors[i] = new Color(r, g, b);
					}
					showPieChart(pieChartValues, pieChartLabels, pieColors, title);
                }

                double[] values = new double[valueVector.size()];
                String[] labels = new String[labelVector.size()];
                for (int i = 0; i < valueVector.size(); i++) {
                    values[i] = valueVector.get(i);
                    labels[i] = labelVector.get(i);
                }
                // Generate Colors
                Color[] colors = new Color[values.length];
                for (int i = 0; i < values.length; i++) {
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();

                    colors[i] = new Color(r, g, b);
                }

                showBarChart(values, labels, colors, title);
            }
		});
    }
	
    private void showBarChart(double[] values, String[] labels, Color[] colors, String title) {
        // Bar chart
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Bar Chart");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1280, 720);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
 
        DiagramBarChart bc = new DiagramBarChart(values, labels, colors, title);
        container.add(bc);
        
        frame.add(container);
        frame.setVisible(true);
    }
	
	private void showPieChart(double[] values, String[] labels, Color[] colors, String title) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("XChart Swing Demo");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1280, 720);

		// Create Chart
		PieChart chart = new PieChartBuilder().width(800).height(600).title("My Pie Chart").build();

		// Customize Chart
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setAnnotationType(AnnotationType.LabelAndPercentage);
		chart.getStyler().setAnnotationDistance(1.15);
		chart.getStyler().setPlotContentSize(.7);
		chart.getStyler().setStartAngleInDegrees(90);

		// Series
		for (int i = 0; i < values.length; i++) {
			chart.addSeries(labels[i], values[i]);
		}

		JPanel container = new XChartPanel(chart);

		frame.add(container);
		frame.setVisible(true);
	}
    
    /**
     * Looks for UndoRedoBehavior on the current editor part
     * 
     * @return the first UndoRedoBehavior object found or null
     */
    private UndoRedoCompoundBehavior getUndoRedoBehavior() {
        IEditorPart activeEditorPart = workspace.getEditorPart();
        IEditorPartBehaviorManager behaviorManager = activeEditorPart.getBehaviorManager();
        List<UndoRedoCompoundBehavior> found = behaviorManager.getBehaviors(UndoRedoCompoundBehavior.class);
        if (found.size() != 1) {
            return null;
        }
        return found.get(0);
    }
    
    /**
     * Looks for CutCopyPasteBehavior on the current editor part
     * 
     * @return the first CutCopyPasteBehavior object found or null
     */
    private CutCopyPasteBehavior getCutCopyPasteBehavior() {
        IEditorPart activeEditorPart = workspace.getEditorPart();
        IEditorPartBehaviorManager behaviorManager = activeEditorPart.getBehaviorManager();
        List<CutCopyPasteBehavior> found = behaviorManager.getBehaviors(CutCopyPasteBehavior.class);
        if (found.size() != 1) {
            return null;
        }
        return found.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.horstmann.violet.framework.display.clipboard.sidebar.ISideBarElement#getTitle()
     */
    public String getTitle()
    {
        return this.title;
    }


    /* (non-Javadoc)
     * @see com.horstmann.violet.product.workspace.sidebar.ISideBarElement#install(com.horstmann.violet.product.workspace.IWorkspace)
     */
    public void install(IWorkspace workspace)
    {
        this.workspace = workspace;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.horstmann.violet.framework.display.clipboard.sidebar.ISideBarElement#getAWTComponent()
     */
    public Component getAWTComponent()
    {
        return this;
    }

    /**
     * @return zoom in button
     */
    public JButton getZoomInButton()
    {
        return this.bZoomIn;
    }

    /**
     * @return zoom out button
     */
    public JButton getZoomOutButton()
    {
        return this.bZoomOut;
    }

    /**
     * @return undo button
     */
    public JButton getUndoButton()
    {
        return this.bUndo;
    }

    /**
     * @return redo button
     */
    public JButton getRedoButton()
    {
        return this.bRedo;
    }

    /**
     * @return delete button
     */
    public JButton getDeleteButton()
    {
        return this.bDelete;
    }

    /**
     * @return cut button
     */
    public JButton getCutButton()
    {
        return this.bCut;
    }

    /**
     * @return copy button
     */
    public JButton getCopyButton()
    {
        return this.bCopy;
    }

    /**
     * @return paste button
     */
    public JButton getPasteButton()
    {
        return this.bPaste;
    }

    public JButton getTmpButton()
    {
        return this.tmp;
    }

    /** current workspace */
    private IWorkspace workspace;

    @ResourceBundleBean(key = "zoomout")
    private JButton tmp;

    @ResourceBundleBean(key = "zoomin")
    private JButton bZoomIn;
    @ResourceBundleBean(key = "zoomout")
    private JButton bZoomOut;
    @ResourceBundleBean(key = "undo")
    private JButton bUndo;
    @ResourceBundleBean(key = "redo")
    private JButton bRedo;
    @ResourceBundleBean(key = "delete")
    private JButton bDelete;
    @ResourceBundleBean(key = "cut")
    private JButton bCut;
    @ResourceBundleBean(key = "copy")
    private JButton bCopy;
    @ResourceBundleBean(key = "paste")
    private JButton bPaste;
    @ResourceBundleBean(key = "title.standardbuttons.text")
    private String title;
    
}
