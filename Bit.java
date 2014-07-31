
//Bit.java

import java.io.*;
import java.lang.*;
import java.util.*;

////////////////////Bit class
class Bit
{
	//bit operations
	public static int Power(int tBase,int tExponent)
	{
		int tAns=1,t;
		for(t=1;t<=tExponent;t++)
		{
			tAns=tAns*tBase;
		}
		return(tAns);
	}

	public static int GetBit(int tValue,int tPos)
	{
		int tBit=0;
		tBit=tValue&Power(2,tPos);
		if(tBit>0) tBit=1;
		return(tBit);
	}

	public static String DecToBin(int tValue,int tLength)
	{
		String tBitStr="";
		int t;
		for(t=0;t<=tLength-1;t++)
		{
			tBitStr=GetBit(tValue,t)+tBitStr;
		}
		return(tBitStr);
	}

	public static int GetBitOnCount(int tValue,int tLength)
	{
		String tBitStr;
		int t,tCount=0;
		tBitStr=DecToBin(tValue,tLength);
		for(t=1;t<=tLength;t++)
		{
			if(tBitStr.substring(t-1,t).equals("1")==true)
			{
				tCount=tCount+1;
			}
		}
		return(tCount);
	}
}
