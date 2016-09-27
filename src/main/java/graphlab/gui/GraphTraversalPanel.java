package graphlab.gui;

import graphlab.algorithms.GraphSearch;
import graphlab.datastructures.*;
import graphlab.utils.ConsumerWithException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GraphTraversalPanel extends GraphPanel {

    private GraphTraversalWorker traversalWorker;

    public GraphTraversalPanel(SearchType searchType, GraphContainerPanel parentPanel, AdjacencyListGraph graph) {
        super(searchType, parentPanel, graph, false);
        this.searchType = searchType;
        this.parentPanel = parentPanel;
        this.graph = graph;
        setBorder(BorderFactory.createEtchedBorder());
        setBackground(new Color(200, 200, 200));

        MouseListener popupListener = new PopupListener();
        this.addMouseListener(popupListener);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void startOperation() {
        visitedEdges = new ArrayList<>();
        visitedNodes = new ArrayList<>();
        processedNodes = new ArrayList<>();
        repaint();
        traversalWorker = new GraphTraversalWorker(visitedNodes, visitedEdges, processedNodes);
        traversalWorker.execute();
    }

    public void stopOperation() {
        traversalWorker.cancel(true);
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension dimension = parentPanel.getSize();
        panelSide = dimension.width < dimension.height * 2 ? dimension.width / 2 - X_SHIFT : dimension.height - Y_SHIFT;
        return new Dimension(panelSide, panelSide);
    }

    class GraphTraversalWorker extends SwingWorker<Void, Void> {

        List<Node> visitedNodes;
        List<Edge> visitedEdges;
        List<Node> processedNodes;

        public GraphTraversalWorker(List<Node> visitedNodes, List<Edge> visitedEdges, List<Node> processedNodes) {
            this.visitedNodes = visitedNodes;
            this.visitedEdges = visitedEdges;
            this.processedNodes = processedNodes;
        }

        @Override
        protected Void doInBackground() throws Exception {

            Boolean isCanceled = new Boolean(false);

            Consumer<Node> visitNode = node -> {
                visitedNodes.add(node);
                setProgressBar((int) ((visitedNodes.size() / (float) graph.getNodes().size()) * 100));
            };
            Consumer<Node> processNode = node -> processedNodes.add(node);
            ConsumerWithException<Edge> visitEdge = edge -> {
                visitedEdges.add(edge);
                updateGraph();
            };

            switch (searchType) {
                case BFS:
                    GraphSearch.bfs(graph, visitNode, visitEdge, processNode, isCanceled, false);
                    break;
                case DFS:
                    GraphSearch.dfs(graph, visitNode, visitEdge, processNode, isCanceled, false);
                    break;
            }

            setProgressBar(0);
            setOperationAsFinished();
            return null;
        }

    }
}