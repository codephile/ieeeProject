//MVSC Algorithm Implementation Main Code

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.text.*;
import javax.swing.filechooser.*;
import javax.vecmath.*;
import org.jfree.ui.RefineryUtilities;

class multiViewCluster extends JFrame implements ActionListener, ComponentListener
{
	JFrame frmRootPath = new JFrame("Clustering with Multi-Viewpoint based Similarity Measure");
	JFrame frmResult = new JFrame("Results Window");
	JFrame frmClustersDocScores = new JFrame("Pairwise Document Scores");
	JFrame frmClusters = new JFrame("Clusters");
	
	JButton btProcess=new JButton("Process");
	JTextField txtFieldAlphaIr = new JTextField ("");
	JTextField txtFieldAlgoChoice = new JTextField ("");
		
	ItemsetCollection Similarities=new ItemsetCollection();
	
	JButton btHistogram=new JButton("Histogram");
	JButton btSimilarity=new JButton("Similarity");
	JButton btCluster=new JButton("Clusters");
	
	ItemsetCollection Hist=new ItemsetCollection();
	
	JLabel lblResult=new JLabel("Result:");
	JLabel lblAlphaIr=new JLabel("IR alpha: ");
	JLabel lblClustersDocScores=new JLabel("Pairwise Document Scores:");
	JLabel lblClusters=new JLabel("Clusters:");
	JLabel lblAlgoChoice = new JLabel("Algorithm Index:");
		
	JTextArea txtResult=new JTextArea("");
	JScrollPane spResult=new JScrollPane(txtResult);
	JTextArea txtMessage=new JTextArea("");
	JScrollPane spMessage=new JScrollPane(txtMessage);
	JTextArea txtClustersDocScores=new JTextArea("");
	JScrollPane spClustersDocScores=new JScrollPane(txtClustersDocScores);	
	JTextArea txtClusters=new JTextArea("");
	JScrollPane spClusters=new JScrollPane(txtClusters);
	
	JButton btChoosefile = new JButton("Select index file");
	
	final JFileChooser fc = new JFileChooser();
	File indexFile;

	//system parameters
	double simalpha=0.6;
	double[][][] sim,sim_perc;
	HTML_Parser parser1;
	Queue frontier;
	int maxPages=30000;
	String[] visitedPages;
	int nVisited=0;
	double similarityThreshold=0.30;
	
	String processData="processlog";
	String clusterPath="clusters";
	String parsePath="Parse";
	
	String similarityPath="similarity";
	String logText="";
	String txtFileRoot="";
	String dirName;
	
	//init documents
	int nDocuments;
	int nRun = 0;
	WebDocument documents[];
	WebDocument CumulativeDocument;
	ItemsetCollection Clusters=new ItemsetCollection();
	
	java.util.ArrayList<GVector> docVectors = new java.util.ArrayList<GVector>();
	java.util.ArrayList<GVector> docNormVectors = new java.util.ArrayList<GVector>();
	GMatrix docMatrix = null;
	GMatrix initialClusterMatrix = null;
	GMatrix initialClusterMatrixCopy = null;
	GMatrix similarityMatrix = null;
	GMatrix similarityMatrixIv = null;
	int[] seedDocumentIndices = null, nr = null, nrIv = null, nrKMeans = null;
	java.util.ArrayList<GVector> docClusters = new java.util.ArrayList<GVector>();
	GVector[] Dr = null;
	GVector[] DrIv = null;
	GVector[] Cr = null;
	GVector[] CrIv = null;
	GVector D = null, C = null;
	GVector DIv = null, CIv = null;
	int numUniqueTerms = 0;
	java.util.List<Integer> v = new ArrayList<Integer>();
	java.util.List<Integer> vIv = new ArrayList<Integer>();
	double alphaVal = 0.3;
	int algoChoice = 1;
	GVector[] finalClusters = null;
	GVector[] finalClustersIv = null;
	GVector[] finalClustersKmeans = null;
	GVector[] finalClustersKMeansCentroid = null;
	GMatrix prevClusterKMeans = null;
	GMatrix currClusterKMeans = null;
	int[][] clusterDocCounts = null;
	int[] totalWordCount = null;
	String stopWords;
	Stemmer s = new Stemmer();
	int repeat_limit = 10000;
	int lock_var = 0;
	int classNum = 0;
	int[] ni = null;
	GVector[] classDistribution = null;
	HashMap<Integer, Integer> classMap = null;
	
	int[][] nij_ir = null, nij_iv = null, nij_kmeans = null;
	double[][] fij_ir = null, fij_iv = null, fij_kmeans = null;
	double fscore_ir = 0.0, fscore_iv = 0.0, fscore_kmeans = 0.0;
	double nmi_ir = 0.0, nmi_iv = 0.0, nmi_kmeans = 0.0;
	double accuracy_ir = 0.0, accuracy_iv = 0.0, accuracy_kmeans = 0.0;

	
	multiViewCluster()
	{
		//Root path frame
		frmRootPath.setDefaultLookAndFeelDecorated(true);
		frmRootPath.setResizable(true);
		frmRootPath.setBounds(50,50,700,400);
		frmRootPath.setLocationRelativeTo(null);
		frmRootPath.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRootPath.addComponentListener(this);
		frmRootPath.getContentPane().setLayout(null);
		frmRootPath.getContentPane().setBackground(new Color(101,67,33));
		
		//Result frame
		frmResult.setDefaultLookAndFeelDecorated(true);
		frmResult.setResizable(true);
		frmResult.setBounds(50,50,600,580);
		frmResult.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmResult.addComponentListener(this);
		frmResult.getContentPane().setLayout(null);
		frmResult.getContentPane().setBackground(new Color(101,67,33));
		
		//Clusters Document Scores frame
		frmClustersDocScores.setDefaultLookAndFeelDecorated(true);
		frmClustersDocScores.setResizable(true);
		frmClustersDocScores.setBounds(100,100,600,580);
		frmClustersDocScores.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmClustersDocScores.addComponentListener(this);
		frmClustersDocScores.getContentPane().setLayout(null);	
		frmClustersDocScores.getContentPane().setBackground(new Color(101,67,33));
		
		//Clusters frame
		frmClusters.setDefaultLookAndFeelDecorated(true);
		frmClusters.setResizable(true);
		frmClusters.setBounds(150,150,600,580);
		frmClusters.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmClusters.addComponentListener(this);
		frmClusters.getContentPane().setLayout(null);			
		frmClusters.getContentPane().setBackground(new Color(101,67,33));
		
		// File Choose Button
		btChoosefile.setBounds(248,50,200,40);
		btChoosefile.addActionListener(this);
		frmRootPath.getContentPane().add(btChoosefile);		
		
		//Process button Design
		btProcess.setBounds(50,250,90,40);
		btProcess.addActionListener(this);
		frmRootPath.getContentPane().add(btProcess);
		
		//Clusters button Design
		btSimilarity.setBounds(150,250,90,40);
		btSimilarity.addActionListener(this);
		frmRootPath.getContentPane().add(btSimilarity);

		//Similarity button Design
		btCluster.setBounds(250,250,90,40);
		btCluster.addActionListener(this);
		frmRootPath.getContentPane().add(btCluster);
		
		//Histogram button design
		btHistogram.setBounds(350,250,90,40);
		btHistogram.addActionListener(this);
		frmRootPath.getContentPane().add(btHistogram);		
		
		txtFieldAlphaIr.setBounds(160, 310, 190, 40);
		txtFieldAlphaIr.setBackground(new Color(128,0,0));
		txtFieldAlphaIr.setForeground(new Color(255,255,255));
		txtFieldAlphaIr.addActionListener(this);
		frmRootPath.getContentPane().add(txtFieldAlphaIr);
		
		txtFieldAlgoChoice.setBounds(470, 310, 190, 40);
		txtFieldAlgoChoice.setBackground(new Color(128,0,0));
		txtFieldAlgoChoice.setForeground(new Color(255,255,255));
		txtFieldAlgoChoice.addActionListener(this);
		frmRootPath.getContentPane().add(txtFieldAlgoChoice);		
		
		lblAlphaIr.setBounds(50, 325, 100, 10);  
		lblAlphaIr.setForeground(new Color(255,255,255));
		frmRootPath.getContentPane().add(lblAlphaIr);
		
		lblAlgoChoice.setBounds(360, 325, 100, 10);  
		lblAlgoChoice.setForeground(new Color(255,255,255));
		frmRootPath.getContentPane().add(lblAlgoChoice);		
		
		//Result Design
		lblResult.setBounds(17,35,100,20);
		lblResult.setForeground(new Color(255,255,255));
		frmResult.getContentPane().add(lblResult);
		spResult.setBounds(15,55,560,450);
		frmResult.getContentPane().add(spResult);
		txtResult.setEditable(false);
		txtResult.setBackground(new Color(128,0,0));
		txtMessage.setBackground(new Color(128,0,0));
		txtResult.setForeground(new Color(255,255,255));
		txtMessage.setForeground(new Color(255,255,255));		
		
		//Clusters Document Scores Design
		lblClustersDocScores.setBounds(17,35,100,20);
		lblClustersDocScores.setForeground(new Color(255,255,255));
		frmClustersDocScores.getContentPane().add(lblClustersDocScores);
		spClustersDocScores.setBounds(15,55,560,450);
		frmClustersDocScores.getContentPane().add(spClustersDocScores);
		txtClustersDocScores.setEditable(false);
		txtClustersDocScores.setBackground(new Color(128,0,0));
		txtClustersDocScores.setForeground(new Color(255,255,255));

		//Clusters Design
		lblClusters.setBounds(17,35,100,20);
		lblClusters.setForeground(new Color(255,255,255));
		frmClusters.getContentPane().add(lblClusters);
		spClusters.setBounds(15,55,560,450);
		frmClusters.getContentPane().add(spClusters);
		txtClusters.setEditable(false);			
		txtClusters.setBackground(new Color(128,0,0));
		txtClusters.setForeground(new Color(255,255,255));
		
		spMessage.setBounds(50,110,595,120);
		frmRootPath.getContentPane().add(spMessage);
		txtMessage.setEditable(false);		
		
		frmRootPath.setVisible(true);
	}
	
	public void componentResized(ComponentEvent e) {
		if(e.getSource() == frmResult)
		{
			spResult.setBounds(15,55,e.getComponent().getWidth() - 45, e.getComponent().getHeight() - 120);
			frmResult.validate();
		}
		if(e.getSource() == frmClustersDocScores)
		{
			spClustersDocScores.setBounds(15,55,e.getComponent().getWidth() - 45, e.getComponent().getHeight() - 120);
			frmClustersDocScores.validate();
		}
		if(e.getSource() == frmClusters)
		{
			spClusters.setBounds(15,55,e.getComponent().getWidth() - 45, e.getComponent().getHeight() - 120);
			frmClusters.validate();
		}
		if(e.getSource() == frmRootPath)
		{
			btChoosefile.setBounds((e.getComponent().getWidth() / 2) - 100,50,200,40);	
			btProcess.setBounds(50,((e.getComponent().getHeight() - 160)),80,40);
			btSimilarity.setBounds((e.getComponent().getWidth() / 2) - 120,((e.getComponent().getHeight() - 160)),80,40);
			btCluster.setBounds((e.getComponent().getWidth() / 2) + 40,((e.getComponent().getHeight() - 160)),80,40);
			btHistogram.setBounds((e.getComponent().getWidth() - 130),((e.getComponent().getHeight() - 160)), 80,40);
			lblAlphaIr.setBounds((e.getComponent().getWidth() / 2) - 300, ((e.getComponent().getHeight() - 85)), 100, 10);
			txtFieldAlphaIr.setBounds((e.getComponent().getWidth() / 2) - 250, ((e.getComponent().getHeight() - 100)), 190, 40);
			lblAlgoChoice.setBounds((e.getComponent().getWidth() / 2) + 20, ((e.getComponent().getHeight() - 85)), 80, 10); 
			txtFieldAlgoChoice.setBounds((e.getComponent().getWidth() / 2) + 110, ((e.getComponent().getHeight() - 100)), 190, 40);
			spMessage.setBounds(50,110,e.getComponent().getWidth() - 100,e.getComponent().getHeight() - 110 - 180);
			frmRootPath.validate();
		}		
			
	}
	
	public void componentHidden(ComponentEvent e) {
       
    }

    public void componentMoved(ComponentEvent e) {
        
    }

