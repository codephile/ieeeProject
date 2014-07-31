
//Itemset.java

import java.lang.*;
import java.io.*;
import java.util.*;

////////////////////Itemset class
class Itemset
{
	ArrayList Items;

	//constructors
	public Itemset()
	{
		Items=new ArrayList();
	}
	
	public Itemset(String tItem)
	{
		Items=new ArrayList();
		Items.add(tItem);
	}
	
	public Itemset(String[] tarr)
	{
		Items=new ArrayList();
		for(int t=0;t<tarr.length;t++)
		{
			Items.add(tarr[t]);
		}
	}
	
	//get functions
	public int get_nItems()
	{
		return(Items.size());
	}

	public String getItem(int tIndex)
	{
		String tStr="";

		if(tIndex>=0&&tIndex<=Items.size()-1)
		{
			tStr=(String)Items.get(tIndex);
		}

		return(tStr);
	}

	//set functions
	public void setItems(Itemset tItemset)
	{
		clear();
		for(int t=0;t<=tItemset.get_nItems()-1;t++)
		{
			addItem(tItemset.getItem(t));
		}
	}
	
	//methods
	public void addItem(String tItem)
	{
		Items.add(tItem);
	}

	public void appendItemset(Itemset tItemset)
	{
		for(int t=0;t<=tItemset.get_nItems()-1;t++)
		{
			addItem(tItemset.getItem(t));
		}
	}

	public void removeItem(String tItem)
	{
		for(int i=0;i<=Items.size()-1;i++)
		{
			if(Items.get(i).equals(tItem)==true)
			{
				Items.remove(i);
				break;
			}
		}
	}
	
	public void removeItem(int tIndex)
	{
		if(tIndex>=0&&tIndex<=Items.size()-1)
		{
			removeItem((String)Items.get(tIndex));
		}
	}

	public void removeItemset(Itemset tItemset)
	{
		for(int t=0;t<=tItemset.get_nItems()-1;t++)
		{
			removeItem(tItemset.getItem(t));
		}
	}

	public void clear()
	{
		Items.clear();
	}

	public int getEqualCount(Itemset tItemset)
	{
		int tCount=0;

		for(int t=0;t<Items.size();t++)
		{
			for(int j=0;j<tItemset.get_nItems();j++)
			{
				if(getItem(t).equals(tItemset.getItem(j))==true)
				{
					tCount=tCount+1;
				}
			}
		}

		return(tCount);
	}

	public Itemset getEqualItemset(Itemset tItemset)
	{
		Itemset tAnsItemset=new Itemset();

		for(int t=0;t<Items.size();t++)
		{
			for(int j=0;j<tItemset.get_nItems();j++)
			{
				if(getItem(t).equals(tItemset.getItem(j))==true)
				{
					tAnsItemset.addItem(getItem(t));
				}
			}
		}

		return(tAnsItemset);
	}

	public boolean isContains(String tItem)
	{
		boolean found=false;

		for(int t=0;t<=Items.size()-1;t++)
		{
			if(getItem(t).equals(tItem)==true)
			{
				found=true;
				break;
			}
		}

		return(found);
	}

	public boolean isContains(Itemset tItemset)
	{
		int tCount=0;
		boolean found=false;

		for(int i=0;i<=tItemset.get_nItems()-1;i++)
		{
			for(int j=0;j<=Items.size()-1;j++)
			{
				if(tItemset.getItem(i).equals(getItem(j))==true)
				{
					tCount=tCount+1;
					break;
				}
			}
		}
		if(tCount==tItemset.get_nItems())
		{
			found=true;
		}

		return(found);
	}
	
	public boolean isEquals(Itemset tItemset)
	{
		boolean flag=true;
		
		for(int i=0;i<=tItemset.get_nItems()-1;i++)
		{
			for(int j=0;j<=Items.size()-1;j++)
			{
				System.out.println("["+tItemset.getItem(i)+","+getItem(j)+"]");
				if(tItemset.getItem(i).equals(getItem(j))==false)
				{
					System.out.println("["+tItemset.getItem(i)+","+getItem(j)+"]");
					flag=false;
					break;
				}
			}
		}
		
		return(flag);
	}

	public ItemsetCollection GetSubSets(int tItemCount)
	{
		ItemsetCollection tItemsetCollection=new ItemsetCollection();
		Itemset tItemset=new Itemset();

		tItemsetCollection.clear();
		String tBitStr;
		for(int t=1;t<=Bit.Power(2,Items.size());t++)
		{
			if(tItemCount==0||Bit.GetBitOnCount(t,Items.size())==tItemCount)
			{
				tBitStr=Bit.DecToBin(t,Items.size());
				tItemset.clear();
				for(int i=1;i<=tBitStr.length();i++)
				{
					if(tBitStr.substring(i-1,i).equals("1")==true)
					{
						tItemset.addItem(getItem(i-1));
					}
				}
				tItemsetCollection.addItemset(tItemset);
			}
		}

		return(tItemsetCollection);
	}

	public String toString()
	{
		String tStr="";

		for(int t=0;t<=Items.size()-1;t++)
		{
			tStr=tStr+getItem(t)+(t==Items.size()-1?"":", ");
		}
		tStr="{"+tStr.trim()+"}";

		return(tStr);
	}
}
