
//ItemsetCollection.java

import java.lang.*;
import java.io.*;
import java.util.*;

////////////////////ItemsetCollection class
class ItemsetCollection
{
	ArrayList Itemsets;
	static boolean printStatus=false;

	//constructors
	public ItemsetCollection()
	{
		Itemsets=new ArrayList();
	}
	
	public ItemsetCollection(Itemset tItemset)
	{
		Itemsets=new ArrayList();
		Itemsets.add(tItemset);
	}
	
	public ItemsetCollection(String[] tarr)
	{
		Itemsets=new ArrayList();
		for(int t=0;t<tarr.length;t++)
		{
			Itemsets.add(new Itemset(tarr[t]));
		}
	}
	
	//get functions
	public int get_nItemsets()
	{
		return(Itemsets.size());
	}

	public Itemset getItemset(int tIndex)
	{
		Itemset tItemset=new Itemset();

		if(tIndex>=0&&tIndex<=Itemsets.size()-1)
		{
			tItemset=(Itemset)Itemsets.get(tIndex);
		}

		return(tItemset);
	}
	
	//set functions
	public void setItemsets(ItemsetCollection tItemsetCollection)
	{
		clear();
		for(int t=0;t<=tItemsetCollection.get_nItemsets()-1;t++)
		{
			addItemset(tItemsetCollection.getItemset(t));
		}
	}
	
	//methods
	public void addItemset(Itemset tItemset)
	{
		Itemset i1=new Itemset();
		for(int t=0;t<tItemset.get_nItems();t++) i1.addItem(tItemset.getItem(t));
		Itemsets.add(i1);
	}

	public void appendItemsetCollection(ItemsetCollection tItemsetCollection)
	{
		int t;
		for(t=0;t<=tItemsetCollection.get_nItemsets()-1;t++)
		{
			addItemset(tItemsetCollection.getItemset(t));
		}
	}

	public void removeItemset(Itemset tItemset)
	{
		for(int i=0;i<=Itemsets.size()-1;i++)
		{
			if(getItemset(i).isEquals(tItemset)==true)
			{
				Itemsets.remove(i);
				break;
			}
		}
	}
	
	public void removeItemset(int tIndex)
	{
		if(tIndex>=0&&tIndex<=Itemsets.size()-1)
		{
			removeItemset(getItemset(tIndex));
		}
	}

	public void removeItemsetCollection(ItemsetCollection tItemsetCollection)
	{
		for(int t=0;t<=tItemsetCollection.get_nItemsets()-1;t++)
		{
			removeItemset(tItemsetCollection.getItemset(t));
		}
	}
	
	public void removeEmptyItemsets()
	{
		for(int t=0;t<=Itemsets.size()-1;t++)
		{
			if(getItemset(t).get_nItems()==0)
			{
				removeItemset(t);
			}
		}
	}
	
	public void clear()
	{
		Itemsets.clear();
	}

	public Itemset getUniqueItemset()
	{
		Itemset tItemset=new Itemset();

		for(int i=0;i<=Itemsets.size()-1;i++)
		{
			for(int j=0;j<=getItemset(i).get_nItems()-1;j++)
			{
				if(tItemset.isContains(getItemset(i).getItem(j))==false)
				{
					tItemset.addItem(getItemset(i).getItem(j));
				}
			}
		}

		return(tItemset);
	}

	public ItemsetCollection getUniqueItemsetCollection()
	{
		ItemsetCollection ic1=new ItemsetCollection();
		
		for(int i=0;i<=Itemsets.size()-1;i++)
		{
			if(ic1.isContains(getItemset(i))==false)
			{
				ic1.addItemset(getItemset(i));
			}
		}
		
		return(ic1);
	}
	
	public double getSupport(String tItem)
	{
		int t,tCount=0;
		double tSupport;

		for(t=0;t<=Itemsets.size()-1;t++)
		{
			if(getItemset(t).isContains(tItem)==true)
			{
				tCount=tCount+1;
			}
		}
		tSupport=((double)tCount/(double)Itemsets.size())*100.0;
		tSupport=Math.round(tSupport);

		return(tSupport);
	}

	public double getSupport(Itemset tItemset)
	{
		int t,tCount=0;
		double tSupport;

		for(t=0;t<=Itemsets.size()-1;t++)
		{
			if(getItemset(t).isContains(tItemset)==true)
			{
				tCount=tCount+1;
			}
		}
		tSupport=((double)tCount/(double)Itemsets.size())*100.0;
		tSupport=Math.round(tSupport);

		return(tSupport);
	}
	
	public int getSupportCount(Itemset tItemset)
	{
		int t,tCount=0;

		for(t=0;t<=Itemsets.size()-1;t++)
		{
			if(getItemset(t).isContains(tItemset)==true)
			{
				tCount=tCount+1;
			}
		}

		return(tCount);
	}
	
	public boolean isContains(Itemset tItemset)
	{
		boolean found=false;

		for(int t=0;t<=Itemsets.size()-1;t++)
		{
			if(getItemset(t).isContains(tItemset)==true)
			{
				found=true;
				break;
			}
		}

		return(found);
	}

	public String toString()
	{
		String tStr="";
		
		for(int t=0;t<=Itemsets.size()-1;t++)
		{
			tStr=tStr+getItemset(t).toString()+"\n\r\n\r";
			if(printStatus==true)
			{
				System.out.print(t+" transactions, "+(tStr.length()/1024)+"k...\r");
			}
		}
		
		return(tStr);
	}

	public String toString1()
	{
		String tStr="";
		
		for(int t=0;t<=Itemsets.size()-1;t++)
		{
			tStr=tStr+getItemset(t).toString()+"\n";
			if(printStatus==true)
			{
				System.out.print(t+" transactions, "+(tStr.length()/1024)+"k...\r");
			}
		}
		
		return(tStr);
	}
}
