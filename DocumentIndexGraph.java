
//DocumentIndexGraph.java

import java.lang.*;
import java.io.*;

public class DocumentIndexGraph
{
	Itemset V;
	ItemsetCollection E;
	
	//constructor
	public DocumentIndexGraph()
	{
		V=new Itemset();
		E=new ItemsetCollection();
	}
	
	//get functions
	public Itemset getV()
	{
		return(V);
	}
	
	public ItemsetCollection getE()
	{
		return(E);
	}
	
	//set functions
	public void setV(Itemset tItemset)
	{
		V.clear();
		V.appendItemset(tItemset);
	}
	
	public void setE(ItemsetCollection tItemsetCollection)
	{
		E.clear();
		E.appendItemsetCollection(tItemsetCollection);
	}
	
	//methods
	public void addNode(String tWord)
	{
		V.addItem(tWord);
	}
	
	public void addEdge(Itemset tEdge)
	{
		E.addItemset(tEdge);
	}
	
	public boolean isEdge(String str1,String str2)
	{
		boolean flag=false;
		
		for(int t=0;t<=E.get_nItemsets()-1;t++)
		{
			String tstr1=E.getItemset(t).getItem(0);
			String tstr2=E.getItemset(t).getItem(1);
			if(str1.compareToIgnoreCase(tstr1)==0&&str2.compareToIgnoreCase(tstr2)==0)
			{
				flag=true;
				break;
			}
		}
		
		return(flag);
	}
	
	public boolean isPath(String str)
	{
		String tarr[]=StringUtils.split(str," ");
		boolean flag=true;
		
		for(int t=0;t<=tarr.length-2;t++)
		{
			if(isEdge(tarr[t],tarr[t+1])==false)
			{
				flag=false;
				break;
			}
		}
		
		return(flag);
	}

	public double findPhrasePathWeight(String str)
	{
		String tarr[]=StringUtils.split(str," ");
		int tCount=0;
		
		for(int t=0;t<=tarr.length-2;t++)
		{
			if(isEdge(tarr[t],tarr[t+1])==true)
			{
				tCount+=1;
			}
		}
		double weight=(double)tCount/(double)(V.get_nItems());
		
		return(weight);
	}
}