    public void componentShown(ComponentEvent e) {
        
    }	

	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource()==btProcess)
		{
			docVectors.clear();
			docNormVectors.clear();
			docClusters.clear();
			v.clear();
			
			docMatrix = null;
			initialClusterMatrix = null;
			similarityMatrix = null;
			seedDocumentIndices = null;
			nr = null;
			Dr = null;
			Cr = null;
			D = null;
			C = null;
			numUniqueTerms = 0;
			finalClusters = null;
			similarityMatrixIv = null;
			nrIv = null;
			DrIv = null;
			CrIv = null;
			DIv = null;
			CIv = null;
			vIv.clear();
			nrKMeans = null;
			ni = null;
			nij_ir = null;
			nij_iv = null;
			nij_kmeans = null;
			fij_ir = null;
			fij_iv = null;
			fij_kmeans = null;
			fscore_ir = 0.0;
			fscore_iv = 0.0;
			fscore_kmeans = 0.0;
			nmi_ir = 0.0;
			nmi_iv = 0.0;
			nmi_kmeans = 0.0;
			accuracy_ir = 0.0;
			accuracy_iv = 0.0;
			accuracy_kmeans = 0.0;
			finalClustersIv = null;
			finalClustersKmeans = null;
			finalClustersKMeansCentroid = null;
			initialClusterMatrixCopy = null;
			currClusterKMeans = null;
			classDistribution = null;
			classNum = 0;
			totalWordCount = null;
			stopWords = null;
			classMap = null;
			btProcess.setText("Wait");
			btProcess.updateUI();
			processData="processlog";
			Similarities=new ItemsetCollection();
			parser1=new HTML_Parser();
			frontier=new Queue();
			visitedPages=new String[maxPages];
			nVisited = 0;
			nRun = nRun + 1;
			parseClassInformation();
			process();
			btProcess.setText("Done");
			btProcess.updateUI();
			frmResult.setVisible(true);
			txtMessage.append("Process Complete\n");
			showContentParsing();
			

		}

		if(evt.getSource()==btHistogram)
		{
			Hist=new ItemsetCollection();
			Histogram();
		}
		
		if(evt.getSource()==btSimilarity)
		{
			Similarity();
			frmClustersDocScores.setVisible(true);
			txtMessage.append("Similarity process Complete\n");
		}
		
		if(evt.getSource()==btCluster)
		{
			Clusters=new ItemsetCollection();
			clusterPath="clusters";
			Cluster();
			
			frmClusters.setVisible(true);
			txtMessage.append("Clustering complete\n");
		}
			
		if(evt.getSource()==btChoosefile)
		{
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				txtMessage.setText("");
				indexFile = fc.getSelectedFile();
				System.out.println(indexFile.getAbsolutePath());
				String fileName = indexFile.getAbsolutePath();
				int endIndex = fileName.lastIndexOf("index.");
				System.out.println(endIndex);
				dirName = fileName.substring(0, endIndex);
				System.out.println(dirName);
				//System.out.println(dirName.replaceAll("\\", "\\\\"));
                txtMessage.append("Selected: " + indexFile.getName() + ".\n");
				txtMessage.append("Please press Process to run the MVSC algorithm ....\n");
				txtMessage.append("Please press Similarity to see the MVSC scores ....\n");
				txtMessage.append("Please press Clusters to see the clusters formed ....\n");
				txtMessage.append("Please press Histogram to see the histogram of similarity measures ....\n");
				txtMessage.append("MVSC-IR alpha is 0.3 by default ....\n");
				txtMessage.append("Valid Algorithm Index Values:\n");
				txtMessage.append("1.MVSC-IR\n");
				txtMessage.append("2.MVSC-IV\n");
				btProcess.setText("Process");
			} 
			else 
			{
				txtMessage.setText("");
				txtMessage.append("Operation cancelled by user.\n");
				txtMessage.append("Please select a file to continue ....\n");
				btProcess.setText("Process");
			}
        }
		if(evt.getSource()==txtFieldAlphaIr)
		{
		    String txtAlphaVal = txtFieldAlphaIr.getText();
			alphaVal = Double.parseDouble(txtAlphaVal);
			txtMessage.append("IR Alpha Value: "+alphaVal+"\n");
			btProcess.setText("Process");
			btProcess.updateUI();			
		}
		if(evt.getSource()==txtFieldAlgoChoice)
		{
		    String txtAlgoChoice = txtFieldAlgoChoice.getText();
			algoChoice = Integer.parseInt(txtAlgoChoice);
			if(algoChoice != 1 && algoChoice != 2)
			{
				txtMessage.append("Algorithm Choice is in error. Allowed Values are: \n1.MVSC-IR \n2.MVSC-IV\n");
				algoChoice = 1;
			}
			else
			{
				txtMessage.append("Algorithm Choice: "+algoChoice+"\n");
			}
			btProcess.setText("Process");
			btProcess.updateUI();			
		}		
	}

	public void calculateFScoreIr()
	{
		fscore_ir = 0.0;
		
		for(int i = 0; i < classNum; i++)
		{
			double nValue = (double) ni[i] / (double) nDocuments;
			
			if(Double.isNaN(nValue))
				nValue = 0.0;
			
			double maxFValue = 0.0;
			
			for(int col = 0; col < classNum; col++)
			{
				if(fij_ir[i][col] > maxFValue)
					maxFValue = fij_ir[i][col];
			}
			
			fscore_ir = fscore_ir + (nValue * maxFValue);
		}
		//txtMessage.append("\nFScore for MVSC-IR: "+fscore_ir);
	}
	
	public void calculateFScoreIv()
	{
		fscore_iv = 0.0;
		
		for(int i = 0; i < classNum; i++)
		{
			double nValue = (double) ni[i] / (double) nDocuments;
			
			if(Double.isNaN(nValue))
				nValue = 0.0;
			
			double maxFValue = 0.0;
			
			for(int col = 0; col < classNum; col++)
			{
				if(fij_iv[i][col] > maxFValue)
					maxFValue = fij_iv[i][col];
			}
			
			fscore_iv = fscore_iv + (nValue * maxFValue);
		}
		//txtMessage.append("\nFScore for MVSC-IV: "+fscore_iv);
	}

	public void calculateFScoreKmeans()
	{
		fscore_kmeans = 0.0;
		
		for(int i = 0; i < classNum; i++)
		{
			double nValue = (double) ni[i] / (double) nDocuments;
			
			if(Double.isNaN(nValue))
				nValue = 0.0;
			
			double maxFValue = 0.0;
			
			for(int col = 0; col < classNum; col++)
			{
				if(fij_kmeans[i][col] > maxFValue)
					maxFValue = fij_kmeans[i][col];
			}
			
			fscore_kmeans = fscore_kmeans + (nValue * maxFValue);
		}
		//txtMessage.append("\nFScore for K-Means: "+fscore_kmeans);
	}	

	public void calculateNMIIr()
	{
		nmi_ir = 0.0;
		double numerator = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			for(int j = 0; j < classNum; j++)
			{
				if(j < finalClusters.length && nij_ir[i][j] != 0.0)
				{
					numerator = numerator + ((double)nij_ir[i][j] * Math.log10(((double)nDocuments * (double)nij_ir[i][j]) / ((double)ni[i] * (double)nr[j])));
				}
			}
		}
		double denom_first = 0.0, denom_second = 0.0, denominator = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			denom_first = denom_first + ((double)ni[i] * Math.log10((double)ni[i] / (double)nDocuments));
		}
		for(int i = 0; i < classNum; i++)
		{
			if(i < finalClusters.length)
				denom_second = denom_second + ((double)nr[i] * Math.log10((double)nr[i] / (double)nDocuments));
		}
		denominator = Math.sqrt(denom_first * denom_second);
		nmi_ir = numerator / denominator;
		//txtMessage.append("\nNMI for MVSC-IR: "+nmi_ir);
	}

		public void calculateNMIIv()
		{
		nmi_iv = 0.0;
		double numerator = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			for(int j = 0; j < classNum; j++)
			{
				if(j < finalClustersIv.length && nij_iv[i][j] != 0.0)
					numerator = numerator + ((double)nij_iv[i][j] * Math.log10(((double)nDocuments * (double)nij_iv[i][j]) / ((double)ni[i] * (double)nrIv[j])));
			}
		}
		double denom_first = 0.0, denom_second = 0.0, denominator = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			denom_first = denom_first + ((double)ni[i] * Math.log10((double)ni[i] / (double)nDocuments));
		}
		for(int i = 0; i < classNum; i++)
		{
			if(i < finalClustersIv.length)
				denom_second = denom_second + ((double)nrIv[i] * Math.log10((double)nrIv[i] / (double)nDocuments));
		}
		denominator = Math.sqrt(denom_first * denom_second);
		nmi_iv = numerator / denominator;
		//txtMessage.append("\nNMI for MVSC-IV: "+nmi_iv);
	}
	
	public void calculateNMIKmeans()
	{
		nmi_kmeans = 0.0;
		double numerator = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			for(int j = 0; j < classNum; j++)
			{
				if(j < finalClustersKmeans.length && nij_kmeans[i][j] != 0.0)
					numerator = numerator + ((double)nij_kmeans[i][j] * Math.log10(((double)nDocuments * (double)nij_kmeans[i][j]) / ((double)ni[i] * (double)nrKMeans[j])));
			}
		}
		double denom_first = 0.0, denom_second = 0.0, denominator = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			denom_first = denom_first + ((double)ni[i] * Math.log10((double)ni[i] / (double)nDocuments));
		}
		for(int i = 0; i < classNum; i++)
		{
			if(i < finalClustersKmeans.length)
				denom_second = denom_second + ((double)nrKMeans[i] * Math.log10((double)nrKMeans[i] / (double)nDocuments));
		}
		denominator = Math.sqrt(denom_first * denom_second);
		nmi_kmeans = numerator / denominator;
		//txtMessage.append("\nNMI for MVSC-IV: "+nmi_kmeans);
	}

	public void calculateAccuracyIr()
	{
		double[][] temp = new double[classNum][classNum];
		
		for(int row = 0; row < classNum; row++)
		{
			txtMessage.append("\n");
			for(int col = 0; col < classNum; col++)
			{
				temp[row][col] = (double) nij_ir[row][col];
				temp[row][col] = -1.0 * temp[row][col];
			}
		}
		HungarianAlgorithm ha = new HungarianAlgorithm(temp);
		
		int[] resultSet = ha.execute();
		
		accuracy_ir = 0.0;
		double tempSum = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			int k = resultSet[i];
			tempSum = tempSum + nij_ir[i][k];
		}
		accuracy_ir = tempSum * (1 / (double) nDocuments);
		//txtMessage.append("\nAccuracy for MVSC-Ir: "+accuracy_ir);
	}	
	
	public void calculateAccuracyIv()
	{
		double[][] temp = new double[classNum][classNum];
		
		for(int row = 0; row < classNum; row++)
		{
			for(int col = 0; col < classNum; col++)
			{
				temp[row][col] = (double) nij_iv[row][col];
				temp[row][col] = -1.0 * temp[row][col];
			}
		}
		HungarianAlgorithm ha = new HungarianAlgorithm(temp);
		
		int[] resultSet = ha.execute();
		
		accuracy_iv = 0.0;
		double tempSum = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			int k = resultSet[i];
			tempSum = tempSum + nij_iv[i][k];
		}
		accuracy_iv = tempSum * (1 / (double) nDocuments);
		//txtMessage.append("\nAccuracy for MVSC-Iv: "+accuracy_iv);
	}

	public void calculateAccuracyKmeans()
	{
		double[][] temp = new double[classNum][classNum];
		
		for(int row = 0; row < classNum; row++)
		{
			for(int col = 0; col < classNum; col++)
			{
				temp[row][col] = (double) nij_kmeans[row][col];
				temp[row][col] = -1.0 * temp[row][col];
			}
		}
		HungarianAlgorithm ha = new HungarianAlgorithm(temp);
		
		int[] resultSet = ha.execute();
		
		accuracy_kmeans	= 0.0;
		double tempSum = 0.0;
		for(int i = 0; i < classNum; i++)
		{
			int k = resultSet[i];
			tempSum = tempSum + nij_kmeans[i][k];
		}
		accuracy_kmeans = tempSum * (1 / (double) nDocuments);
		//txtMessage.append("\nAccuracy for K-Means: "+accuracy_kmeans);
	}
	
	// Retrieve the cluster in which a particular document falls
	public void getFinalClusterNumbersIr()
	{
		for(int row = 0; row < finalClusters.length; row++)
		{
			nr[row] = finalClusters[row].getSize();
		}
	}

	// Retrieve the cluster in which a particular document falls
	public void getFinalClusterNumbersIv()
	{
		for(int row = 0; row < finalClustersIv.length; row++)
		{
			nrIv[row] = finalClustersIv[row].getSize();
		}
	}

	// Retrieve the cluster in which a particular document falls
	public void getFinalClusterNumbersKmeans()
	{
		nrKMeans = new int[finalClustersKmeans.length];
		
		for(int row = 0; row < finalClustersKmeans.length; row++)
		{
			nrKMeans[row] = finalClustersKmeans[row].getSize();
		}
	}	
	
	public void populatenijir()
	{
		HashMap<Integer, Integer> clusterIrMap = new HashMap<Integer, Integer>();
		nij_ir = new int[classNum][classNum];
		fij_ir = new double[classNum][classNum];
		
		double pij, rij;
		
		for(int i = 0; i < nDocuments; i++)
			clusterIrMap.put(i, getFinalCluster(i));
		
		getFinalClusterNumbersIr();
		
		for(int row = 0; row < classNum; row++)
		{
			for(int col = 0; col < classNum; col++)
			{
				int docCount = 0;
				for(int doc = 0; doc < nDocuments; doc++)
				{
						if(clusterIrMap.get(doc) == col && classMap.get(doc) == row)
							docCount++;
				}
				nij_ir[row][col] = docCount;
				if(col > finalClusters.length - 1)
				{
					pij = 0.0;
				}
				else
				{
					pij = (double) nij_ir[row][col] / (double) nr[col];
				}
				rij = (double) nij_ir[row][col] / (double) ni[row];
				if(Double.isNaN(pij))
					pij = 0.0;
				if(Double.isNaN(rij))
					rij = 0.0;
				fij_ir[row][col] = (2 * pij * rij) / (pij + rij);
				if(Double.isNaN(fij_ir[row][col]))
					fij_ir[row][col] = 0.0;
			}
		}
		/*txtMessage.append("\nMVSC-Ir");
		for(int row = 0; row < classNum; row++)
		{
			txtMessage.append("\n");
			for(int col = 0; col < classNum; col++)
			{
				txtMessage.append(" "+nij_ir[row][col]);
			}
		}
		txtMessage.append("\nMVSC-Ir Fij");
		for(int row = 0; row < classNum; row++)
		{
			txtMessage.append("\n");
			for(int col = 0; col < classNum; col++)
			{
				txtMessage.append(" "+fij_ir[row][col]);
			}
		}*/		
		
	}

	public void populatenijiv()
	{
		HashMap<Integer, Integer> clusterIvMap = new HashMap<Integer, Integer>();
		nij_iv = new int[classNum][classNum];
		fij_iv = new double[classNum][classNum];
		
		double pij, rij;
		
		for(int i = 0; i < nDocuments; i++)
			clusterIvMap.put(i, getFinalClusterIv(i));
		
		getFinalClusterNumbersIv();
		
		for(int row = 0; row < classNum; row++)
		{
			for(int col = 0; col < classNum; col++)
			{
				int docCount = 0;
				for(int doc = 0; doc < nDocuments; doc++)
				{
						if(clusterIvMap.get(doc) == col && classMap.get(doc) == row)
							docCount++;
				}
				nij_iv[row][col] = docCount;
				if(col > finalClustersIv.length - 1)
				{
					pij = 0.0;
				}
				else
				{
					pij = (double) nij_iv[row][col] / (double) nrIv[col];
				}
				rij = (double) nij_iv[row][col] / (double) ni[row];
				//txtMessage.append("\nrow: "+row+" col: "+col+" nij: "+nij_iv[row][col]+" nrIv: "+nrIv[col]+" ni: "+ni[row]+" pij: "+pij+" rij: "+rij);
				if(Double.isNaN(pij))
					pij = 0.0;
				if(Double.isNaN(rij))
					rij = 0.0;
				fij_iv[row][col] = (2 * pij * rij) / (pij + rij);
				if(Double.isNaN(fij_iv[row][col]))
					fij_iv[row][col] = 0.0;				
			}
		}
		/*txtMessage.append("\nMVSC-Iv");
		for(int row = 0; row < classNum; row++)
		{
			txtMessage.append("\n");
			for(int col = 0; col < classNum; col++)
			{
				txtMessage.append(" "+nij_iv[row][col]);
			}
		}
		txtMessage.append("\nMVSC-Iv Fij");
		for(int row = 0; row < classNum; row++)
		{
			txtMessage.append("\n");
			for(int col = 0; col < classNum; col++)
			{
				txtMessage.append(" "+fij_iv[row][col]);
			}
		}*/		
	}

	public void populatenijkmeans()
	{
		HashMap<Integer, Integer> clusterKMeansMap = new HashMap<Integer, Integer>();
		nij_kmeans = new int[classNum][classNum];
		fij_kmeans = new double[classNum][classNum];
		
		double pij, rij;
		
		for(int i = 0; i < nDocuments; i++)
			clusterKMeansMap.put(i, getFinalClusterKMeans(i));
		
		getFinalClusterNumbersKmeans();	
		
		for(int row = 0; row < classNum; row++)
		{
			for(int col = 0; col < classNum; col++)
			{
				int docCount = 0;
				for(int doc = 0; doc < nDocuments; doc++)
				{
						if(clusterKMeansMap.get(doc) == col && classMap.get(doc) == row)
							docCount++;
				}
				nij_kmeans[row][col] = docCount;
				if(col > finalClustersKmeans.length - 1)
				{				
					pij = 0.0;
				}
				else
				{
					pij = (double) nij_kmeans[row][col] / (double) nrKMeans[col];
				}
				rij = (double) nij_kmeans[row][col] / (double) ni[row];
				if(Double.isNaN(pij))
					pij = 0.0;
				if(Double.isNaN(rij))
					rij = 0.0;
				fij_kmeans[row][col] = (2 * pij * rij) / (pij + rij);
				if(Double.isNaN(fij_kmeans[row][col]))
					fij_kmeans[row][col] = 0.0;					
			}
		}
		/*txtMessage.append("\n K-Means");
		for(int row = 0; row < classNum; row++)
		{
			txtMessage.append("\n");
			for(int col = 0; col < classNum; col++)
			{
				txtMessage.append(" "+nij_kmeans[row][col]);
			}
		}
		txtMessage.append("\nKMeans Fij");
		for(int row = 0; row < classNum; row++)
		{
			txtMessage.append("\n");
			for(int col = 0; col < classNum; col++)
			{
				txtMessage.append(" "+fij_kmeans[row][col]);
			}
		}*/	
	}
	
	public void parseClassInformation()
	{
		try
		{
			Scanner fileName = new Scanner(new File(dirName+"classInfo.txt"));
			int LineCount = 0, clusterIndex = 0;
			
			classMap = new HashMap<Integer, Integer>();
			
			while(fileName.hasNext())  
			{	// if file is not empty yet
				String line=fileName.nextLine().trim();
				if(LineCount == 0)
				{
					classNum = Integer.parseInt(line);
					classDistribution = new GVector[classNum];
					ni = new int[classNum];
					LineCount = 1;
				}
				else
				{
					String[] clusterDocNumArray = line.split(",");
					classDistribution[clusterIndex] = new GVector(clusterDocNumArray.length);
					for(int i = 0; i < clusterDocNumArray.length; i++)
					{
						classMap.put(Integer.parseInt(clusterDocNumArray[i].trim()), clusterIndex);
						classDistribution[clusterIndex].setElement(i, (double)Integer.parseInt(clusterDocNumArray[i].trim()));
					}
					ni[clusterIndex] = clusterDocNumArray.length;
					clusterIndex++;
				}
			}
			/*for(Integer a: classMap.keySet())
				txtMessage.append("\nDocNum "+a+" ClusterNum "+classMap.get(a));*/
		}
		catch(Exception e)
		{
			txtMessage.append("\nError reading file: "+e);
		}
	}
	
	public String doStemmer(String inputWord)
	{
		String testString = inputWord;
		char[] testArray = new char[testString.length()];
		char[] outputArray = new char[testString.length()];
		int outputIndex = 0, stemFlag = 0;
		testString = testString.toLowerCase();
		testArray = testString.toCharArray();
		for(int k = 0; k < testString.length(); k++)
		{
			if(Character.isLetter(testArray[k]))
			{
				stemFlag = 1;
				s.add(testArray[k]);
			}
			else
			{	
				if(stemFlag == 1)
				{
					s.stem();
					String temp = s.toString();
					char[] tempArray = temp.toCharArray();
					for(int index = 0; index < temp.length(); index++)
					{
						if(Character.isLetter(tempArray[index]))
							outputArray[outputIndex++] = tempArray[index];
					}
					stemFlag = 0;
				}
				outputArray[outputIndex++] = testArray[k];
			}
			//txtMessage.append("\nAdded char: "+testArray[k]);
		}
		if(stemFlag == 1)
		{
			s.stem();
			String temp = s.toString();
			char[] tempArray = temp.toCharArray();
			for(int index = 0; index < temp.length(); index++)
			{
				outputArray[outputIndex++] = tempArray[index];
			}
			stemFlag = 0;		
		}
		String outputString = new String(outputArray);
		//txtMessage.append("\nTest String: "+testString);
		//txtMessage.append("\nOutput String: "+outputString);		
		return outputString.trim();
	}
	
	public int findOccurenceSubstring(String mainString, String searchString)
	{
		int lastIndex = 0;
		int count =0;

		while(lastIndex != -1)
		{

			   lastIndex = mainString.indexOf(searchString,lastIndex);

			   if( lastIndex != -1){
					 count ++;
					 lastIndex+=searchString.length();
			  }
		}
		return count;
	}
	
	public void process()
	{
		try
		{
			String timeStamp_now = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			processData = "Process_Log_run_" + nRun + "_" + timeStamp_now + ".txt";
			FileOutputStream foutlog=new FileOutputStream(processData);
			//starting-urls
			
			getStopWords();
			
			String tRootPath = indexFile.getName();
			frontier.enqueue(tRootPath);

			//breadth-first-search
			nVisited=0;
			txtResult.setText("");
			
			while(nVisited<maxPages&&frontier.isEmpty()==false)
			{
				String tstrFrontier=frontier.toString();
				String tPath=frontier.dequeue();
				if(isVisitedPage(tPath)==false)
				{
					addVisitedPage(tPath);
					//parser1.setFilePath("data\\"+tPath);
					parser1.setFilePath(dirName+tPath);
					Queue q=parser1.findLinks();

					while(q.isEmpty()==false)
					{
						String tPath1=q.dequeue();
						if(isVisitedPage(tPath1)==false) frontier.enqueue(tPath1);
					}
				}
			}
			
			//construct webdocuments and its DIG
			nDocuments=nVisited;
			documents=new WebDocument[nDocuments];
			CumulativeDocument=new WebDocument();

			//find metas and construct cumulative document index graph
			addResultText(" Document index and names:\n\n");
			ItemsetCollection icWords=new ItemsetCollection();
			ItemsetCollection icEdges=new ItemsetCollection();
			
			for(int t=0;t<nDocuments;t++) //for each document
			{
				documents[t]=new WebDocument();
				documents[t].setDocName(visitedPages[t]);
				//parser1.setFilePath("data\\"+visitedPages[t]);
				parser1.setFilePath(dirName+visitedPages[t]);
				
				Queue q=parser1.findMetas(); //get meta-data
				String tstr=q.toString();
				tstr=StringUtils.replaceString(tstr,",","");
				tstr=StringUtils.replaceString(tstr,"{","");
				tstr=StringUtils.replaceString(tstr,"}","");
				
				//get unique words in this document
				String tarr[]=StringUtils.split(tstr," ");
				
				logText = "\n tarr: ";
				foutlog.write(logText.getBytes());
				int stopWordCount = 0;
				for(String s: tarr)
				{
					if(findOccurenceSubstring(stopWords, s) > 0)
					{
						stopWordCount++;
					}
					logText = " "+s;
					foutlog.write(logText.getBytes());
				}
				logText = "\n Stop Words Count : "+stopWordCount;
				foutlog.write(logText.getBytes());				
				String stopWordTarr[] = new String[tarr.length-stopWordCount];
				int arrayIndex = 0;
				logText = "\n Stop Words Removed tarr: \n";
				foutlog.write(logText.getBytes());				
				for(String s: tarr)
				{
					if(findOccurenceSubstring(stopWords, s) == 0)
					{
						stopWordTarr[arrayIndex++] = doStemmer(s);
						logText = " "+stopWordTarr[arrayIndex-1];
						foutlog.write(logText.getBytes());						
					}
				}				
				Itemset tItemset=new Itemset(stopWordTarr);
				ItemsetCollection ic1=new ItemsetCollection(tItemset);
				tItemset=ic1.getUniqueItemset();
				
				simalpha=0.3;

				icWords.addItemset(tItemset);
				documents[t].DIG.setV(tItemset);
				
				//get unique edges in this document
				tstr=q.toString();
				tstr=StringUtils.replaceString(tstr,"{","");
				tstr=StringUtils.replaceString(tstr,"}","");
				tarr=StringUtils.split(tstr,", ");
				logText = "\n tarr edges: ";
				foutlog.write(logText.getBytes());
				int stopWordEdgesCount = 0;
				for(String s: tarr)
				{
					if(findOccurenceSubstring(stopWords, s) > 0)
					{
						stopWordEdgesCount++;
					}
					logText = " "+s;
					foutlog.write(logText.getBytes());
				}
				String stopWordTarrEdges[] = new String[tarr.length-stopWordEdgesCount];
				arrayIndex = 0;
				logText = "\n Stop Word Removed Edges: \n";
				foutlog.write(logText.getBytes());				
				for(String s: tarr)
				{
					if(findOccurenceSubstring(stopWords, s) == 0)
					{
						stopWordTarrEdges[arrayIndex++] = doStemmer(s);
						logText = " "+stopWordTarrEdges[arrayIndex-1];
						foutlog.write(logText.getBytes());						
					}
				}				
				for(int j=0;j<stopWordTarrEdges.length;j++)
				{
					documents[t].addPhrase(stopWordTarrEdges[j]);
					CumulativeDocument.addPhrase(stopWordTarrEdges[j]);
					String[] tarr1=StringUtils.split(stopWordTarrEdges[j]," ");
					if(stopWordTarrEdges.length>1)
					{
						for(int k=0;k<=stopWordTarrEdges.length-2;k++)
						{
								Itemset i1=new Itemset(); //if word-(k+1) appears next to word-k
								i1.addItem(stopWordTarrEdges[k]);
								i1.addItem(stopWordTarrEdges[k+1]);
								icEdges.addItemset(i1);
								documents[t].DIG.addEdge(i1);
						}
					}
				}
			}
			
			//set graph nodes and edges
			for(int t=0;t<nDocuments;t++)
			{
				ItemsetCollection ic1=new ItemsetCollection();
				ic1=documents[t].DIG.getE();
				documents[t].DIG.setE(ic1.getUniqueItemsetCollection());
			}
			CumulativeDocument.DIG.setV(icWords.getUniqueItemset());
			CumulativeDocument.DIG.setE(icEdges.getUniqueItemsetCollection());
			
     		//show each document phrases and dig
			for(int t=0;t<nDocuments;t++)
			{
				addResultText(" Document"+t+"  : "+documents[t].getDocName()+"\n");
				addResultText(" Phrases:\n"+documents[t].getPhrases().toString()+"\n");
				addResultText(" Nodes:\n"+documents[t].getDIG().getV().toString()+"\n");
				addResultText(" Edges:\n"+documents[t].getDIG().getE().toString1()+"\n");
				logText = " Document"+t+"  : "+documents[t].getDocName()+"\n";
				foutlog.write(logText.getBytes());
				logText = " Phrases:\n"+documents[t].getPhrases().toString()+"\n";
				foutlog.write(logText.getBytes());
				logText = " Nodes:\n"+documents[t].getDIG().getV().toString()+"\n";
				foutlog.write(logText.getBytes());
				logText = " Edges:\n"+documents[t].getDIG().getE().toString1()+"\n";
				foutlog.write(logText.getBytes());
			}
			
			addResultText("\n Cumulative DIG:\n");
			addResultText(" Phrases:\n"+CumulativeDocument.getPhrases().toString()+"\n");
			addResultText(" Nodes:\n"+CumulativeDocument.DIG.getV().toString()+"\n");
			addResultText(" Edges:\n"+CumulativeDocument.DIG.getE().toString1());
			logText = "\n Cumulative DIG:\n";
			foutlog.write(logText.getBytes());
			logText = " Phrases:\n"+CumulativeDocument.getPhrases().toString()+"\n";
			foutlog.write(logText.getBytes());
			logText = " Nodes:\n"+CumulativeDocument.DIG.getV().toString()+"\n";
			foutlog.write(logText.getBytes());
			logText = " Edges:\n"+CumulativeDocument.DIG.getE().toString1()+"\n";
			foutlog.write(logText.getBytes());			
			foutlog.close();
		}
		catch(IOException e)
		{
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");
		}
}
			
	//display histogram
	public void Histogram()
	{
		try
		{
			double[][][] histArray = new double[nDocuments][nDocuments][1];
			for(int row = 0; row < similarityMatrix.getNumRow(); row++)
			{
				for(int col = 0; col < similarityMatrix.getNumCol(); col++)
				{
					histArray[row][col][0] = similarityMatrix.getElement(row, col) * 100.0;
				}
			}
			
			for(int i=0;i<nDocuments;i++)
			{
				Histogram hist = new Histogram("Document "+i+" Similiarity",histArray[i]);
			}
		}
		catch(Exception e)
		{
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");
		}
	}
	
	//display similarity
	public void Similarity()
	{
		try
		{
			txtClustersDocScores.setText("");
			String timeStamp_now = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			similarityPath = "Similarity_Log_run_" + nRun + "_" + timeStamp_now + ".txt";
			FileOutputStream foutlog_similarity=new FileOutputStream(similarityPath);
			
			addClusterDocumentScoreText("\n Pairwise Document MVS Similarity Scores based on clusters formed using MVSC-IR\n");
			logText = "\n Pairwise Document MVS Similarity Scores based on clusters formed using MVSC-IR\n";
			foutlog_similarity.write(logText.getBytes());			
			
			for(int row = 0; row < similarityMatrix.getNumRow(); row++)
			{
				for(int col = 0; col < similarityMatrix.getNumCol(); col++)
				{
					addClusterDocumentScoreText("\n Document Pair("+row+","+col+") \t MVS Similarity Score: "+similarityMatrix.getElement(row, col)+"\n");
					logText = "\n Document Pair("+row+","+col+") \t MVS Similarity Score: "+similarityMatrix.getElement(row, col)+"\n";
					foutlog_similarity.write(logText.getBytes());					
					logText = " "+similarityMatrix.getElement(row, col);
					foutlog_similarity.write(logText.getBytes());
				}
				foutlog_similarity.write(logText.getBytes());
			}
			foutlog_similarity.write(logText.getBytes());
			
			addClusterDocumentScoreText("\n\n Pairwise Document MVS Similarity Scores based on clusters formed using MVSC-IV\n");
			logText = "\n\n Pairwise Document MVS Similarity Scores based on clusters formed using MVSC-IV\n";
			foutlog_similarity.write(logText.getBytes());			
			
			for(int row = 0; row < similarityMatrixIv.getNumRow(); row++)
			{
				for(int col = 0; col < similarityMatrixIv.getNumCol(); col++)
				{
					addClusterDocumentScoreText("\n Document Pair("+row+","+col+") \t MVS Similarity Score: "+similarityMatrixIv.getElement(row, col)+"\n");
					logText = "\n Document Pair("+row+","+col+") \t MVS Similarity Score: "+similarityMatrixIv.getElement(row, col)+"\n";
					foutlog_similarity.write(logText.getBytes());
					logText = " "+similarityMatrixIv.getElement(row, col);
					foutlog_similarity.write(logText.getBytes());
				}
				foutlog_similarity.write(logText.getBytes());
			}
			foutlog_similarity.write(logText.getBytes());	
			
			foutlog_similarity.close();
		}
		catch(Exception e)
		{
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");
		}
	}
	
	// Retrieve the cluster in which a particular document falls
	public int getFinalCluster(int docIndex)
	{
		for(int row = 0; row < finalClusters.length; row++)
		{
			for(int col = 0; col < finalClusters[row].getSize(); col++)
			{
				if((int)finalClusters[row].getElement(col) == docIndex)
				{
					return row;
				}
			}
		}
		return -1;
	}
	
	// MVS Similarity scores for all document pairs
	public int similarityMatrix(String logFileName)
	{
		lock_var = 1;
		txtClustersDocScores.setText("");
		
		similarityMatrix = new GMatrix(nDocuments, nDocuments);
		similarityMatrix.setZero();
		
		GVector[] Dvalues = new GVector[finalClusters.length];
		for(int j = 0; j < finalClusters.length; j++)
		{
			Dvalues[j] = new GVector(numUniqueTerms);
			Dvalues[j].zero();
		}
		
		try
		{
			FileOutputStream foutlog=new FileOutputStream(logFileName, true);
			logText = "\nSimilarity Matrix Generation";
			//foutlog.write(logText.getBytes());
			logText = "\nD Vector Generation";
			//foutlog.write(logText.getBytes());
			for(int j = 0; j < finalClusters.length; j++)
			{
				logText = "\nD(S/S"+j+") Vector Generation";
				//foutlog.write(logText.getBytes());
				for(int k = 0; k < finalClusters.length; k++)
				{
					if(j != k)
					{
						for(int l = 0; l < finalClusters[k].getSize(); l++)
						{
							logText = "\n\tAdding doc index "+finalClusters[k].getElement(l)+" from cluster "+k;
							//foutlog.write(logText.getBytes());
							Dvalues[j].add(docNormVectors.get((int)finalClusters[k].getElement(l)));
							logText = "\n\tAdded Vector: "+docNormVectors.get((int)finalClusters[k].getElement(l)).toString();
							//foutlog.write(logText.getBytes());
							logText = "\n\tResult Vector: "+Dvalues[j].toString();
							//foutlog.write(logText.getBytes());
						}
					}
				}
			}
			
			for(int j = 0; j < nDocuments; j++)
			{
				for(int k = 0; k < nDocuments; k++)
				{
					int docjCluster = getFinalCluster(j);
					int dockCluster = getFinalCluster(k);
					
					double dotProductFirst = docNormVectors.get(j).dot(docNormVectors.get(k));
					int nValue = 0;
					
					if(docjCluster == dockCluster)
						nValue = nDocuments - nr[docjCluster];
					else
						nValue = nDocuments - nr[docjCluster] - 1;
						
					GVector tempVector = new GVector(numUniqueTerms);
					tempVector.zero();
					
					if(docjCluster == dockCluster)
						tempVector.add(Dvalues[docjCluster]);
					else
					{
						tempVector.add(Dvalues[docjCluster]);
						tempVector.sub(docNormVectors.get(k));
					}
					
					GVector tempC = new GVector(numUniqueTerms);
					for(int b = 0; b < tempVector.getSize(); b++)
					{
						double tempValue = tempVector.getElement(b) / nValue;
						tempC.setElement(b, tempValue);
					}
					
					double dotProductSecond = docNormVectors.get(j).dot(tempC);
					double dotProductThird = docNormVectors.get(k).dot(tempC);
					
					double similarityValue = dotProductFirst - dotProductSecond - dotProductThird + 1;
					
					similarityMatrix.setElement(j, k, similarityValue);
				}
			}
			
			logText = "\nSimilarity Matrix: \n";
			foutlog.write(logText.getBytes());
			for(int row = 0; row < similarityMatrix.getNumRow(); row++)
			{
				for(int col = 0; col < similarityMatrix.getNumCol(); col++)
				{
					logText = " "+similarityMatrix.getElement(row, col);
					foutlog.write(logText.getBytes());
				}
				logText = "\n";
				foutlog.write(logText.getBytes());
			}
			lock_var = 0;
			
			
		}
		catch(Exception e)
		{
			lock_var = 0;
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");		
		}
		return 1;
	}
	// Finding the stuff parsed in each document
	public void showContentParsing()
	{
		try
		{
			String timeStamp_now = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			parsePath = "Parse_Log_run_" + nRun + "_" + timeStamp_now + ".txt";
			FileOutputStream foutlog=new FileOutputStream(parsePath);
			
			String[] allTerms = null, uniqueTerms = null;
			int allTermsIndex = 0, numOfTerms = 0, uniqueIndex = 0;
			numUniqueTerms = 0;
			
			for(int t=0;t<=nDocuments-1;t++)
			{
				for(int k=0;k<documents[t].DIG.V.get_nItems();k++)
				{
					numOfTerms = numOfTerms + 1;
				}
			}
			
			allTerms = new String[numOfTerms];

			for(int t=0;t<=nDocuments-1;t++)
			{		
				for(int k=0;k<documents[t].DIG.V.get_nItems();k++)
				{		
					allTerms[allTermsIndex++] = documents[t].DIG.V.getItem(k);
				}	
			}

			uniqueTerms = new HashSet<String>(Arrays.asList(allTerms)).toArray(new String[allTerms.length]);
			
			for(String u: uniqueTerms)
			{
				if(u != null)
					numUniqueTerms++;
			}
			
			//txtMessage.append("Unique Terms: "+numUniqueTerms);
			totalWordCount = new int[nDocuments];
			int totalCount = 0;
			
			for(int t=0;t<=nDocuments-1;t++)
			{
				totalCount = 0;
				for(String u: uniqueTerms)
				{
					if(u != null)
					{
						totalCount+=getCountOfWord(u,documents[t],t);
						
					
					}
				}
				totalWordCount[t] = totalCount;
				logText = "\n Total Words " + t + " -> Count: " + totalWordCount[t];
				foutlog.write(logText.getBytes());
			}
			
			docMatrix = new GMatrix(nDocuments, numUniqueTerms);
			
			for(int t=0;t<=nDocuments-1;t++)
			{
				logText = "\n Document Number: "+(t);
				foutlog.write(logText.getBytes());		
				for(String u: uniqueTerms)
				{
					if(u != null)
					{
						double tfidf1=findTFIDF(u,documents[t],t);
						docMatrix.setElement(t, uniqueIndex++, tfidf1);
						logText = "\n Term: " + u + " -> tfidf: " + tfidf1;
						foutlog.write(logText.getBytes());					
					}
				}
				logText = "\n ----------------------------------------------------------------";
				foutlog.write(logText.getBytes());
				uniqueIndex = 0;
				
			}
			
			double[] tempArray = new double[numUniqueTerms];
			for(int t=0;t<=nDocuments-1;t++)
			{
				docMatrix.getRow(t, tempArray);
				GVector tempVector = new GVector(numUniqueTerms);
				tempVector.set(tempArray);
				docVectors.add(tempVector);
			}
			
			logText = "\n All Terms in all documents: ";
			foutlog.write(logText.getBytes());
			for(String a: allTerms)
			{
				logText = " "+a;
				foutlog.write(logText.getBytes());
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());		
			logText = "\n Unique Terms in all documents: ";
			foutlog.write(logText.getBytes());
			uniqueTerms = new HashSet<String>(Arrays.asList(allTerms)).toArray(new String[allTerms.length]);
			for(String u: uniqueTerms)
			{
				logText = " "+u;
				foutlog.write(logText.getBytes());
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			logText = "\n Document Matrix: \n";
			foutlog.write(logText.getBytes());				
			for(int t=0;t<=nDocuments-1;t++)
			{	
				for(int k=0;k<=numUniqueTerms-1;k++)
				{	
					logText = "\t"+docMatrix.getElement(t, k);
					foutlog.write(logText.getBytes());
				}
				logText = "\n";
				foutlog.write(logText.getBytes());
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			logText = "\n Document Vectors: \n";
			foutlog.write(logText.getBytes());				
			for(int t=0;t<=docVectors.size()-1;t++)
			{	
				logText = "Doc: "+(t)+" Contents: "+docVectors.get(t).toString();
				//foutlog.write(logText.getBytes());
				logText = "\n Norm of Vector = "+docVectors.get(t).norm();
				GVector tempVector = docVectors.get(t);
				tempVector.normalize(tempVector);
				docNormVectors.add(tempVector);
				//foutlog.write(logText.getBytes());
				logText = "\n Normalized Vector = "+docNormVectors.get(t).toString();
				foutlog.write(logText.getBytes());
				logText = "\n Norm of Vector = "+docNormVectors.get(t).norm();
				foutlog.write(logText.getBytes());
				logText = "\n";
				foutlog.write(logText.getBytes());
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			logText = "\n Initial Cluster Matrix: ";
			foutlog.write(logText.getBytes());	
			

			while(formInitialClusters() != 1)
			{
			}

			
			initialClusterMatrixCopy = new GMatrix(initialClusterMatrix);
			for(int t = 0; t < initialClusterMatrix.getNumRow(); t++)
			{
				logText = "\n Cluster: "+(t)+" Values: ";
				foutlog.write(logText.getBytes());
				for(int k = 0; k < initialClusterMatrix.getNumCol(); k++)
				{
					logText = " "+initialClusterMatrix.getElement(t, k);
					foutlog.write(logText.getBytes());
				}
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			logText = "\n Cluster Counts: ";
			foutlog.write(logText.getBytes());
			for(int t = 0; t < nr.length; t++)
			{
				logText = "\nCluster : "+(t)+" Count: "+nr[t];
				foutlog.write(logText.getBytes());
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			logText = "\n Composite Vectors: ";
			foutlog.write(logText.getBytes());
			for(int t = 0; t < seedDocumentIndices.length; t++)
			{
				logText = "\nCluster : "+(t)+" Composite Vector: "+Dr[t].toString();
				foutlog.write(logText.getBytes());
			}	
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			logText = "\n Value of D and C: ";
			foutlog.write(logText.getBytes());
			logText = "\nValue of D ["+D.toString()+"]";
			foutlog.write(logText.getBytes());			
			logText = "\nValue of C ["+C.toString()+"]";
			foutlog.write(logText.getBytes());
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());	
			algoChoice = 1;

			while(performRefinementStep(parsePath) != 1)
			{
			}

			

			while(similarityMatrix(parsePath) != 1)
			{
			}
			
			populatenijir();

			
			DrIv = null;
			CrIv = null;
			DIv = null;
			CIv = null;
			vIv.clear();
			
			/*initialClusterMatrix.setZero();
			initialClusterMatrix.add(initialClusterMatrixCopy);*/
			
			calculateNrDrDIv();
			
			String timeStamp_nowIv = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			String parsePathIv = "Parse_Log_Iv_run_" + nRun + "_" + timeStamp_nowIv + ".txt";
			FileOutputStream foutlogIv=new FileOutputStream(parsePathIv);	
			
			algoChoice = 2;
/*			logText = "\n ----------------------------------------------------------------";
			foutlogIv.write(logText.getBytes());
			logText = "\n Clustering Using MVSC-IV ";
			foutlogIv.write(logText.getBytes());				
			logText = "\n Initial Cluster Matrix: ";
			foutlogIv.write(logText.getBytes());		
			for(int t = 0; t < initialClusterMatrixCopy.getNumRow(); t++)
			{
				logText = "\n Cluster: "+(t)+" Values: ";
				foutlogIv.write(logText.getBytes());
				for(int k = 0; k < initialClusterMatrixCopy.getNumCol(); k++)
				{
					logText = " "+initialClusterMatrixCopy.getElement(t, k);
					foutlogIv.write(logText.getBytes());
				}
			}
			logText = "\n ----------------------------------------------------------------";
			foutlogIv.write(logText.getBytes());				
			logText = "\n Cluster Counts: ";
			foutlogIv.write(logText.getBytes());
			for(int t = 0; t < nrIv.length; t++)
			{
				logText = "\nCluster : "+(t)+" Count: "+nrIv[t];
				foutlogIv.write(logText.getBytes());
			}
			logText = "\n ----------------------------------------------------------------";
			foutlogIv.write(logText.getBytes());				
			logText = "\n Composite Vectors: ";
			foutlogIv.write(logText.getBytes());
			for(int t = 0; t < seedDocumentIndices.length; t++)
			{
				logText = "\nCluster : "+(t)+" Composite Vector: "+DrIv[t].toString();
				foutlogIv.write(logText.getBytes());
			}	
			logText = "\n ----------------------------------------------------------------";
			foutlogIv.write(logText.getBytes());				
			logText = "\n Value of D and C: ";
			foutlogIv.write(logText.getBytes());
			logText = "\nValue of D ["+DIv.toString()+"]";
			foutlogIv.write(logText.getBytes());			
			logText = "\nValue of C ["+CIv.toString()+"]";
			foutlogIv.write(logText.getBytes());
			logText = "\n ----------------------------------------------------------------";
			foutlogIv.write(logText.getBytes());	
			*/
			
			while(performRefinementStepIv(parsePathIv) != 1)
			{
			}

			

			while(similarityMatrixIv(parsePathIv) != 1)
			{
			}
			
			populatenijiv();
			
			/*String timeStamp_nowKMeans = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			String KMeansPath = "K-Means_Run_" + nRun + "_" + timeStamp_nowKMeans + ".txt";
			FileOutputStream foutlogKMeans=new FileOutputStream(KMeansPath);

			logText = "\n ----------------------------------------------------------------";
			foutlogKMeans.write(logText.getBytes());				
			logText = "\n K-Means Algorithm Run";
			foutlogKMeans.write(logText.getBytes());*/
			//
			
			doKMeans(classNum);
			populatenijkmeans();
			
			calculateFScoreIr();
			calculateNMIIr();
			calculateAccuracyIr();
			
			calculateFScoreIv();
			calculateNMIIv();			
			calculateAccuracyIv();			
			
			calculateFScoreKmeans();
			calculateNMIKmeans();
			calculateAccuracyKmeans();
			
			String timeStamp_nowMeasures = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			String measuresPath = "Measures_Log_run_" + nRun + "_" + timeStamp_nowMeasures + ".txt";
			FileOutputStream foutlogMeasures=new FileOutputStream(measuresPath);		

			logText = "\nMeasures for the Algorithms";
			foutlogMeasures.write(logText.getBytes());
			logText = "\n\nFScore measures";
			foutlogMeasures.write(logText.getBytes());	
			logText = "\n---------------";
			foutlogMeasures.write(logText.getBytes());			
			logText = "\nMVSCIr: "+fscore_ir+" MVSCIv: "+fscore_iv+" K-Means: "+fscore_kmeans;
			foutlogMeasures.write(logText.getBytes());
			logText = "\n\nNMI measures";
			foutlogMeasures.write(logText.getBytes());	
			logText = "\n------------";
			foutlogMeasures.write(logText.getBytes());			
			logText = "\nMVSCIr: "+nmi_ir+" MVSCIv: "+nmi_iv+" K-Means: "+nmi_kmeans;
			foutlogMeasures.write(logText.getBytes());	
			logText = "\n\nAccuracy measures";
			foutlogMeasures.write(logText.getBytes());	
			logText = "\n-----------------";
			foutlogMeasures.write(logText.getBytes());			
			logText = "\nMVSCIr: "+accuracy_ir+" MVSCIv: "+accuracy_iv+" K-Means: "+accuracy_kmeans;
			foutlogMeasures.write(logText.getBytes());	
			foutlogMeasures.close();
			
			foutlogIv.close();
			foutlog.close();
		}
		catch(Exception e)
		{
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");		
		}
		
	}
	
	// Used to generate the random numbers to form the seeds in the Initialization step
	public int randInt(int min, int max) 
	{
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}	
	
	// Randomly pick k documents to form clusters around
	// k is set to be three quarters of the number of documents under process
	// This is part of the Initialization process
	public void setInitialClusterIndices()
	{
		int initialNumOfClusters = classNum;
		
		seedDocumentIndices = new int[initialNumOfClusters];
		int seedDocumentIndicesIndex = 0;
		
		for(int k = 0; k < initialNumOfClusters; k++)
		{
			seedDocumentIndices[seedDocumentIndicesIndex++] = randInt(0, nDocuments-1);
		}
		//txtMessage.append("Initial Cluster Indices Done");
	}

	//Getting cluster value for a given document in the Initialization step
	public int getClusterNumber(GVector documentUnderTest)
	{
		double dotProductValue = 0.0, prev = 0.0;
		int max = 0;
		
		for(int k = 0; k < seedDocumentIndices.length; k++)
		{
			dotProductValue = docNormVectors.get(seedDocumentIndices[k]).dot(documentUnderTest);
			if(prev <= dotProductValue)
			{
				prev = dotProductValue;
				max = k;
			}
		}
		return max;
	}
	
	// Calculate the Dr, Nr, and D values along with Cr and C
	public void calculateNrDrD()
	{
		nr = new int[seedDocumentIndices.length];
		Dr = new GVector[seedDocumentIndices.length];
		Cr = new GVector[seedDocumentIndices.length];
		
		calculateNr();
		calculateDr();
		calculateD();	
	}
	
	//Forming initial clusters as part of the Initialization step
	public int formInitialClusters()
	{
		lock_var = 1;
		setInitialClusterIndices();
		
		initialClusterMatrix = new GMatrix(seedDocumentIndices.length, nDocuments);
		initialClusterMatrix.setZero();
		
		for(int j = 0; j < nDocuments; j++)
		{
			//txtMessage.append("\nStarting getClusterNumber for document "+j);
			int rowVal = getClusterNumber(docNormVectors.get(j));
			initialClusterMatrix.setElement(rowVal, j, 1.0);
			//txtMessage.append("\nDone getClusterNumber for document "+j+" Cluster Assigned: "+rowVal);
		}
		
		calculateNrDrD();
		lock_var = 0;
		
		return 1;
	}
	
	//Calculating the number of documents per cluster nr
	public void calculateNr()
	{
		int nrIndex = 0;
		for(int row = 0; row < initialClusterMatrix.getNumRow(); row++)
		{
			int clusterCount = 0;
			for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
			{
				if(initialClusterMatrix.getElement(row, col) == 1.0)
				{
					clusterCount++;
				}
			}
			nr[nrIndex++] = clusterCount;
		}
	}
	
	// Calculating composite vector of documents in a cluster Dr
	public void calculateDr()
	{
		//txtMessage.append("\nIn Calculate Dr");
		for(int row = 0; row < initialClusterMatrix.getNumRow(); row++)
		{
			Dr[row] = new GVector(numUniqueTerms);
			Dr[row].zero();
			for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
			{
				if(initialClusterMatrix.getElement(row, col) == 1.0)
				{
					Dr[row].add(docNormVectors.get(col));
				}
			}
			Cr[row] = new GVector(numUniqueTerms);
			Cr[row].zero();
			if(nr[row] != 0)
			{
				Cr[row].add(Dr[row]);
				for(int b = 0; b < Dr[row].getSize(); b++)
				{
					double tempValue = Dr[row].getElement(b) / nr[row];
					Cr[row].setElement(b, tempValue);
				}
			}
			else
			{
				Cr[row].zero();
			}
			//txtMessage.append("\nAfter Dr-> row = "+row+" Dr: "+Dr[row].toString());
			//txtMessage.append("\nAfter Cr-> row = "+row+" Cr: "+Cr[row].toString());
		}
	}
	
	// Calculating composite vector of documents in a cluster Dr
	public void calculateD()
	{
		//txtMessage.append("\nIn Calculate D");
		D = new GVector(numUniqueTerms);
		D.zero();
		for(int k = 0; k < nDocuments; k++)
		{
			D.add(docNormVectors.get(k));
		}
		C = new GVector(numUniqueTerms);
		C.zero();
		C.add(D);
		for(int b = 0; b < D.getSize(); b++)
		{
			double tempValue = D.getElement(b) / nDocuments;
			C.setElement(b, tempValue);
		}
		//txtMessage.append("\nD: "+D.toString());
		//txtMessage.append("\nC: "+C.toString());		
	}
	
	// Creating v list with document indices
	public void createV()
	{
		for(int j = 0; j < nDocuments; j++)
		{
			v.add(j);
		}
	}
	
	// Generating random permutation of v for Refinement step
	public void generatePermutationv()
	{
		java.util.Collections.shuffle(v);
	}
	
	// Composite Value of a vector calculated using Ir formula
	public double calculateIusingIr(int nFactor, GVector vectorFactor)
	{
		double nFactorToOneMinusAlpha = Math.pow((double)nFactor, (1.0 - alphaVal));
		double reciprocalVal = 1 / nFactorToOneMinusAlpha;
		
		double factorOne = (nDocuments + nFactor) / (nDocuments - nFactor);
		double squaredNorm = Math.pow(vectorFactor.norm(), 2.0);
		double factorTwo = factorOne - 1.0;
		
		double dotProduct = vectorFactor.dot(D);
		
		double valueIusingIr = reciprocalVal * ((factorOne * squaredNorm) - (factorTwo * dotProduct));
		
		return valueIusingIr;
	}

	// Composite Value of a vector calculated using Iv formula
/*	public double calculateIusingIv(int nFactor, GVector vectorFactor)
	{
		double normVectorFactor = vectorFactor.norm();
		double factorOne = (nDocuments + normVectorFactor) / (nDocuments - nFactor);
		double factorTwo = factorOne - 1.0;
		double dotProduct = vectorFactor.dot(D);
		
		double valueIusingIv = (factorOne * normVectorFactor) - (factorTwo * (dotProduct / normVectorFactor));
		
		return valueIusingIv;
	}	*/

	//Getting value q in the Refinement step based on IR
	public int getQNumberUsingIr(int pValue, int iValue, GVector vectorFactor)
	{
		double currValue = 0.0, prev = 0.0;
		int max = 0;
		GVector tempVector = new GVector(numUniqueTerms);
		
		for(int k = 0; k < seedDocumentIndices.length; k++)
		{
			if(k != pValue)
			{
				tempVector.zero();
				tempVector.add(Dr[k], docNormVectors.get(iValue));
				currValue = calculateIusingIr((nr[k] + 1), tempVector) - calculateIusingIr(nr[k], Dr[k]);
				if(prev <= currValue)
				{
					prev = currValue;
					max = k;
				}
			}
		}
		return max;
	}

	//Getting value q in the Refinement step based on IV
	/*public int getQNumberUsingIv(int pValue, int iValue, GVector vectorFactor)
	{
		double currValue = 0.0, prev = 0.0;
		int max = 0;
		GVector tempVector = new GVector(numUniqueTerms);
		
		for(int k = 0; k < seedDocumentIndices.length; k++)
		{
			if(k != pValue)
			{
				tempVector.zero();
				tempVector.add(Dr[k], docNormVectors.get(iValue));			
				currValue = calculateIusingIv((nr[k] + 1), tempVector) - calculateIusingIv(nr[k], Dr[k]);
				if(prev <= currValue)
				{
					prev = currValue;
					max = k;
				}
			}
		}
		return max;
	}	*/
	
	// Reassign cluster for a particular document in the Refinement step
	public int reassignCluster(int iValue, int pValue, int qValue)
	{
		if(initialClusterMatrix.getElement(pValue, iValue) == 0.0)
			return -1;
		initialClusterMatrix.setElement(pValue, iValue, 0.0);
		initialClusterMatrix.setElement(qValue, iValue, 1.0);
		
		return 0;
	}
	
	// Recalculate Dr for a given cluster in the Refinement step
	public void reCalculateDr(int rValue)
	{
		Dr[rValue].zero();
		for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
		{
			if(initialClusterMatrix.getElement(rValue, col) == 1.0)
			{
				Dr[rValue].add(docNormVectors.get(col));
			}
		}

		Cr[rValue].zero();
		if(nr[rValue] != 0)
		{
			Cr[rValue].add(Dr[rValue]);
			for(int b = 0; b < Dr[rValue].getSize(); b++)
			{
				double tempValue = Dr[rValue].getElement(b) / nr[rValue];
				Cr[rValue].setElement(b, tempValue);
			}
		}
		else
		{
			Cr[rValue].zero();
		}	
	}

	// Recalculate nr for a given cluster in the Refinement step	
	public void reCalculateNr(int rValue)
	{
		int clusterCount = 0;
		for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
		{
			if(initialClusterMatrix.getElement(rValue, col) == 1.0)
			{
				clusterCount++;
			}
		}	
		nr[rValue] = clusterCount;
	}
	
	// Refinement step based on choice of clustering criterion to be used
	// 1 - MVSC-IR
	// 2 - MVSC-IV
	public int performRefinementStep(String logFileName)
	{
		lock_var = 1;
		int hasDocumentMoved = 1, iVal = 0, pVal = 0, qVal = 0, totalNumRuns = 0;;
		double deltaIp = 0.0, deltaIq = 0.0;
		GVector tempV = new GVector(numUniqueTerms);
		createV();

		
		try
		{
			FileOutputStream foutlog=new FileOutputStream(logFileName, true);
		
			while(hasDocumentMoved != 0 && totalNumRuns < 20000)
			{
				hasDocumentMoved = 0;
				totalNumRuns++;
				generatePermutationv();
				
				for(int j = 0; j < v.size(); j++)
				{
					iVal = v.get(j);
					
					for(int row = 0; row < initialClusterMatrix.getNumRow(); row++)
					{
						if(initialClusterMatrix.getElement(row, iVal) == 1.0)
						{
							pVal = row;
							break;
						}
					}
					
					logText = "\nj = "+j+" iVal = "+iVal+" pVal = "+pVal;
					//foutlog.write(logText.getBytes());
					
					if(algoChoice == 1)
					{
						tempV.zero();
						tempV.sub(Dr[pVal], docNormVectors.get(iVal));

						deltaIp = calculateIusingIr(nr[pVal] - 1, tempV) - calculateIusingIr(nr[pVal], Dr[pVal]);
						if(Double.isNaN(deltaIp))
						{
							deltaIp = 0.0;
						}
						logText = "\n\tdeltaIp Calculation: \n\tnp-1 = "+(nr[pVal]-1)+"\n\tDp-di: "+tempV.toString()+"\n\tnp = "+nr[pVal]+"\n\tDp: "+Dr[pVal];
						//foutlog.write(logText.getBytes());
						
						qVal = getQNumberUsingIr(pVal, iVal, docNormVectors.get(iVal));
						
						tempV.zero();
						tempV.add(Dr[qVal], docNormVectors.get(iVal));	
						
						deltaIq = calculateIusingIr(nr[qVal] + 1, tempV) - calculateIusingIr(nr[qVal], Dr[qVal]);
						if(Double.isNaN(deltaIq))
						{
							deltaIq = 0.0;
						}
						
						logText = "\n\tdeltaIq Calculation: \n\tnq+1 = "+(nr[qVal]+1)+"\n\tDq+di: "+tempV.toString()+"\n\tnq = "+nr[qVal]+"\n\tDq: "+Dr[qVal];
						//foutlog.write(logText.getBytes());
					}
					else if(algoChoice == 2)
					{
						tempV.zero();
						tempV.sub(Dr[pVal], docNormVectors.get(iVal));
						
						deltaIp = calculateIusingIv(nr[pVal] - 1, tempV) - calculateIusingIv(nr[pVal], Dr[pVal]);
						if(Double.isNaN(deltaIp))
						{
							deltaIp = 0.0;
						}
						
						logText = "\n\tdeltaIp Calculation: \n\tnp-1 = "+(nr[pVal]-1)+"\n\tDp-di: "+tempV.toString()+"\n\tnp = "+nr[pVal]+"\n\tDp: "+Dr[pVal];
						//foutlog.write(logText.getBytes());	
						
						qVal = getQNumberUsingIv(pVal, iVal, docNormVectors.get(iVal));
						
						tempV.zero();
						tempV.add(Dr[qVal], docNormVectors.get(iVal));
						
						deltaIq = calculateIusingIv(nr[qVal] + 1, tempV) - calculateIusingIv(nr[qVal], Dr[qVal]);
						if(Double.isNaN(deltaIq))
						{
							deltaIq = 0.0;
						}
						
						logText = "\n\tdeltaIq Calculation: \n\tnq+1 = "+(nr[qVal]+1)+"\n\tDq+di: "+tempV.toString()+"\n\tnq = "+nr[qVal]+"\n\tDq: "+Dr[qVal];
						//foutlog.write(logText.getBytes());						
					}
					
					logText = "\nqVal = "+qVal+" deltaIp = "+deltaIp+" deltaIq = "+deltaIq+" delIp + delIq = "+(deltaIp + deltaIq);
					//foutlog.write(logText.getBytes());	
					
					if((deltaIp + deltaIq) > 0.0)
					{
						reassignCluster(iVal, pVal, qVal);
						reCalculateNr(pVal);
						reCalculateNr(qVal);
						reCalculateDr(pVal);
						reCalculateDr(qVal);
						hasDocumentMoved = 1;
						
					}
				}
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());		
			logText = "\nFinal Clusters Matrix: "; 
			foutlog.write(logText.getBytes());

			for(int row = 0; row < initialClusterMatrix.getNumRow(); row++)
			{
				logText = "\nCluster: "+(row)+" -> "; 
				foutlog.write(logText.getBytes());
				for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
				{
					logText = " "+initialClusterMatrix.getElement(row, col); 
					foutlog.write(logText.getBytes());
				}
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());		
			logText = "\nFinal Clusters using algorithm "+algoChoice; 
			foutlog.write(logText.getBytes());
			
			int numOfClustersFinal = 0;		

			for(int row = 0; row < initialClusterMatrix.getNumRow(); row++)
			{
				for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
				{
					if(initialClusterMatrix.getElement(row, col) == 1.0)
					{
						numOfClustersFinal++;
						break;
					}
				}
			}

			foutlog.write(logText.getBytes());
			
			finalClusters = new GVector[numOfClustersFinal];
			
			int docsPerCluster = 0, clustersIndex = 0;
			
			for(int row = 0; row < initialClusterMatrix.getNumRow(); row++)
			{
				docsPerCluster = 0;
				for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
				{
					if(initialClusterMatrix.getElement(row, col) == 1.0)
					{
						docsPerCluster++;
					}
				}
				if(docsPerCluster > 0)
				{
					finalClusters[clustersIndex++] = new GVector(docsPerCluster);
				}
			}		
			
			int vectorIndex = 0, hasDocs = 0;
			clustersIndex = 0;
			for(int row = 0; row < initialClusterMatrix.getNumRow(); row++)
			{
				vectorIndex = 0;
				hasDocs = 0;
				for(int col = 0; col < initialClusterMatrix.getNumCol(); col++)
				{
					if(initialClusterMatrix.getElement(row, col) == 1.0)
					{
						finalClusters[clustersIndex].setElement(vectorIndex++,col);
						hasDocs = 1;
					}
				}
				if(hasDocs == 1)
					clustersIndex++;
			}
			
			/*for(int t=0;t<finalClusters.length;t++)
			{
				Clusters.addItemset(new Itemset(""+t));
			}*/
			
			for(int k = 0; k < finalClusters.length; k++)
			{
				logText = "\nFinal Cluster: "+(k+1)+" -> "; 
				foutlog.write(logText.getBytes());
				for(int m = 0; m < finalClusters[k].getSize(); m++)
				{
					int tempNum = (int)finalClusters[k].getElement(m);
					logText = " "+tempNum;
					foutlog.write(logText.getBytes());
					//Clusters.getItemset(k).addItem(""+tempNum);
				}
			}
			
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			/*logText = "\n Initial Cluster Copy Matrix: ";
			foutlog.write(logText.getBytes());		
			for(int t = 0; t < initialClusterMatrixCopy.getNumRow(); t++)
			{
				logText = "\n Cluster: "+(t)+" Values: ";
				foutlog.write(logText.getBytes());
				for(int k = 0; k < initialClusterMatrixCopy.getNumCol(); k++)
				{
					logText = " "+initialClusterMatrixCopy.getElement(t, k);
					foutlog.write(logText.getBytes());
				}
			}	*/
			lock_var = 0;
			

		}
		catch(Exception e)
		{
			lock_var = 0;
			txtMessage.append("Error Occured in refinement: \n");
			txtMessage.append(e+"\n");		
		}		
		return 1;		
	}
	
	//display clusters
	public void Cluster()
	{
		try
		{
			String timeStamp_now = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			clusterPath = "Cluster_Log_run_" + nRun + "_" + timeStamp_now + ".txt";
			FileOutputStream foutlogCluster=new FileOutputStream(clusterPath, true);
			
			//int nClusters=0;
			txtClusters.setText("");
			addClustersText("\nClusters formed with MVSC-Ir");
			logText = "\nClusters formed with MVSC-Ir"; 
			foutlogCluster.write(logText.getBytes());			
			for(int k = 0; k < finalClusters.length; k++)
			{
				addClustersText("\nCluster: "+k+" ->");
				logText = "\nFinal Cluster: "+(k+1)+" -> "; 
				foutlogCluster.write(logText.getBytes());
				for(int m = 0; m < finalClusters[k].getSize(); m++)
				{
					int tempNum = (int)finalClusters[k].getElement(m);
					addClustersText(" "+tempNum);
					logText = " "+tempNum;
					foutlogCluster.write(logText.getBytes());
					//Clusters.getItemset(k).addItem(""+tempNum);
				}
			}
			addClustersText("\nNumber of clusters formed: "+finalClusters.length);
			logText = "\nNumber of clusters formed: "+finalClusters.length;
			foutlogCluster.write(logText.getBytes());
			
			addClustersText("\n\nClusters formed with MVSC-Iv");
			logText = "\n\nClusters formed with MVSC-Iv"; 
			foutlogCluster.write(logText.getBytes());			
			for(int k = 0; k < finalClustersIv.length; k++)
			{
				addClustersText("\nCluster: "+k+" ->");
				logText = "\nFinal Cluster: "+(k+1)+" -> "; 
				foutlogCluster.write(logText.getBytes());
				for(int m = 0; m < finalClustersIv[k].getSize(); m++)
				{
					int tempNum = (int)finalClustersIv[k].getElement(m);
					addClustersText(" "+tempNum);
					logText = " "+tempNum;
					foutlogCluster.write(logText.getBytes());
					//Clusters.getItemset(k).addItem(""+tempNum);
				}
			}
			addClustersText("\nNumber of clusters formed: "+finalClustersIv.length);
			logText = "\nNumber of clusters formed: "+finalClustersIv.length;
			foutlogCluster.write(logText.getBytes());			

			foutlogCluster.close();
			printkMeansClusters(classNum,clusterPath);
		}
		catch(Exception e)
		{
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");		
		}
	}
	
/*	double findSimilarity(WebDocument d1,WebDocument d2)
	{
		double simp=findPhraseSimilarity(d1,d2);
		double simt=findTermSimilarity(d1,d2);
		double sim=(simalpha*simp)+((1.0-simalpha)*simt);
		return(sim);
	}
	
	double findPhraseSimilarity(WebDocument d1,WebDocument d2)
	{
		WebDocument doc1=CombineDocument(d1,d2);
		
		//find sigmaj
		double sigmaj=0.0;
		for(int t=0;t<d1.getPhrases().get_nItems();t++)
		{
			double s1j=StringUtils.split(d1.getPhrase(t)," ").length;
			double tweight=doc1.DIG.findPhrasePathWeight(d1.getPhrase(t));
			sigmaj+=s1j*tweight;
		}
		
		//find sigmak
		double sigmak=0.0;
		for(int t=0;t<d2.getPhrases().get_nItems();t++)
		{
			double s2k=StringUtils.split(d2.getPhrase(t)," ").length;
			double tweight=doc1.DIG.findPhrasePathWeight(d2.getPhrase(t));
			sigmak+=s2k*tweight;
		}
		
		double fragmentationFactor=1.2; //proposed constant
		
		//find sigmap
		double sigmap=0.0;
		for(int t=0;t<doc1.getPhrases().get_nItems();t++)
		{
			double li=StringUtils.split(doc1.getPhrase(t)," ").length;
			double si=doc1.getPhrases().get_nItems();
			double gi=java.lang.Math.pow(li/si,fragmentationFactor);
			double f1i=d1.findPhraseFrequency(doc1.getPhrase(t));
			double w1i=doc1.DIG.findPhrasePathWeight(doc1.getPhrase(t));
			double f2i=d2.findPhraseFrequency(doc1.getPhrase(t));
			double w2i=doc1.DIG.findPhrasePathWeight(doc1.getPhrase(t));
			double tsum=(f1i*w1i)+(f2i+w2i);
			sigmap+=java.lang.Math.pow(gi*tsum,2.0);
		}
		
		//find sim_p
		double simp=java.lang.Math.sqrt(sigmap);
		simp/=(sigmaj+sigmak);
		return(simp);
	}
	
	double findTermSimilarity(WebDocument d1,WebDocument d2)
	{
		WebDocument doc1=CombineDocument(d1,d2);
		
		double sigma1=0.0;
		double sigma21=0.0,sigma22=0.0;
		for(int t=0;t<doc1.DIG.V.get_nItems();t++)
		{
			double tfidf1=findTFIDF(doc1.DIG.V.getItem(t),d1, t);
			double tfidf2=findTFIDF(doc1.DIG.V.getItem(t),d2, t);
			sigma1+=tfidf1*tfidf2;
			sigma21+=tfidf1*tfidf1;
			sigma22+=tfidf2*tfidf2;
		}
		
		//consine similarity
		double simt=sigma1/java.lang.Math.sqrt(sigma21*sigma22);

		return(simt);
	}*/
	
	public int getCountOfWord(String term, WebDocument d1, int docNum)
	{
		HTML_Parser parser1 = new HTML_Parser();
		parser1.setFilePath(dirName+visitedPages[docNum]);
		Queue q=parser1.findBody(); 
		String tstr=q.toString();
		tstr = doStemmer(tstr);
		int bodyCount = findOccurenceSubstring(tstr.toLowerCase(), term.toLowerCase());
		double n1=d1.findTermFrequency(term);
		int totalCount = bodyCount + (int)n1;
		return totalCount;
	}
	
	public void getStopWords()
	{
		try
		{
			String word_freq_file = "Word_Freq_File"+ ".txt";
			FileOutputStream foutlog_wordFreq=new FileOutputStream(word_freq_file, true);
			HTML_Parser parser1 = new HTML_Parser();
			parser1.setFilePath(dirName+"stop-words.txt");
			Queue q=parser1.findStopWords(); 
			stopWords=q.toString();
			logText = "\nStop Words: "+stopWords;
			foutlog_wordFreq.write(logText.getBytes());	
			int bodyCount = findOccurenceSubstring(stopWords.toLowerCase(), "whither");
			logText = "\nbodyCount: "+bodyCount;
			foutlog_wordFreq.write(logText.getBytes());	
			bodyCount = findOccurenceSubstring(stopWords.toLowerCase(), "sushanta");
			logText = "\nbodyCount: "+bodyCount;
			foutlog_wordFreq.write(logText.getBytes());			
			foutlog_wordFreq.close();
		}
		catch(Exception e)
		{
			txtMessage.append("Error Occured in refinement: \n");
			txtMessage.append(e+"\n");		
		}		
	}	
		
	double findTFIDF(String term,WebDocument d1, int docNum)
	{
		try
		{
			HTML_Parser parser1 = new HTML_Parser();
			String timeStamp_now = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			String word_freq_file = "Word_Freq_File"+ ".txt";
			FileOutputStream foutlog_wordFreq=new FileOutputStream(word_freq_file, true);
			
			parser1.setFilePath(dirName+visitedPages[docNum]);
			logText = "\nFile Name: "+(dirName+visitedPages[docNum]);
			foutlog_wordFreq.write(logText.getBytes());				
			Queue q=parser1.findBody(); //get meta-data
			String tstr=q.toString();
			tstr = doStemmer(tstr);
			int bodyCount = findOccurenceSubstring(tstr.toLowerCase(), term.toLowerCase());
			logText = "\nBody Freq: "+bodyCount;
			foutlog_wordFreq.write(logText.getBytes());	
			//find tf
			double n1=d1.findTermFrequency(term);
			logText = "\nFile Number: "+docNum+" term: "+term+" frequency: "+n1;
			foutlog_wordFreq.write(logText.getBytes());
			foutlog_wordFreq.close();
			double tsum=0.0;
			/*for(int t=0;t<d1.DIG.V.get_nItems();t++)
			{
				tsum+=d1.findTermFrequency(d1.DIG.V.getItem(t));
			}*/
			double tf=((double)bodyCount + n1)/((double)totalWordCount[docNum]);
			
			//find idf
			int tDocCount=0;
			for(int t=0;t<nDocuments;t++)
			{
				if(documents[t].DIG.V.isContains(term)==true)
				{
					tDocCount+=1;
				}
			}
			double tval=(double)nDocuments/(double)tDocCount;
			double idf=java.lang.Math.log(tval);
			
			double tfidf=tf*idf;
			
			return(tfidf);
		}
	
		catch(Exception e)
		{
			txtMessage.append("Error Occured in refinement: \n");
			txtMessage.append(e+"\n");		
		}
		return 0;
	}
	
	WebDocument CombineDocument(WebDocument d1,WebDocument d2)
	{
		//construct combined doc to find matching phrases
		DocumentIndexGraph dig1=new DocumentIndexGraph();
		dig1.V.appendItemset(d1.DIG.V);
		dig1.V.appendItemset(d2.DIG.V);
		ItemsetCollection ic1=new ItemsetCollection(dig1.V);
		dig1.V=ic1.getUniqueItemset();
		dig1.E.appendItemsetCollection(d1.DIG.E);
		dig1.E.appendItemsetCollection(d2.DIG.E);
		ic1=dig1.E;
		dig1.E=ic1.getUniqueItemsetCollection();
		WebDocument doc1=new WebDocument();
		doc1.setDIG(dig1);
		doc1.Phrases.appendItemset(d1.getPhrases());
		doc1.Phrases.appendItemset(d2.getPhrases());
		ic1=new ItemsetCollection(doc1.getPhrases());
		doc1.setPhrases(ic1.getUniqueItemset());
		return(doc1);
	}
	
	void addResultText(String tStr)
	{
		txtResult.append(tStr);
		txtResult.updateUI();
	}
	
	void addClusterDocumentScoreText(String tStr)
	{
		txtClustersDocScores.append(tStr);
		txtClustersDocScores.updateUI();
	}

	void addClustersText(String tStr)
	{
		txtClusters.append(tStr);
		txtClusters.updateUI();
	}	
	
	private void addVisitedPage(String tStr)
	{
		if(isVisitedPage(tStr)==false)
		{
			visitedPages[nVisited]=tStr;
			nVisited++;
		}
	}
	
	private boolean isVisitedPage(String tStr)
	{
		boolean visited=false;
		
		for(int t=0;t<nVisited;t++)
		{
			if(tStr.compareToIgnoreCase(visitedPages[t])==0)
			{
				visited=true;
			}
		}
		
		return(visited);
	}
	
	// Cosine similarity measure for k-means algorithm
	public double findCosineSimilarity(int indexdocA, GVector clusterCentroid)
	{
		double dotProduct = clusterCentroid.dot(docNormVectors.get(indexdocA));
		double magnitudeOfA = clusterCentroid.norm();
		double magnitudeOfB = docNormVectors.get(indexdocA).norm();
		double result = dotProduct / (magnitudeOfA * magnitudeOfB);
		//when 0 is divided by 0 it shows result NaN so return 0 in such case.
		if (Double.isNaN(result))
			return 0;
		else
			return (double)result;
	}	
	
	// Generating unique set of random numbers for k-means algorithm
	public void generateUniqueRandomNumber(HashSet<Integer> uniqRand, int k)
	{
		Random r = new Random();
		
		if (k > nDocuments)
		{
			do
			{
				int pos = r.nextInt(nDocuments);
				uniqRand.add(pos);

			} while (uniqRand.size() != nDocuments);
		}            
		else
		{
			do
			{
				int pos = r.nextInt(nDocuments);
				uniqRand.add(pos);

			} while (uniqRand.size() != k);
		}
	}
	
	// Initialization of clusters for k-means. Number of clusters to form
	// initially is got through an argument. The maximum that can be assigned to a
	// single cluster are all the documents. So all the GVectors are initialized that
	// way
	public void initializeClusterCentroidKMeans(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);
			finalClustersKMeansCentroid = new GVector[k];
			
			for(int i = 0; i < k; i++)
			{
				finalClustersKMeansCentroid[i] = new GVector(numUniqueTerms);
			}
			logText = "\nInitialized K-Means initial cluster centroids";
			//foutlog.write(logText.getBytes());
			//foutlog.close();			
		}
		catch(Exception e)
		{
		}
	}
	
	// Assigning the first random k number of documents as the starting 
	// centroids
	public void assignInitialClusterCentroids(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);	
			HashSet<Integer> uniqIndex = new HashSet<Integer>();
			generateUniqueRandomNumber(uniqIndex, k);
			logText = "\nAssigning K-Means initial cluster centroids";
			//foutlog.write(logText.getBytes());		
		
			int clusterCentroidIndex = 0;
			for(int num: uniqIndex)
			{
				finalClustersKMeansCentroid[clusterCentroidIndex++].add(docNormVectors.get(num));
				logText = "\nNumber selected: "+num+" For centroid index: "+(clusterCentroidIndex - 1);
				//foutlog.write(logText.getBytes());
				logText = "\nVector selected: "+docNormVectors.get(num).toString();
				//foutlog.write(logText.getBytes());			
			}
			//foutlog.close();	
		}
		catch(Exception e)
		{
		}
	}
	
	// Initialize the previous and current cluster documents count
	// Contains -1 indicating no prior operation has taken place
	public void initializeClusterCounts(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);			
			clusterDocCounts = new int[k][2];
		
			for(int row = 0; row < k; row++)
			{
				for(int col = 0; col < 2; col++)
				{
					clusterDocCounts[row][col] = 0;
				}
			}
			
			logText = "\nInitialized cluster document counts array";
			//foutlog.write(logText.getBytes());
			//foutlog.close();	
		}
		catch(Exception e)
		{
		}
	}

	// Reinitialize the counts for next iteration.
	// Basically copy the right column in to the left column
	public void reinitializeClusterCounts(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);		
			logText = "\nReinitializing cluster counts tracker";
			//foutlog.write(logText.getBytes());				
			for(int row = 0; row < k; row++)
			{
				logText = "\nLeft Column Value: "+clusterDocCounts[row][0]+" Right Column Value: "+clusterDocCounts[row][1];
				clusterDocCounts[row][0] = clusterDocCounts[row][1];
				//foutlog.write(logText.getBytes());				
			}
			//foutlog.close();
		}
		catch(Exception e)
		{
		}
	}

	// Initialize the matrices holding the current and previous clusters
	public void initializeClusterMatrices(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);	
			currClusterKMeans = new GMatrix(k, nDocuments);
			currClusterKMeans.setZero();
			logText = "\nInitialized cluster matrices";
			//foutlog.write(logText.getBytes());
			//foutlog.close();	
		}
		catch(Exception e)
		{
		}
	}
	
	// Calculate the cluster document counts
	public void populateClusterDocumentCount(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);	
			logText = "\nPopulating Cluster document count";
			//foutlog.write(logText.getBytes());			
			for(int row = 0; row < k; row++)
			{
				int sum = 0;
				for(int col = 0; col < nDocuments; col++)
				{
					if(currClusterKMeans.getElement(row, col) == 1.0)
						sum++;
				}
				clusterDocCounts[row][1] = sum;
				logText = "\nNumber of documents in cluster: "+row+" = "+clusterDocCounts[row][1];
				//foutlog.write(logText.getBytes());				
			}
			//foutlog.close();
		}
		catch(Exception e)
		{
		}
	}
	
	// Calculate new centroid
	public void recalculateCentroids(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);	
			logText = "\nRecalculating centroids";
			//foutlog.write(logText.getBytes());	
			for(int row = 0; row < k; row++)
			{
				GVector temp = new GVector(numUniqueTerms);
				temp.zero();
				int docCount = 0;
				logText = "\nConsidering current cluster index: "+row;
				//foutlog.write(logText.getBytes());				
				for(int col = 0; col < nDocuments; col++)
				{
					if(currClusterKMeans.getElement(row, col) == 1.0)
					{
						logText = "\nDocument vector index "+col+" content: "+docNormVectors.get(col).toString();
						//foutlog.write(logText.getBytes());						
						temp.add(docNormVectors.get(col));
						docCount++;
					}
				}
				logText = "\nAddition result: "+temp.toString();
				//foutlog.write(logText.getBytes());				
				double additivetfidf = 0;
				for(int i = 0; i < temp.getSize(); i++)
				{
					additivetfidf = temp.getElement(i) / (double) docCount;
					temp.setElement(i, additivetfidf);
				}
				logText = "\nCentroid calculation: "+temp.toString();
				//foutlog.write(logText.getBytes());				
			}
			//foutlog.close();
		}
		catch(Exception e)
		{
		}
	}
	
	// Check if cluster movements occured or not
	public boolean checkClusterChange(int k)
	{
		for(int row = 0; row < k; row++)
		{
			if(clusterDocCounts[row][0] != clusterDocCounts[row][1])
				return true;
		}
		return false;
	}
	
	// Find closest cluster centroid index
	public int findClosestClusterCentroidIndex(int k, int docVal)
	{
		int index = 0;
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);	
			logText = "\nFinding closest centroid for document index:"+docVal;
			//foutlog.write(logText.getBytes());	
			double prev = 0.0; 
			for(int row = 0; row < k; row++)
			{
				double temp = findCosineSimilarity(docVal, finalClustersKMeansCentroid[row]);
				logText = "\nLooking at cluster centroid index: "+row+" Value of similarity: "+temp;
				//foutlog.write(logText.getBytes());				
				if(prev < temp)
				{
					prev = temp;
					index = row;
				}
			}
			//foutlog.close();
			
		}
		catch(Exception e)
		{
		}
		return index;
	}
	
	// Performing the actual k-means algorithm
	public void doKMeans(int k)
	{
		try
		{
			//FileOutputStream foutlog=new FileOutputStream(logFileName);	
			logText = "\nStarting K-Means Process:";
			//foutlog.write(logText.getBytes());	
			//foutlog.close();
			
			initializeClusterCentroidKMeans(k);
			assignInitialClusterCentroids(k);
			initializeClusterCounts(k);
			initializeClusterMatrices(k);
			
			int repeatCount = 0;


			do
			{
				currClusterKMeans.setZero();
				//foutlog=new FileOutputStream(logFileName);	
				logText = "\nReset Clusters";
				//foutlog.write(logText.getBytes());	
				//foutlog.close();			
				for(int i = 0; i < nDocuments; i++)
				{
					int row = findClosestClusterCentroidIndex(k, i);
					currClusterKMeans.setElement(row, i, 1.0);
				}
				
				reinitializeClusterCounts(k);
				populateClusterDocumentCount(k);
				if(!checkClusterChange(k))
					break;
				else
					repeatCount++;
				recalculateCentroids(k);
				//foutlog=new FileOutputStream(foutlog);	

				logText = "\nClusters this run:";
				//foutlog.write(logText.getBytes());
				for(int i = 0; i < k; i++)
				{	
					logText = "\nCluster "+i+" : ";
					//foutlog.write(logText.getBytes());
					for(int j = 0; j < nDocuments; j++)
					{
						logText = " "+currClusterKMeans.getElement(i,j);
						//foutlog.write(logText.getBytes());	
					}
				}
				//foutlog.close();			
				
			}while(repeatCount < repeat_limit);

			int numOfClustersFinal = 0;		

			for(int row = 0; row < currClusterKMeans.getNumRow(); row++)
			{
				for(int col = 0; col < currClusterKMeans.getNumCol(); col++)
				{
					if(currClusterKMeans.getElement(row, col) == 1.0)
					{
						numOfClustersFinal++;
						break;
					}
				}
			}

			finalClustersKmeans = new GVector[numOfClustersFinal];
			
			int docsPerCluster = 0, clustersIndex = 0;
			
			for(int row = 0; row < currClusterKMeans.getNumRow(); row++)
			{
				docsPerCluster = 0;
				for(int col = 0; col < currClusterKMeans.getNumCol(); col++)
				{
					if(currClusterKMeans.getElement(row, col) == 1.0)
					{
						docsPerCluster++;
					}
				}
				if(docsPerCluster > 0)
				{
					finalClustersKmeans[clustersIndex++] = new GVector(docsPerCluster);
				}
			}		
			
			int vectorIndex = 0, hasDocs = 0;
			clustersIndex = 0;
			for(int row = 0; row < currClusterKMeans.getNumRow(); row++)
			{
				vectorIndex = 0;
				hasDocs = 0;
				for(int col = 0; col < currClusterKMeans.getNumCol(); col++)
				{
					if(currClusterKMeans.getElement(row, col) == 1.0)
					{
						finalClustersKmeans[clustersIndex].setElement(vectorIndex++,col);
						hasDocs = 1;
					}
				}
				if(hasDocs == 1)
					clustersIndex++;
			}			
			//printkMeansClusters();
		}
		catch(Exception e)
		{
		}
	}
	
	// Printing K-Means Clusters
	public void printkMeansClusters(int k, String logFileName)
	{
		try
		{
			FileOutputStream foutlogCluster=new FileOutputStream(logFileName, true);
			addClustersText("\n\nK-Means Clusters");
			logText = "\n\nK-Means Clusters ";
			foutlogCluster.write(logText.getBytes());	

			for(int row = 0; row < k; row++)
			{
				addClustersText("\nCluster "+(row + 1)+":");
				logText = "\nCluster "+(row + 1)+":";
				foutlogCluster.write(logText.getBytes());		
				for(int col = 0; col < nDocuments; col++)
				{
					if(currClusterKMeans.getElement(row, col) == 1.0)
					{
						addClustersText(" "+col);
						logText = " "+col;
						foutlogCluster.write(logText.getBytes());
					}
				}
			}
			addClustersText("\nNumber of clusters formed: "+k);
			logText = "\nNumber of clusters formed: "+k;
			foutlogCluster.write(logText.getBytes());		
		}
		catch(Exception e)
		{
		}
	}

	//Getting cluster value for a given document in the Initialization step Iv
	public int getClusterNumberIv(GVector documentUnderTest)
	{
		double dotProductValue = 0.0, prev = 0.0;
		int max = 0;
		
		for(int k = 0; k < seedDocumentIndices.length; k++)
		{
			dotProductValue = docNormVectors.get(seedDocumentIndices[k]).dot(documentUnderTest);
			if(prev <= dotProductValue)
			{
				prev = dotProductValue;
				max = k;
			}
		}
		return max;
	}
	
	// Calculate the Dr, Nr, and D values along with Cr and C Iv
	public void calculateNrDrDIv()
	{
		nrIv = new int[seedDocumentIndices.length];
		DrIv = new GVector[seedDocumentIndices.length];
		CrIv = new GVector[seedDocumentIndices.length];
		
		calculateNrIv();
		calculateDrIv();
		calculateDIv();	
	}

	// Retrieve the cluster in which a particular document falls
	public int getFinalClusterIv(int docIndex)
	{
		for(int row = 0; row < finalClustersIv.length; row++)
		{
			for(int col = 0; col < finalClustersIv[row].getSize(); col++)
			{
				if((int)finalClustersIv[row].getElement(col) == docIndex)
				{
					return row;
				}
			}
		}
		return -1;
	}
	// Retrieve the cluster in which a particular document falls for KMeans
	public int getFinalClusterKMeans(int docIndex)
	{
		for(int row = 0; row < currClusterKMeans.getNumRow(); row++)
		{
				if(currClusterKMeans.getElement(row, docIndex) == 1.0)
				{
					return row;
				}
		}
		return -1;
	}	
	
	// MVS Similarity scores for all document pairs
	public int similarityMatrixIv(String logFileName)
	{
		lock_var = 1;
		txtClustersDocScores.setText("");
		
		similarityMatrixIv = new GMatrix(nDocuments, nDocuments);
		similarityMatrixIv.setZero();
		
		GVector[] Dvalues = new GVector[finalClusters.length];
		for(int j = 0; j < finalClustersIv.length; j++)
		{
			Dvalues[j] = new GVector(numUniqueTerms);
			Dvalues[j].zero();
		}
		
		try
		{
			FileOutputStream foutlog=new FileOutputStream(logFileName, true);
			logText = "\nSimilarity Matrix Generation";
			//foutlog.write(logText.getBytes());
			logText = "\nD Vector Generation";
			//foutlog.write(logText.getBytes());
			for(int j = 0; j < finalClustersIv.length; j++)
			{
				logText = "\nD(S/S"+j+") Vector Generation";
				//foutlog.write(logText.getBytes());
				for(int k = 0; k < finalClustersIv.length; k++)
				{
					if(j != k)
					{
						for(int l = 0; l < finalClustersIv[k].getSize(); l++)
						{
							logText = "\n\tAdding doc index "+finalClustersIv[k].getElement(l)+" from cluster "+k;
							//foutlog.write(logText.getBytes());
							Dvalues[j].add(docNormVectors.get((int)finalClustersIv[k].getElement(l)));
							logText = "\n\tAdded Vector: "+docNormVectors.get((int)finalClustersIv[k].getElement(l)).toString();
							//foutlog.write(logText.getBytes());
							logText = "\n\tResult Vector: "+Dvalues[j].toString();
							//foutlog.write(logText.getBytes());
						}
					}
				}
			}
			
			for(int j = 0; j < nDocuments; j++)
			{
				for(int k = 0; k < nDocuments; k++)
				{
					int docjCluster = getFinalClusterIv(j);
					int dockCluster = getFinalClusterIv(k);
					
					double dotProductFirst = docNormVectors.get(j).dot(docNormVectors.get(k));
					int nValue = 0;
					
					if(docjCluster == dockCluster)
						nValue = nDocuments - nrIv[docjCluster];
					else
						nValue = nDocuments - nrIv[docjCluster] - 1;
						
					GVector tempVector = new GVector(numUniqueTerms);
					tempVector.zero();
					
					if(docjCluster == dockCluster)
						tempVector.add(Dvalues[docjCluster]);
					else
					{
						tempVector.add(Dvalues[docjCluster]);
						tempVector.sub(docNormVectors.get(k));
					}
					
					GVector tempC = new GVector(numUniqueTerms);
					for(int b = 0; b < tempVector.getSize(); b++)
					{
						double tempValue = tempVector.getElement(b) / nValue;
						tempC.setElement(b, tempValue);
					}
					
					double dotProductSecond = docNormVectors.get(j).dot(tempC);
					double dotProductThird = docNormVectors.get(k).dot(tempC);
					
					double similarityValue = dotProductFirst - dotProductSecond - dotProductThird + 1;
					
					similarityMatrixIv.setElement(j, k, similarityValue);
				}
			}
			
			logText = "\nSimilarity Matrix: \n";
			foutlog.write(logText.getBytes());
			for(int row = 0; row < similarityMatrixIv.getNumRow(); row++)
			{
				for(int col = 0; col < similarityMatrixIv.getNumCol(); col++)
				{
					logText = " "+similarityMatrixIv.getElement(row, col);
					foutlog.write(logText.getBytes());
				}
				logText = "\n";
				foutlog.write(logText.getBytes());
			}
			lock_var = 0;
			
			
		}
		catch(Exception e)
		{
			lock_var = 0;
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");		
		}
		return 1;
	}	
	
	//Calculating the number of documents per cluster nr
	public void calculateNrIv()
	{
		int nrIndex = 0;
		for(int row = 0; row < initialClusterMatrixCopy.getNumRow(); row++)
		{
			int clusterCount = 0;
			for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
			{
				if(initialClusterMatrixCopy.getElement(row, col) == 1.0)
				{
					clusterCount++;
				}
			}
			nrIv[nrIndex++] = clusterCount;
		}
	}
	
	//Calculating the number of documents per cluster nr
	public void calculateNrKMeans()
	{
		nrKMeans = new int[currClusterKMeans.getNumRow()];
		
		int nrIndex = 0;
		for(int row = 0; row < currClusterKMeans.getNumRow(); row++)
		{
			int clusterCount = 0;
			for(int col = 0; col < currClusterKMeans.getNumCol(); col++)
			{
				if(initialClusterMatrixCopy.getElement(row, col) == 1.0)
				{
					clusterCount++;
				}
			}
			nrKMeans[nrIndex++] = clusterCount;
		}
	}	
	
	// Calculating composite vector of documents in a cluster Dr
	public void calculateDrIv()
	{
		//txtMessage.append("\nIn Calculate Dr");
		for(int row = 0; row < initialClusterMatrixCopy.getNumRow(); row++)
		{
			DrIv[row] = new GVector(numUniqueTerms);
			DrIv[row].zero();
			for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
			{
				if(initialClusterMatrixCopy.getElement(row, col) == 1.0)
				{
					DrIv[row].add(docNormVectors.get(col));
				}
			}
			CrIv[row] = new GVector(numUniqueTerms);
			CrIv[row].zero();
			if(nrIv[row] != 0)
			{
				CrIv[row].add(DrIv[row]);
				for(int b = 0; b < DrIv[row].getSize(); b++)
				{
					double tempValue = DrIv[row].getElement(b) / nrIv[row];
					CrIv[row].setElement(b, tempValue);
				}
			}
			else
			{
				CrIv[row].zero();
			}
			//txtMessage.append("\nAfter Dr-> row = "+row+" Dr: "+Dr[row].toString());
			//txtMessage.append("\nAfter Cr-> row = "+row+" Cr: "+Cr[row].toString());
		}
	}
	
	// Calculating composite vector of documents in a cluster Dr
	public void calculateDIv()
	{
		//txtMessage.append("\nIn Calculate D");
		DIv = new GVector(numUniqueTerms);
		DIv.zero();
		for(int k = 0; k < nDocuments; k++)
		{
			DIv.add(docNormVectors.get(k));
		}
		CIv = new GVector(numUniqueTerms);
		CIv.zero();
		CIv.add(D);
		for(int b = 0; b < DIv.getSize(); b++)
		{
			double tempValue = DIv.getElement(b) / nDocuments;
			CIv.setElement(b, tempValue);
		}
		//txtMessage.append("\nD: "+D.toString());
		//txtMessage.append("\nC: "+C.toString());		
	}
	
	// Creating v list with document indices
	public void createVIv()
	{
		for(int j = 0; j < nDocuments; j++)
		{
			vIv.add(j);
		}
	}
	
	// Generating random permutation of v for Refinement step
	public void generatePermutationvIv()
	{
		java.util.Collections.shuffle(vIv);
	}

	public double calculateIusingIv(int nFactor, GVector vectorFactor)
	{
		double normVectorFactor = vectorFactor.norm();
		double factorOne = (nDocuments + normVectorFactor) / (nDocuments - nFactor);
		double factorTwo = factorOne - 1.0;
		double dotProduct = vectorFactor.dot(D);
		
		double valueIusingIv = (factorOne * normVectorFactor) - (factorTwo * (dotProduct / normVectorFactor));
		
		return valueIusingIv;
	}		

	//Getting value q in the Refinement step based on IV
	public int getQNumberUsingIv(int pValue, int iValue, GVector vectorFactor)
	{
		double currValue = 0.0, prev = 0.0;
		int max = 0;
		GVector tempVector = new GVector(numUniqueTerms);
		
		for(int k = 0; k < seedDocumentIndices.length; k++)
		{
			if(k != pValue)
			{
				tempVector.zero();
				tempVector.add(DrIv[k], docNormVectors.get(iValue));			
				currValue = calculateIusingIv((nrIv[k] + 1), tempVector) - calculateIusingIv(nrIv[k], DrIv[k]);
				if(prev <= currValue)
				{
					prev = currValue;
					max = k;
				}
			}
		}
		return max;
	}	
	
	// Reassign cluster for a particular document in the Refinement step
	public int reassignClusterIv(int iValue, int pValue, int qValue)
	{
		if(initialClusterMatrixCopy.getElement(pValue, iValue) == 0.0)
			return -1;
		initialClusterMatrixCopy.setElement(pValue, iValue, 0.0);
		initialClusterMatrixCopy.setElement(qValue, iValue, 1.0);
		
		return 0;
	}
	
	// Recalculate Dr for a given cluster in the Refinement step
	public void reCalculateDrIv(int rValue)
	{
		DrIv[rValue].zero();
		for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
		{
			if(initialClusterMatrixCopy.getElement(rValue, col) == 1.0)
			{
				DrIv[rValue].add(docNormVectors.get(col));
			}
		}

		CrIv[rValue].zero();
		if(nrIv[rValue] != 0)
		{
			CrIv[rValue].add(DrIv[rValue]);
			for(int b = 0; b < DrIv[rValue].getSize(); b++)
			{
				double tempValue = DrIv[rValue].getElement(b) / nrIv[rValue];
				CrIv[rValue].setElement(b, tempValue);
			}
		}
		else
		{
			CrIv[rValue].zero();
		}	
	}

	// Recalculate nr for a given cluster in the Refinement step	
	public void reCalculateNrIv(int rValue)
	{
		int clusterCount = 0;
		for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
		{
			if(initialClusterMatrixCopy.getElement(rValue, col) == 1.0)
			{
				clusterCount++;
			}
		}	
		nrIv[rValue] = clusterCount;
	}
	
	// Refinement step based on choice of clustering criterion to be used
	// 1 - MVSC-IR
	// 2 - MVSC-IV
	public int performRefinementStepIv(String logFileName)
	{
		lock_var = 1;
		int hasDocumentMoved = 1, iVal = 0, pVal = 0, qVal = 0, totalNumRuns = 0;;
		double deltaIp = 0.0, deltaIq = 0.0;
		GVector tempV = new GVector(numUniqueTerms);
		createVIv();

		
		try
		{
			FileOutputStream foutlog=new FileOutputStream(logFileName, true);
		
			while(hasDocumentMoved != 0 && totalNumRuns < 20000)
			{
				hasDocumentMoved = 0;
				totalNumRuns++;
				generatePermutationvIv();
				
				for(int j = 0; j < v.size(); j++)
				{
					iVal = vIv.get(j);
					
					for(int row = 0; row < initialClusterMatrixCopy.getNumRow(); row++)
					{
						if(initialClusterMatrixCopy.getElement(row, iVal) == 1.0)
						{
							pVal = row;
							break;
						}
					}
					
					logText = "\nj = "+j+" iVal = "+iVal+" pVal = "+pVal;
					//foutlog.write(logText.getBytes());
					
					if(algoChoice == 1)
					{
						tempV.zero();
						tempV.sub(DrIv[pVal], docNormVectors.get(iVal));

						deltaIp = calculateIusingIr(nrIv[pVal] - 1, tempV) - calculateIusingIr(nrIv[pVal], DrIv[pVal]);
						if(Double.isNaN(deltaIp))
						{
							deltaIp = 0.0;
						}
						logText = "\n\tdeltaIp Calculation: \n\tnp-1 = "+(nrIv[pVal]-1)+"\n\tDp-di: "+tempV.toString()+"\n\tnp = "+nrIv[pVal]+"\n\tDp: "+DrIv[pVal];
						//foutlog.write(logText.getBytes());
						
						qVal = getQNumberUsingIr(pVal, iVal, docNormVectors.get(iVal));
						
						tempV.zero();
						tempV.add(DrIv[qVal], docNormVectors.get(iVal));	
						
						deltaIq = calculateIusingIr(nrIv[qVal] + 1, tempV) - calculateIusingIr(nrIv[qVal], DrIv[qVal]);
						if(Double.isNaN(deltaIq))
						{
							deltaIq = 0.0;
						}
						
						logText = "\n\tdeltaIq Calculation: \n\tnq+1 = "+(nrIv[qVal]+1)+"\n\tDq+di: "+tempV.toString()+"\n\tnq = "+nrIv[qVal]+"\n\tDq: "+DrIv[qVal];
						//foutlog.write(logText.getBytes());
					}
					else if(algoChoice == 2)
					{
						tempV.zero();
						tempV.sub(DrIv[pVal], docNormVectors.get(iVal));
						
						deltaIp = calculateIusingIv(nrIv[pVal] - 1, tempV) - calculateIusingIv(nrIv[pVal], DrIv[pVal]);
						if(Double.isNaN(deltaIp))
						{
							deltaIp = 0.0;
						}
						
						logText = "\n\tdeltaIp Calculation: \n\tnp-1 = "+(nrIv[pVal]-1)+"\n\tDp-di: "+tempV.toString()+"\n\tnp = "+nrIv[pVal]+"\n\tDp: "+DrIv[pVal];
						//foutlog.write(logText.getBytes());	
						
						qVal = getQNumberUsingIv(pVal, iVal, docNormVectors.get(iVal));
						
						tempV.zero();
						tempV.add(DrIv[qVal], docNormVectors.get(iVal));
						
						deltaIq = calculateIusingIv(nrIv[qVal] + 1, tempV) - calculateIusingIv(nrIv[qVal], DrIv[qVal]);
						if(Double.isNaN(deltaIq))
						{
							deltaIq = 0.0;
						}
						
						logText = "\n\tdeltaIq Calculation: \n\tnq+1 = "+(nrIv[qVal]+1)+"\n\tDq+di: "+tempV.toString()+"\n\tnq = "+nrIv[qVal]+"\n\tDq: "+DrIv[qVal];
						//foutlog.write(logText.getBytes());						
					}
					
					logText = "\nqVal = "+qVal+" deltaIp = "+deltaIp+" deltaIq = "+deltaIq+" delIp + delIq = "+(deltaIp + deltaIq);
					//foutlog.write(logText.getBytes());	
					
					if((deltaIp + deltaIq) > 0.0)
					{
						reassignClusterIv(iVal, pVal, qVal);
						reCalculateNrIv(pVal);
						reCalculateNrIv(qVal);
						reCalculateDrIv(pVal);
						reCalculateDrIv(qVal);
						hasDocumentMoved = 1;
						
					}
				}
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());		
			logText = "\nFinal Clusters Matrix: "; 
			foutlog.write(logText.getBytes());

			for(int row = 0; row < initialClusterMatrixCopy.getNumRow(); row++)
			{
				logText = "\nCluster: "+(row)+" -> "; 
				foutlog.write(logText.getBytes());
				for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
				{
					logText = " "+initialClusterMatrixCopy.getElement(row, col); 
					foutlog.write(logText.getBytes());
				}
			}
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());		
			logText = "\nFinal Clusters using algorithm "+algoChoice; 
			foutlog.write(logText.getBytes());
			
			int numOfClustersFinal = 0;		

			for(int row = 0; row < initialClusterMatrixCopy.getNumRow(); row++)
			{
				for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
				{
					if(initialClusterMatrixCopy.getElement(row, col) == 1.0)
					{
						numOfClustersFinal++;
						break;
					}
				}
			}

			foutlog.write(logText.getBytes());
			
			finalClustersIv = new GVector[numOfClustersFinal];
			
			int docsPerCluster = 0, clustersIndex = 0;
			
			for(int row = 0; row < initialClusterMatrixCopy.getNumRow(); row++)
			{
				docsPerCluster = 0;
				for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
				{
					if(initialClusterMatrixCopy.getElement(row, col) == 1.0)
					{
						docsPerCluster++;
					}
				}
				if(docsPerCluster > 0)
				{
					finalClustersIv[clustersIndex++] = new GVector(docsPerCluster);
				}
			}
		
			int vectorIndex = 0, hasDocs = 0;
			clustersIndex = 0;
			for(int row = 0; row < initialClusterMatrixCopy.getNumRow(); row++)
			{
				vectorIndex = 0;
				hasDocs = 0;
				for(int col = 0; col < initialClusterMatrixCopy.getNumCol(); col++)
				{
					if(initialClusterMatrixCopy.getElement(row, col) == 1.0)
					{
						finalClustersIv[clustersIndex].setElement(vectorIndex++,col);
						hasDocs = 1;
					}
				}
				if(hasDocs == 1)
					clustersIndex++;
			}
			
			/*for(int t=0;t<finalClusters.length;t++)
			{
				Clusters.addItemset(new Itemset(""+t));
			}*/
			
			for(int k = 0; k < finalClustersIv.length; k++)
			{
				logText = "\nFinal Cluster: "+(k+1)+" -> "; 
				foutlog.write(logText.getBytes());
				for(int m = 0; m < finalClustersIv[k].getSize(); m++)
				{
					int tempNum = (int)finalClustersIv[k].getElement(m);
					logText = " "+tempNum;
					foutlog.write(logText.getBytes());
					//Clusters.getItemset(k).addItem(""+tempNum);
				}
			}
			
			logText = "\n ----------------------------------------------------------------";
			foutlog.write(logText.getBytes());				
			/*logText = "\n Initial Cluster Copy Matrix: ";
			foutlog.write(logText.getBytes());		
			for(int t = 0; t < initialClusterMatrixCopy.getNumRow(); t++)
			{
				logText = "\n Cluster: "+(t)+" Values: ";
				foutlog.write(logText.getBytes());
				for(int k = 0; k < initialClusterMatrixCopy.getNumCol(); k++)
				{
					logText = " "+initialClusterMatrixCopy.getElement(t, k);
					foutlog.write(logText.getBytes());
				}
			}	*/
			lock_var = 0;
			

		}
		catch(Exception e)
		{
			lock_var = 0;
			txtMessage.append("Error Occured in refinement: \n");
			txtMessage.append(e+"\n");		
		}		
		return 1;		
	}
	
	static public void main(String[] args)
	{
		try 
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		new multiViewCluster();
	}
}
