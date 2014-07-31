
//Queue.java

import java.lang.*;
import java.io.*;
import java.util.*;

public class Queue
{
	String[] Items;
	int nItems;
	
	//constructor
	public Queue()
	{
		int maxItems=30000;
		
		Items=new String[maxItems];
		nItems=0;
	}
	
	//get functions
	public int get_nItems()
	{
		return(nItems);
	}
	
	//set functions
	//
	
	//methods
	public void enqueue(String tItem)
	{
		if(tItem.length()>0)
		{
			Items[nItems]=tItem;
			nItems=nItems+1;
		}
	}
	
	public String dequeue()
	{
		String tItem="";

		if(nItems>0)
		{
			tItem=Items[0];
			for(int j=0;j<nItems-1;j++)
			{
				Items[j]=Items[j+1];
			}
			nItems=nItems-1;
		}

		return(tItem);		
	}
	
	public void clear()
	{
		nItems=0;
	}
	
	public boolean isEmpty()
	{
		return(nItems==0?true:false);
	}
	
	public boolean isContains(String tItem)
	{
		boolean flag=false;
		
		for(int t=0;t<=nItems-1;t++)
		{
			if(tItem.compareTo(Items[t])==0)
			{
				flag=true;
				break;
			}
		}
		
		return(flag);
	}

	public String toString()
	{
		String tStr="";
		int t;

		for(t=0;t<=nItems-1;t++)
		{
			tStr=tStr+Items[t]+(t==nItems-1?"":", ");
		}
		tStr="{"+tStr.trim()+"}";

		return(tStr);
	}
}
