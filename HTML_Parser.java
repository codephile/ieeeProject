
//HTML_Parser.java

import java.lang.*;
import java.io.*;

////TagBlock
class TagBlock
{
	int start;
	int end;
	int depth;
	String text;
	
	//constructor
	TagBlock()
	{
		start=-1;
		end=-1;
		depth=0;
	}
	
	//get functions
	int get_start()
	{
		return(start);
	}
	
	int get_end()
	{
		return(end);
	}
	
	int get_depth()
	{
		return(depth);
	}
	
	String get_text()
	{
		return(text);
	}
	
	//set functions
	void set_start(int tstart)
	{
		start=tstart;		
	}
	
	void set_end(int tend)
	{
		end=tend;
	}
	
	void set_depth(int tdepth)
	{
		depth=tdepth;
	}
	
	void set_text(String tText)
	{
		text=tText;
	}
}

////HTML_Script
class HTML_Script
{
	private String filePath;
	String code;
	String lines[];
	int nLines;
	
	String BlockStart;
	String BlockEnd;
	TagBlock blocks[];
	int nBlocks;
	
	//constructor
	public HTML_Script()
	{
		filePath="";
		int maxLines=100000;
		int maxBlocks=100000;

		lines=new String[maxLines];
		nLines=0;
		//System.out.println("\n Initializing tag-blocks");
		blocks=new TagBlock[maxBlocks];
		nBlocks=0;
		for(int t=0;t<maxBlocks;t++) 
		{
			blocks[t]=new TagBlock();
			//System.out.println("\n Assigned t = " + (t + 1));
		}
		BlockStart="";
		BlockEnd="";
	}
	
	//get functions
	public String get_filePath()
	{
		return(filePath);
	}
	
	public String get_code()
	{
		return(code);
	}
	
	public int get_nLines()
	{
		return(nLines);
	}
	
	public String getLine(int tIndex)
	{
		String tLine="";
		
		if(nLines>0)
		{
			if(tIndex>=0&&tIndex<=nLines-1)
			{
				tLine=lines[tIndex];
			}
		}
		
		return(tLine);
	}
	
	public int get_nBlocks()
	{
		return(nBlocks);
	}
	
	public TagBlock getBlock(int tIndex)
	{
		TagBlock tBlock=new TagBlock();
		
		if(nBlocks>0)
		{
			if(tIndex>=0&&tIndex<=nBlocks-1)
			{
				tBlock=blocks[tIndex];
			}
		}
		
		return(tBlock);
	}
	
	public String getBlockStart()
	{
		return(BlockStart);
	}
	
	public String getBlockEnd()
	{
		return(BlockEnd);
	}
	
	//set functions
	public void set_filePath(String tFilePath)
	{
		filePath=tFilePath;
	}
	
	public void setBlockStart(String tBlockStart)
	{
		BlockStart=tBlockStart;
	}
	
	public void setBlockEnd(String tBlockEnd)
	{
		BlockEnd=tBlockEnd;
	}
	
	//methods
	public void read()
	{
		//System.out.print("Reading ["+filePath+"]...\n");
		nLines=0;
		nBlocks=0;
		code="";
		
		try
		{
			FileInputStream fin;
			fin=new FileInputStream(filePath);
			int ch=0;
			
			String tstr="";
			while((ch=fin.read())!=-1)
			{
				//System.out.println(ch);
				if(ch==13)
				//if(ch==10)
				{
					lines[nLines]=tstr;
					nLines++;
					//System.out.println("\n Read Line : " + nLines);
					tstr="";					
					ch=fin.read();
					code+=(char)ch;
					continue;
				}
				tstr+=(char)ch;
				code+=(char)ch;
			}
		}
		catch(Exception e)
		{
			System.out.println("Error: "+e.getMessage());
		}
	}
	
	public void findBlocks()
	{
		int blockIndex;
		int currentPosition=0;
		int tdepth;
		int tstart,tend;
		//System.out.println("\n Finding blocks");
		nBlocks=0;
		String tcode=code.toLowerCase();
		blockIndex=tcode.indexOf(BlockStart,currentPosition);
		while(blockIndex!=-1)
		{
			currentPosition=blockIndex+BlockStart.length();
			tdepth=1;
			nBlocks++;
			blocks[nBlocks-1].set_start(currentPosition);
			int tend1=tcode.indexOf(BlockEnd,currentPosition);
			blocks[nBlocks-1].set_end(tend1);
			blocks[nBlocks-1].set_depth(tdepth);
			blocks[nBlocks-1].set_text(BlockStart+code.substring(currentPosition,tend1)+BlockEnd);
			
			do
			{
				tend=tcode.indexOf(BlockEnd,currentPosition);
				tstart=tcode.indexOf(BlockStart,currentPosition);
				if(tend==-1) break;
				
				if(tstart<tend&&tstart!=-1)
				{
					currentPosition=tstart+BlockStart.length();
					tdepth=tdepth+1;
					
					nBlocks++;
					blocks[nBlocks-1].set_start(tstart);
					blocks[nBlocks-1].set_end(tend);
					blocks[nBlocks-1].set_depth(tdepth);
					blocks[nBlocks-1].set_text(BlockStart+code.substring(tstart,tend)+BlockEnd);
				}
				else
				{
					currentPosition=tend+BlockStart.length();
					tdepth=tdepth-1>=0?tdepth-1:0;
				}
				if(tdepth==0) break;
			}while(true);
			
			blockIndex=tcode.indexOf(BlockStart,currentPosition);
			//System.out.println("\n nBlocks" + nBlocks);
		}
	}
}

