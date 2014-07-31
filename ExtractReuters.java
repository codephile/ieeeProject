/**
* Functions perfectly
**/
     
     import java.io.BufferedReader;
     import java.io.File;
     import java.io.FileFilter;
     import java.io.FileReader;
     import java.io.FileWriter;
     import java.io.IOException;
     import java.util.regex.Matcher;
     import java.util.regex.Pattern;
	 import java.util.*;
     
     
     /**
      * Split the Reuters SGML documents into Simple Text files containing: Title, Date, Dateline, Body
      */
     public class ExtractReuters
     {
        private File reutersDir;
         private File outputDir;
         private static final String LINE_SEPARATOR = System.getProperty("line.separator");
     
         public ExtractReuters(File reutersDir, File outputDir)
         {
             this.reutersDir = reutersDir;
             this.outputDir = outputDir;
             System.out.println("Deleting all files in " + outputDir);
             File [] files = outputDir.listFiles();
             for (int i = 0; i < files.length; i++)
             {
                 files[i].delete();
             }
     
        }
     
         public void extract()
         {
             File [] sgmFiles = reutersDir.listFiles(new FileFilter()
            {
                 public boolean accept(File file)
                 {
                     return file.getName().endsWith(".sgm");
                 }
             });
             if (sgmFiles != null && sgmFiles.length > 0)
             {
                 for (int i = 0; i < sgmFiles.length; i++)
                 {
                     File sgmFile = sgmFiles[i];
                     extractFile(sgmFile);
                 }
             }
             else
            {
                 System.err.println("No .sgm files in " + reutersDir);
             }
         }
		 
		public void make_index()
		{
			try
			{
				String fileName;
				File indexFile = new File(outputDir, "index.html");
				FileWriter indexWriter = new FileWriter(indexFile, true);
				
				
				File [] htmlFiles = outputDir.listFiles(new FileFilter()
				{
					public boolean accept(File file)
					{
						return file.getName().endsWith(".html");
					}
				});
				fileName = "<table>";
				indexWriter.write(fileName);
				if (htmlFiles != null && htmlFiles.length > 0)
				{
					for (int i = 0; i < htmlFiles.length; i++)
					{
						File htmlFile = htmlFiles[i];
						fileName = "\n\t<tr>\n\t\t<td><a href="+htmlFile.getName()+">"+htmlFile.getName()+"</a>\n\t\t</td>\n\t</tr>";
						indexWriter.write(fileName);
						fileName = "";
					}
					fileName = "\n</table>";
				}
				else
				{
					System.err.println("No .html files in " + reutersDir);
				}	
				indexWriter.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
    
         Pattern EXTRACTION_PATTERN = Pattern.compile("<TITLE>(.*?)</TITLE>");
		 Pattern EXTRACTION_PATTERN_1 = Pattern.compile("<BODY>(.*?)</BODY>");
		 Pattern EXTRACTION_PATTERN_MULTIPLE = Pattern.compile("<D>(.*?)</D>");
		 Pattern EXTRACTION_PATTERN_TOPICS = Pattern.compile("<TOPICS>(.*?)</TOPICS>");
		 Pattern EXTRACTION_PATTERN_PLACES = Pattern.compile("<PLACES>(.*?)</PLACES>");
		 Pattern EXTRACTION_PATTERN_PEOPLE = Pattern.compile("<PEOPLE>(.*?)</PEOPLE>");
		 Pattern EXTRACTION_PATTERN_ORGS = Pattern.compile("<ORGS>(.*?)</ORGS>");
		 Pattern EXTRACTION_PATTERN_EXCHANGES = Pattern.compile("<EXCHANGES>(.*?)</EXCHANGES>");
		 Pattern EXTRACTION_PATTERN_COMPANIES = Pattern.compile("<COMPANIES>(.*?)</COMPANIES>");
		 Pattern EXTRACTION_PATTERN_UNKNOWN = Pattern.compile("<UNKNOWN>(.*?)</UNKNOWN>");
     
         private static String[] META_CHARS
                = {"&", "<", ">", "\"", "'"};
     
         private static String[] META_CHARS_SERIALIZATIONS
                 = {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};
     
        /**
          * Override if you wish to change what is extracted
          *
          * @param sgmFile
          */
         protected void extractFile(File sgmFile)
         {
             try
             {
                 BufferedReader reader = new BufferedReader(new FileReader(sgmFile));
     
                 StringBuffer buffer = new StringBuffer(1024);
                 StringBuffer outBuffer = new StringBuffer(1024);
				 StringBuffer tempBuffer = new StringBuffer(1024);
				 StringBuffer htmlBuffer = new StringBuffer(1024);
     
                 String line = null;
                 int index = -1;
                 int docNumber = 0;
				 int matches = 0;
                 while ((line = reader.readLine()) != null)
                 {
                     //when we see a closing reuters tag, flush the file
     
                     if ((index = line.indexOf("</REUTERS")) == -1)
                     {
                         //Replace the SGM escape sequences
     
                         buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                     }
                     else
                     {
                         //Extract the relevant pieces and write to a file in the output dir
                         Matcher matcher = EXTRACTION_PATTERN.matcher(buffer);
                         outBuffer.append("<html>\n\t<head>\n\t\t<title>");
                         while (matcher.find())
                         {
                             for (int i = 1; i <= matcher.groupCount(); i++)
                             {
                                 if (matcher.group(i) != null)
                                 {
                                     outBuffer.append(matcher.group(i));
                                 }
                             }
							 outBuffer.append("</title>");
                             //outBuffer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
                         }
                         String out = outBuffer.toString();
                         for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                         {
                             out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                         }
						 out = out.toLowerCase();
                        File outFile = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         //System.out.println("Writing " + outFile);
                         FileWriter writer = new FileWriter(outFile, true);
                         writer.write(out);
                         writer.close();
                         outBuffer.setLength(0);
                         buffer.setLength(0);
                     }
                 }
                 reader.close();

      			BufferedReader reader2 = new BufferedReader(new FileReader(sgmFile));
				 
                buffer.setLength(0);
                outBuffer.setLength(0);
				htmlBuffer.setLength(0);
				tempBuffer.setLength(0);
     
                line = null;
                index = -1;
                docNumber = 0;
				matches = 0;
                while ((line = reader2.readLine()) != null)
                {
                    //when we see a closing reuters tag, flush the file
                    if ((index = line.indexOf("</REUTERS")) == -1)
                    {
                        //Replace the SGM escape sequences
     
                        buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                    }
                    else
                    {
                        //Extract the relevant pieces and write to a file in the output dir
                        Matcher matcher2 = EXTRACTION_PATTERN_TOPICS.matcher(buffer);
                         
                        while (matcher2.find())
                        {
                            for (int i = 1; i <= matcher2.groupCount(); i++)
                            {
                                if (matcher2.group(i) != null)
                                {
                                    outBuffer.append(matcher2.group(i));
                                }
                            }
							Matcher matcher3 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							while(matcher3.find())
							{
								matches = matches + 1;
							}
							Matcher matcher4 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							 
							int internalMatch = 0;
							
								
							while(matcher4.find())
							{
								for (int i = 1; i <= matcher4.groupCount(); i++)
								{
									if (matcher4.group(i) != null)
									{
										tempBuffer.append("topics ");
										tempBuffer.append(matcher4.group(i));
										internalMatch = internalMatch + 1;
										if(internalMatch != matches )
											tempBuffer.append(' ');
									}
								}
							}
							if(tempBuffer.length() != 0)
							{
								htmlBuffer.append("\n\t\t<meta name=\"keywords\" content=\"");
								htmlBuffer.append(tempBuffer);
								htmlBuffer.append("\">");
							}
                             
                        }
                        String out = htmlBuffer.toString();
                        for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                        {
                            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                        }
						
						out = out.toLowerCase();
                        File outFile3 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         
                        FileWriter writer3 = new FileWriter(outFile3, true);
                        writer3.write(out);
                        writer3.close();
                        outBuffer.setLength(0);
                        buffer.setLength(0);
						htmlBuffer.setLength(0);
						tempBuffer.setLength(0);
						matches = 0;
                    }
                }
                reader2.close();

                BufferedReader reader3 = new BufferedReader(new FileReader(sgmFile));
				 
                buffer.setLength(0);
                outBuffer.setLength(0);
				htmlBuffer.setLength(0);
				tempBuffer.setLength(0);
     
                line = null;
                index = -1;
                docNumber = 0;
				matches = 0;
                while ((line = reader3.readLine()) != null)
                {
                    //when we see a closing reuters tag, flush the file
                    if ((index = line.indexOf("</REUTERS")) == -1)
                    {
                        //Replace the SGM escape sequences
     
                        buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                    }
                    else
                    {
                        //Extract the relevant pieces and write to a file in the output dir
                        Matcher matcher5 = EXTRACTION_PATTERN_PLACES.matcher(buffer);
                         
                        while (matcher5.find())
                        {
                            for (int i = 1; i <= matcher5.groupCount(); i++)
                            {
                                if (matcher5.group(i) != null)
                                {
                                    outBuffer.append(matcher5.group(i));
                                }
                            }
							Matcher matcher6 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							while(matcher6.find())
							{
								matches = matches + 1;
							}
							Matcher matcher7 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							 
							int internalMatch = 0;
							
								
							while(matcher7.find())
							{
								for (int i = 1; i <= matcher7.groupCount(); i++)
								{
									if (matcher7.group(i) != null)
									{
										tempBuffer.append("places ");
										tempBuffer.append(matcher7.group(i));
										internalMatch = internalMatch + 1;
										if(internalMatch != matches )
											tempBuffer.append(' ');
									}
								}
							}
							if(tempBuffer.length() != 0)
							{
								htmlBuffer.append("\n\t\t<meta name=\"keywords\" content=\"");
								htmlBuffer.append(tempBuffer);
								htmlBuffer.append("\">");
							}
                             
                        }
                        String out = htmlBuffer.toString();
                        for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                        {
                            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                        }
						
						out = out.toLowerCase();
                        File outFile4 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         
                        FileWriter writer4 = new FileWriter(outFile4, true);
                        writer4.write(out);
                        writer4.close();
                        outBuffer.setLength(0);
                        buffer.setLength(0);
						htmlBuffer.setLength(0);
						tempBuffer.setLength(0);
						matches = 0;
                    }
                }
                reader3.close();	

                BufferedReader reader4 = new BufferedReader(new FileReader(sgmFile));
				 
                buffer.setLength(0);
                outBuffer.setLength(0);
				htmlBuffer.setLength(0);
				tempBuffer.setLength(0);
     
                line = null;
                index = -1;
                docNumber = 0;
				matches = 0;
                while ((line = reader4.readLine()) != null)
                {
                    //when we see a closing reuters tag, flush the file
                    if ((index = line.indexOf("</REUTERS")) == -1)
                    {
                        //Replace the SGM escape sequences
     
                        buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                    }
                    else
                    {
                        //Extract the relevant pieces and write to a file in the output dir
                        Matcher matcher8 = EXTRACTION_PATTERN_PEOPLE.matcher(buffer);
                         
                        while (matcher8.find())
                        {
                            for (int i = 1; i <= matcher8.groupCount(); i++)
                            {
                                if (matcher8.group(i) != null)
                                {
                                    outBuffer.append(matcher8.group(i));
                                }
                            }
							Matcher matcher9 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							while(matcher9.find())
							{
								matches = matches + 1;
							}
							Matcher matcher10 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							 
							int internalMatch = 0;
							
							
							while(matcher10.find())
							{
								for (int i = 1; i <= matcher10.groupCount(); i++)
								{
									if (matcher10.group(i) != null)
									{
										tempBuffer.append("people ");
										tempBuffer.append(matcher10.group(i));
										internalMatch = internalMatch + 1;
										if(internalMatch != matches )
											tempBuffer.append(' ');
									}
								}
							}
							if(tempBuffer.length() != 0)
							{
								htmlBuffer.append("\n\t\t<meta name=\"keywords\" content=\"");
								htmlBuffer.append(tempBuffer);
								htmlBuffer.append("\">");
							}
                             
                        }
                        String out = htmlBuffer.toString();
                        for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                        {
                            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                        }
						
						out = out.toLowerCase();
                        File outFile5 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         
                        FileWriter writer5 = new FileWriter(outFile5, true);
                        writer5.write(out);
                        writer5.close();
                        outBuffer.setLength(0);
                        buffer.setLength(0);
						htmlBuffer.setLength(0);
						tempBuffer.setLength(0);
						matches = 0;
                    }
                }
                reader4.close();

                BufferedReader reader5 = new BufferedReader(new FileReader(sgmFile));
				 
                buffer.setLength(0);
                outBuffer.setLength(0);
				htmlBuffer.setLength(0);
				tempBuffer.setLength(0);
     
                line = null;
                index = -1;
                docNumber = 0;
				matches = 0;
                while ((line = reader5.readLine()) != null)
                {
                    //when we see a closing reuters tag, flush the file
                    if ((index = line.indexOf("</REUTERS")) == -1)
                    {
                        //Replace the SGM escape sequences
     
                        buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                    }
                    else
                    {
                        //Extract the relevant pieces and write to a file in the output dir
                        Matcher matcher8 = EXTRACTION_PATTERN_ORGS.matcher(buffer);
                         
                        while (matcher8.find())
                        {
                            for (int i = 1; i <= matcher8.groupCount(); i++)
                            {
                                if (matcher8.group(i) != null)
                                {
                                    outBuffer.append(matcher8.group(i));
                                }
                            }
							Matcher matcher9 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							while(matcher9.find())
							{
								matches = matches + 1;
							}
							Matcher matcher10 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							 
							int internalMatch = 0;
							
							
							while(matcher10.find())
							{
								for (int i = 1; i <= matcher10.groupCount(); i++)
								{
									if (matcher10.group(i) != null)
									{
										tempBuffer.append("orgs ");
										tempBuffer.append(matcher10.group(i));
										internalMatch = internalMatch + 1;
										if(internalMatch != matches )
											tempBuffer.append(' ');
									}
								}
							}
							if(tempBuffer.length() != 0)
							{
								htmlBuffer.append("\n\t\t<meta name=\"keywords\" content=\"");
								htmlBuffer.append(tempBuffer);
								htmlBuffer.append("\">");
							}
                             
                        }
                        String out = htmlBuffer.toString();
                        for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                        {
                            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                        }
						
						out = out.toLowerCase();
                        File outFile5 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         
                        FileWriter writer5 = new FileWriter(outFile5, true);
                        writer5.write(out);
                        writer5.close();
                        outBuffer.setLength(0);
                        buffer.setLength(0);
						htmlBuffer.setLength(0);
						tempBuffer.setLength(0);
						matches = 0;
                    }
                }
                reader5.close();				
             		

                BufferedReader reader6 = new BufferedReader(new FileReader(sgmFile));
				 
                buffer.setLength(0);
                outBuffer.setLength(0);
				htmlBuffer.setLength(0);
				tempBuffer.setLength(0);
     
                line = null;
                index = -1;
                docNumber = 0;
				matches = 0;
                while ((line = reader6.readLine()) != null)
                {
                    //when we see a closing reuters tag, flush the file
                    if ((index = line.indexOf("</REUTERS")) == -1)
                    {
                        //Replace the SGM escape sequences
     
                        buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                    }
                    else
                    {
                        //Extract the relevant pieces and write to a file in the output dir
                        Matcher matcher8 = EXTRACTION_PATTERN_EXCHANGES.matcher(buffer);
                         
                        while (matcher8.find())
                        {
                            for (int i = 1; i <= matcher8.groupCount(); i++)
                            {
                                if (matcher8.group(i) != null)
                                {
                                    outBuffer.append(matcher8.group(i));
                                }
                            }
							Matcher matcher9 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							while(matcher9.find())
							{
								matches = matches + 1;
							}
							Matcher matcher10 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							 
							int internalMatch = 0;
							
							
							while(matcher10.find())
							{
								for (int i = 1; i <= matcher10.groupCount(); i++)
								{
									if (matcher10.group(i) != null)
									{
										tempBuffer.append("exchanges ");
										tempBuffer.append(matcher10.group(i));
										internalMatch = internalMatch + 1;
										if(internalMatch != matches )
											tempBuffer.append(' ');
									}
								}
							}
							if(tempBuffer.length() != 0)
							{
								htmlBuffer.append("\n\t\t<meta name=\"keywords\" content=\"");
								htmlBuffer.append(tempBuffer);
								htmlBuffer.append("\">");
							}
                             
                        }
                        String out = htmlBuffer.toString();
                        for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                        {
                            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                        }
						
						out = out.toLowerCase();
                        File outFile5 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         
                        FileWriter writer5 = new FileWriter(outFile5, true);
                        writer5.write(out);
                        writer5.close();
                        outBuffer.setLength(0);
                        buffer.setLength(0);
						htmlBuffer.setLength(0);
						tempBuffer.setLength(0);
						matches = 0;
                    }
                }
                reader6.close();	

                BufferedReader reader7 = new BufferedReader(new FileReader(sgmFile));
				 
                buffer.setLength(0);
                outBuffer.setLength(0);
				htmlBuffer.setLength(0);
				tempBuffer.setLength(0);
     
                line = null;
                index = -1;
                docNumber = 0;
				matches = 0;
                while ((line = reader7.readLine()) != null)
                {
                    //when we see a closing reuters tag, flush the file
                    if ((index = line.indexOf("</REUTERS")) == -1)
                    {
                        //Replace the SGM escape sequences
     
                        buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                    }
                    else
                    {
                        //Extract the relevant pieces and write to a file in the output dir
                        Matcher matcher8 = EXTRACTION_PATTERN_COMPANIES.matcher(buffer);
                         
                        while (matcher8.find())
                        {
                            for (int i = 1; i <= matcher8.groupCount(); i++)
                            {
                                if (matcher8.group(i) != null)
                                {
                                    outBuffer.append(matcher8.group(i));
                                }
                            }
							Matcher matcher9 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							while(matcher9.find())
							{
								matches = matches + 1;
							}
							Matcher matcher10 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							 
							int internalMatch = 0;
							
							while(matcher10.find())
							{
								for (int i = 1; i <= matcher10.groupCount(); i++)
								{
									if (matcher10.group(i) != null)
									{
										tempBuffer.append("companies ");
										tempBuffer.append(matcher10.group(i));
										internalMatch = internalMatch + 1;
										if(internalMatch != matches )
											tempBuffer.append(' ');
									}
								}
							}
							if(tempBuffer.length() != 0)
							{
								htmlBuffer.append("\n\t\t<meta name=\"keywords\" content=\"");
								htmlBuffer.append(tempBuffer);
								htmlBuffer.append("\">");
							}
                             
                        }
                        String out = htmlBuffer.toString();
                        for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                        {
                            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                        }
						
						out = out.toLowerCase();
                        File outFile5 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         
                        FileWriter writer5 = new FileWriter(outFile5, true);
                        writer5.write(out);
                        writer5.close();
                        outBuffer.setLength(0);
                        buffer.setLength(0);
						htmlBuffer.setLength(0);
						tempBuffer.setLength(0);
						matches = 0;
                    }
                }
                reader7.close();		

                BufferedReader reader8 = new BufferedReader(new FileReader(sgmFile));
				 
                buffer.setLength(0);
                outBuffer.setLength(0);
				htmlBuffer.setLength(0);
				tempBuffer.setLength(0);
     
                line = null;
                index = -1;
                docNumber = 0;
				matches = 0;
                while ((line = reader8.readLine()) != null)
                {
                    //when we see a closing reuters tag, flush the file
                    if ((index = line.indexOf("</REUTERS")) == -1)
                    {
                        //Replace the SGM escape sequences
     
                        buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                    }
                    else
                    {
                        //Extract the relevant pieces and write to a file in the output dir
                        Matcher matcher8 = EXTRACTION_PATTERN_UNKNOWN.matcher(buffer);
                         
                        while (matcher8.find())
                        {
                            for (int i = 1; i <= matcher8.groupCount(); i++)
                            {
                                if (matcher8.group(i) != null)
                                {
                                    outBuffer.append(matcher8.group(i));
                                }
                            }
							Matcher matcher9 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							while(matcher9.find())
							{
								matches = matches + 1;
							}
							Matcher matcher10 = EXTRACTION_PATTERN_MULTIPLE.matcher(outBuffer);
							 
							int internalMatch = 0;
							
							
							while(matcher10.find())
							{
								for (int i = 1; i <= matcher10.groupCount(); i++)
								{
									if (matcher10.group(i) != null)
									{
										tempBuffer.append("unknown ");
										tempBuffer.append(matcher10.group(i));
										internalMatch = internalMatch + 1;
										if(internalMatch != matches )
											tempBuffer.append(' ');
									}
								}
							}
							if(tempBuffer.length() != 0)
							{
								htmlBuffer.append("\n\t\t<meta name=\"keywords\" content=\"");
								htmlBuffer.append(tempBuffer);
								htmlBuffer.append("\">");
							}
                             
                        }
                        String out = htmlBuffer.toString();
                        for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                        {
                            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                        }
						
						out = out.toLowerCase();
                        File outFile5 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         
                        FileWriter writer5 = new FileWriter(outFile5, true);
                        writer5.write(out);
                        writer5.close();
                        outBuffer.setLength(0);
                        buffer.setLength(0);
						htmlBuffer.setLength(0);
						tempBuffer.setLength(0);
						matches = 0;
                    }
                }
                reader8.close();

                 BufferedReader reader1 = new BufferedReader(new FileReader(sgmFile));
     
                 buffer.setLength(0);
                 outBuffer.setLength(0);
     
                 line = null;
                 index = -1;
                 docNumber = 0;
				 matches = 0;
                 while ((line = reader1.readLine()) != null)
                 {
                     //when we see a closing reuters tag, flush the file
     
                     if ((index = line.indexOf("</REUTERS")) == -1)
                     {
                         //Replace the SGM escape sequences
     
                         buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                     }
                     else
                     {
                         //Extract the relevant pieces and write to a file in the output dir
                         Matcher matcher1 = EXTRACTION_PATTERN_1.matcher(buffer);
                         outBuffer.append("\n</head>\n<body>\n\t<h3>");
                         while (matcher1.find())
                         {
                             for (int i = 1; i <= matcher1.groupCount(); i++)
                             {
                                 if (matcher1.group(i) != null)
                                 {
                                     outBuffer.append(matcher1.group(i));
                                 }
                             }
							 outBuffer.append("</h3>\n</body>\n</html>");
                             //outBuffer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
                         }
                         String out = outBuffer.toString();
                         for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                         {
                             out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                         }
						 out = out.toLowerCase();
                        File outFile1 = new File(outputDir, sgmFile.getName() + "-" + (docNumber++) + ".html");
                         //System.out.println("Writing " + outFile);
                         FileWriter writer1 = new FileWriter(outFile1, true);
                         writer1.write(out);
                         writer1.close();
                         outBuffer.setLength(0);
                         buffer.setLength(0);
                     }
                 }
                 reader1.close();				
            }			 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
        }
     
     
         public static void main(String[] args)
         {
             if (args.length != 2)
             {
                 printUsage();
             }
             File reutersDir = new File(args[0]);
     
             if (reutersDir.exists())
             {
                 File outputDir = new File(args[1]);
                 outputDir.mkdirs();
                 ExtractReuters extractor = new ExtractReuters(reutersDir, outputDir);
                 extractor.extract();
				 extractor.make_index();
             }
             else
             {
                 printUsage();
             }
         }
     
         private static void printUsage()
         {
            System.err.println("Usage: java -cp <...> org.apache.lucene.benchmark.utils.ExtractReuters <Path to Reuters SGM files> <Output Path>");
         }
     }