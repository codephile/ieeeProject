//MVSC Algorithm Implementation Main Code

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.text.*;
import javax.swing.filechooser.*;
import org.jfree.ui.RefineryUtilities;

class multiViewCluster extends JFrame implements ActionListener
{
	JFrame frmRootPath = new JFrame("Clustering with Multi-Viewpoint based Similarity Measure");
	JFrame frmResult = new JFrame("Results Window");
	
	JButton btProcess=new JButton("Process");
	JTextField txtFieldSimThresh = new JTextField ("");
		
	ItemsetCollection Similarities=new ItemsetCollection();
	
	JButton btHistogram=new JButton("Histogram");
	JButton btCluster=new JButton("Clusters");
	
	ItemsetCollection Hist=new ItemsetCollection();
	
	JLabel lblResult=new JLabel("Result:");
	JLabel lblSimThresh=new JLabel("Similarity Score: ");
		
	JTextArea txtResult=new JTextArea("");
	JScrollPane spResult=new JScrollPane(txtResult);
	JTextArea txtMessage=new JTextArea("");
	JScrollPane spMessage=new JScrollPane(txtMessage);
	
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
	String logText="";
	String txtFileRoot="";
	String dirName;
	
	//init documents
	int nDocuments;
	int nRun = 0;
	WebDocument documents[];
	WebDocument CumulativeDocument;
	ItemsetCollection Clusters=new ItemsetCollection();
	