////HTML_Parser
class HTML_Parser
{
	String FilePath;
	HTML_Script script;
	
	//constructor
	public HTML_Parser()
	{
		FilePath="";
		script=new HTML_Script();
	}
	
	//get functions
	public String getFilePath()
	{
		return(FilePath);
	}
	
	//set functions
	public void setFilePath(String tFilePath)
	{
		FilePath=tFilePath;
	}
	
	//methods
	public Queue findLinks()
	{
		//System.out.println("\n Finding links");
		script.set_filePath(FilePath);
		script.read();
		script.setBlockStart("<a href");
		script.setBlockEnd("</a>");
		script.findBlocks();
		
		Queue links=new Queue();
		links.clear();
		//find href links
		for(int t=0;t<script.get_nBlocks();t++)
		{
			String tStr=script.getBlock(t).get_text();
			String tLink=tStr.substring(tStr.indexOf("=")+1);
			tLink=tLink.substring(0,tLink.indexOf(">"));
			if(tLink.indexOf("?")!=-1) //if GET parameters found in link
			{
				tLink=tLink.substring(0,tLink.indexOf("?"));
			}
			tLink=tLink.trim();
			tLink=tLink.toLowerCase();
			tLink=StringUtils.replaceString(tLink,"\"","");
			//System.out.println("\n Link found to : data\\" + tLink);
			if(links.isContains(tLink)==false) links.enqueue(tLink);
		}
		
		//find form action links
		script.setBlockStart("<form");
		script.setBlockEnd(">");
		script.findBlocks();
		for(int t=0;t<script.get_nBlocks();t++)
		{
			String tStr=script.getBlock(t).get_text();
			tStr=tStr.toLowerCase();
			if(tStr.indexOf("action")!=-1)
			{
				tStr=tStr.substring(tStr.indexOf("action")+6);
				String tLink=tStr.substring(tStr.indexOf("=")+1);
				tLink=tLink.substring(0,tLink.indexOf(">"));
				if(tLink.indexOf(" ")!=-1) //if GET parameters found in link
				{
					tLink=tLink.substring(0,tLink.indexOf(" "));
				}
				tLink=tLink.trim();
				tLink=tLink.toLowerCase();
				tLink=StringUtils.replaceString(tLink,"\"","");
				
				if(links.isContains(tLink)==false) links.enqueue(tLink);
			}
		}
		
		return(links);
	}
	
	public Queue findMetas()
	{
		script.set_filePath(FilePath);
		script.read();
		script.setBlockStart("<meta");
		script.setBlockEnd("\">");
		script.findBlocks();
		
		Queue links=new Queue();
		for(int t=0;t<script.get_nBlocks();t++)
		{
			String tStr=script.getBlock(t).get_text();
			String tLink=tStr.substring(tStr.indexOf("content=\""));
			tLink=tLink.substring(0,tLink.indexOf(">"));
			tLink=tLink.trim();
			tLink=StringUtils.replaceString(tLink,"content","");
			tLink=StringUtils.replaceString(tLink,"=","");
			tLink=StringUtils.replaceString(tLink,"\"","");
			links.enqueue(tLink);
		}
		
		return(links);
	}
	
	public Queue findBody()
	{
		script.set_filePath(FilePath);
		script.read();
		script.setBlockStart("<body");
		script.setBlockEnd("body>");
		script.findBlocks();
		
		Queue links=new Queue();
		for(int t=0;t<script.get_nBlocks();t++)
		{
			String tStr=script.getBlock(t).get_text();
			tStr=tStr.trim();
			tStr=StringUtils.replaceString(tStr,"<","");
			tStr=StringUtils.replaceString(tStr,"=","");
			tStr=StringUtils.replaceString(tStr,"\"","");
			tStr=StringUtils.replaceString(tStr,">","");
			links.enqueue(tStr);
		}
		
		return(links);
	}

	public Queue findStopWords()
	{
		script.set_filePath(FilePath);
		script.read();
		script.setBlockStart("<html");
		script.setBlockEnd("html>");
		script.findBlocks();
		
		Queue links=new Queue();
		for(int t=0;t<script.get_nBlocks();t++)
		{
			String tStr=script.getBlock(t).get_text();
			tStr=tStr.trim();
			tStr=StringUtils.replaceString(tStr,"<","");
			tStr=StringUtils.replaceString(tStr,">","");
			tStr=StringUtils.replaceString(tStr,"}","");
			tStr=StringUtils.replaceString(tStr,"{","");
			tStr=StringUtils.replaceString(tStr,"html","");
			tStr=StringUtils.replaceString(tStr,"\\/","");
			links.enqueue(tStr);
		}
		
		return(links);
	}		
	
	public String toString()
	{
		String tStr="";
		
		tStr=tStr+Integer.toString(script.get_nBlocks())+" blocks\n\n";
		for(int t=0;t<script.get_nBlocks();t++)
		{
			tStr=tStr+"(";
			tStr=tStr+Integer.toString(script.getBlock(t).get_start())+",";
			tStr=tStr+Integer.toString(script.getBlock(t).get_end())+",";
			tStr=tStr+Integer.toString(script.getBlock(t).get_depth())+",";
			tStr=tStr+"["+script.getBlock(t).get_text()+"]";
			tStr=tStr+")\n";
		}
		
		return(tStr);
	}
}
