
//WebDocument.java

import java.lang.*;
import java.io.*;

public class WebDocument
{
	String DocName;
	Itemset Phrases;
	DocumentIndexGraph DIG;
	
	//constructor
	public WebDocument()
	{
		DocName="";
		Phrases=new Itemset();
		DIG=new DocumentIndexGraph();
	}
	
	//get functions
	public String getDocName()
	{
		return(DocName);
	}
	
	public Itemset getPhrases()
	{
		return(Phrases);
	}
	
	public DocumentIndexGraph getDIG()
	{
		return(DIG);
	}
	
	public String getPhrase(int tIndex)
	{
		String tPhrase="";
		
		if(tIndex>=0&&tIndex<=Phrases.get_nItems()-1)
		{
			tPhrase=Phrases.getItem(tIndex);
		}
		
		return(tPhrase);
	}
	
	public int get_nPhrases()
	{
		return(Phrases.get_nItems());
	}
	
	//set functions
	public void setDocName(String tDocName)
	{
		DocName=tDocName;
	}
	
	public void setPhrases(Itemset tPhrases)
	{
		Phrases=tPhrases;
	}
	
	public void setDIG(DocumentIndexGraph tDIG)
	{
		DIG=tDIG;
	}
	
	//methods
	public void addPhrase(String tPhrase)
	{
		Phrases.addItem(tPhrase);
	}
	
	public int findPhraseFrequency(String tPhrase)
	{
		String tstr=Phrases.toString();
		tstr=StringUtils.replaceString(tstr,"{","");
		tstr=StringUtils.replaceString(tstr,"}","");
		String tarr[]=StringUtils.split(tstr,", ");
		ItemsetCollection ic1=new ItemsetCollection(tarr);
		int tFrequency=ic1.getSupportCount(new Itemset(tPhrase));
		return(tFrequency);
	}
	
	public int findTermFrequency(String tTerm)
	{
		ItemsetCollection ic1=new ItemsetCollection();
		for(int t=0;t<Phrases.get_nItems();t++)
		{
			ic1.addItemset(new Itemset(StringUtils.split(Phrases.getItem(t)," ")));
		}
		int tFrequency=ic1.getSupportCount(new Itemset(tTerm));
		return(tFrequency);
	}
}