	multiViewCluster()
	{
		//Root path frame
		frmRootPath.setDefaultLookAndFeelDecorated(true);
		frmRootPath.setResizable(false);
		frmRootPath.setBounds(50,50,700,400);
		frmRootPath.setLocationRelativeTo(null);
		frmRootPath.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRootPath.getContentPane().setLayout(null);
		
		//Result frame
		frmResult.setDefaultLookAndFeelDecorated(true);
		frmResult.setResizable(false);
		frmResult.setBounds(50,50,600,580);
		frmResult.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmResult.getContentPane().setLayout(null);

		// File Choose Button
		btChoosefile.setBounds(248,50,200,40);
		btChoosefile.addActionListener(this);
		frmRootPath.getContentPane().add(btChoosefile);		
		
		//Process button Design
		btProcess.setBounds(50,250,190,40);
		btProcess.addActionListener(this);
		frmRootPath.getContentPane().add(btProcess);
		
		//Histogram button Design
		btHistogram.setBounds(252,250,190,40);
		btHistogram.addActionListener(this);
		frmRootPath.getContentPane().add(btHistogram);

		//Cluster button Design
		btCluster.setBounds(455,250,190,40);
		btCluster.addActionListener(this);
		frmRootPath.getContentPane().add(btCluster);
		
		txtFieldSimThresh.setBounds(330, 310, 190, 40);
		txtFieldSimThresh.addActionListener(this);
		frmRootPath.getContentPane().add(txtFieldSimThresh);
		
		lblSimThresh.setBounds(220, 325, 100, 10);  
		frmRootPath.getContentPane().add(lblSimThresh);
		
		//Result Design
		lblResult.setBounds(17,35,100,20);
		frmResult.getContentPane().add(lblResult);
		spResult.setBounds(15,55,560,450);
		frmResult.getContentPane().add(spResult);
		txtResult.setEditable(false);
		
		spMessage.setBounds(50,110,595,120);
		frmRootPath.getContentPane().add(spMessage);
		txtMessage.setEditable(false);		
		
		frmRootPath.setVisible(true);
	}
	

	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource()==btProcess)
		{
			btProcess.setText("Wait");
			btProcess.updateUI();
			processData="processlog";
			Similarities=new ItemsetCollection();
			parser1=new HTML_Parser();
			frontier=new Queue();
			visitedPages=new String[maxPages];
			nVisited = 0;
			nRun = nRun + 1;
			process();
			btProcess.setText("Done");
			btProcess.updateUI();
			frmResult.setVisible(true);
			txtMessage.append("Process Complete\n");
		}

		if(evt.getSource()==btHistogram)
		{
			Hist=new ItemsetCollection();
			Histogram();
		}
		
		if(evt.getSource()==btCluster)
		{
			Clusters=new ItemsetCollection();
			clusterPath="clusters";
			Cluster();
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
				txtMessage.append("Please press Clusters to see the clusters formed ....\n");
				txtMessage.append("Please press Histogram to see the histogram of similarity measures ....\n");
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
		if(evt.getSource()==txtFieldSimThresh)
		{
		    String txtSimThresh = txtFieldSimThresh.getText();
			similarityThreshold = Double.parseDouble(txtSimThresh) / 100.00;
			txtMessage.append("Similarity Threshold: "+similarityThreshold+"\n");
		}
	}
	
	public void process()
	{
		try
		{
			String timeStamp_now = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			processData = processData + "_run_" + nRun + "_" + timeStamp_now + ".txt";
			FileOutputStream foutlog=new FileOutputStream(processData);
			//starting-urls

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
				Itemset tItemset=new Itemset(tarr);
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
				for(int j=0;j<tarr.length;j++)
				{
					documents[t].addPhrase(tarr[j]);
					CumulativeDocument.addPhrase(tarr[j]);
					String[] tarr1=StringUtils.split(tarr[j]," ");
					if(tarr1.length>1)
					{
						for(int k=0;k<=tarr1.length-2;k++)
						{
								Itemset i1=new Itemset(); //if word-(k+1) appears next to word-k
								i1.addItem(tarr1[k]);
								i1.addItem(tarr1[k+1]);
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
				logText = " Document"+t+"  : "+documents[t].getDocName()+"\n";
				foutlog.write(logText.getBytes());
			}
	
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
			for(int i=0;i<nDocuments;i++)
			{
				Histogram hist = new Histogram("Document "+i+" Similiarity",sim_perc[i]);
				hist.pack();
				RefineryUtilities.centerFrameOnScreen(hist);
				hist.setVisible(true);
			}
		}
		catch(Exception e)
		{
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");
		}
	}

	//display clusters
	public void Cluster()
	{
		try
		{
			txtResult.setText("");
			String timeStamp_now = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
			clusterPath = clusterPath + "_run_" + nRun + "_" + timeStamp_now + ".txt";
			FileOutputStream foutlog=new FileOutputStream(clusterPath);
			
			addResultText("\nClusters With the Obtained OLP :\n");
			
			//initialize clusters
			for(int t=0;t<nDocuments;t++)
			{
				Clusters.addItemset(new Itemset(""+t));
			}
			
			sim=new double[nDocuments][nDocuments][1];
			sim_perc=new double[nDocuments][nDocuments][1];
			
			for(int t=0;t<=nDocuments-1;t++)
			{
				for(int j=0;j<=nDocuments-1;j++)
				{
					double hratio=findSimilarity(documents[t],documents[j]);
					Itemset i1=new Itemset();
					i1.addItem(""+t);
					i1.addItem(""+j);
					i1.addItem(""+hratio);
					Similarities.addItemset(i1);
					
					addResultText("\n Document Pair("+t+","+j+") \t Similarity Score: "+hratio*100+"\n");
					logText = "\n Document Pair("+t+","+j+") \t Similarity Score: "+hratio*100+"\n";
					foutlog.write(logText.getBytes());
					
					sim[t][j][0]=hratio;
					sim_perc[t][j][0]=hratio*100;
					
					if(hratio>=similarityThreshold)
					{
						String tstr1=""+t;
						String tstr2=""+j;
						int tNewClusterIndex=-1;
						int tOldClusterIndex=-1;
						for(int i=0;i<=Clusters.get_nItemsets()-1;i++)
						{
							if(Clusters.getItemset(i).isContains(tstr1)==true)
							{
								tNewClusterIndex=i;
							}
							if(Clusters.getItemset(i).isContains(tstr2)==true)
							{
								tOldClusterIndex=i;
							}
						}
						if(tNewClusterIndex!=-1&&tOldClusterIndex!=-1)
						{
							Clusters.getItemset(tOldClusterIndex).removeItem(tstr2);
							Clusters.getItemset(tNewClusterIndex).addItem(tstr2);
						}
					}
				}
			}
			addResultText("\n\n");
			logText = "\n\n";
			foutlog.write(logText.getBytes());
			int nClusters=0;
			for(int t=0;t<=Clusters.get_nItemsets()-1;t++)
			{
				if(Clusters.getItemset(t).get_nItems()!=0)
				{
					addResultText("Cluster"+(nClusters+1)+": "+Clusters.getItemset(t).toString()+"\n");
					logText = "Cluster"+(nClusters+1)+": "+Clusters.getItemset(t).toString()+"\n";
					foutlog.write(logText.getBytes());
					nClusters+=1;
				}
			}
			addResultText("\nNumber of clusters formed: "+nClusters);
			logText = "\nNumber of clusters formed: "+nClusters;
			foutlog.write(logText.getBytes());
			foutlog.close();
		}
		catch(Exception e)
		{
			txtMessage.append("Error Occured: \n");
			txtMessage.append(e+"\n");		
		}
	}
	
	double findSimilarity(WebDocument d1,WebDocument d2)
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
			double tfidf1=findTFIDF(doc1.DIG.V.getItem(t),d1);
			double tfidf2=findTFIDF(doc1.DIG.V.getItem(t),d2);
			sigma1+=tfidf1*tfidf2;
			sigma21+=tfidf1*tfidf1;
			sigma22+=tfidf2*tfidf2;
		}
		
		//consine similarity
		double simt=sigma1/java.lang.Math.sqrt(sigma21*sigma22);

		return(simt);
	}
	
	double findTFIDF(String term,WebDocument d1)
	{
		//find tf
		double n1=d1.findTermFrequency(term);
		double tsum=0.0;
		for(int t=0;t<d1.DIG.V.get_nItems();t++)
		{
			tsum+=d1.findTermFrequency(d1.DIG.V.getItem(t));
		}
		double tf=n1/tsum;
		
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