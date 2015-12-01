<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"
    import="org.terrier.querying.*"
    import="org.terrier.structures.Index"
    import="org.terrier.structures.MetaIndex"
    import="org.terrier.matching.ResultSet"
    import="org.terrier.querying.SearchRequest"
    import="org.terrier.utility.ApplicationSetup"
    import="org.terrier.services.websitesearch.WebsiteSearch"
    import="org.terrier.structures.CollectionStatistics"
     %>
<%!

/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org/
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is results.jsp
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
static int NUM_RESULTS_PER_PAGE = 10;
static int NEAREST_PAGES = 5;
static boolean SHOW_NEXT_PREV = true;
static String cropmeta = "true";

protected static void displayResults(ResultSet rs, int iStart, javax.servlet.jsp.JspWriter out) throws IOException
{
	String[] displayKeys = rs.getMetaKeys();
	String[][] meta = new String[displayKeys.length][];
	
	
	for(int j=0;j<displayKeys.length;j++)
	{
		meta[j] = rs.getMetaItems(displayKeys[j]);
	}
	double[] scores = rs.getScores();
	
	out.print("<table border=\"0\" cellspacing=\"3\">");
	out.print("<tr>");
	out.print("<th>"+"Rank"+"</th>");
	out.print("<th>"+"Document"+"</th>");
	out.print("<th>"+"Score"+"</th>");
	out.print("</tr>");
	
	int depth = (iStart+10);
	if (depth>rs.getResultSize()) depth = rs.getResultSize();

	for(int i=iStart;i<depth;i++)
	{
		final int rank = i + 1;
		String title = "";
		String url = "";
		String time = "";
		String snippet = "";
		for(int j=0;j<displayKeys.length;j++) {
			if ( displayKeys[j].compareTo("title_emph")==0 ) {
			   title = meta[j][i];
			}
			if ( displayKeys[j].compareTo("URL")==0 ) {
			   url = meta[j][i];
			}
			if ( displayKeys[j].compareTo("time")==0 ) {
			   time = meta[j][i];
			}
			if ( displayKeys[j].compareTo("content_emph")==0 ) {
			   snippet = meta[j][i];
			}
		}

		if (title.length()==0) title=url;
		if (snippet.length()==0) snippet="No snippet could be generated";

	    
		out.print("<tr>");
                out.print("<td>"+rank+"</td>");
                out.print("<td align=\"left\"><font size=\"4\" color=\"#2200cc\"><span class=\"results_title\"><a href=\""+url+"\">"+title+"</a></font></span></td>");
                out.print("<td>"+""+"</td>");
                out.print("</tr>");
                out.print("<tr>");
                out.print("<td>"+""+"</td>");
                out.print("<td align=\"left\"><span class=\"results_body_emph\"><font size=\"3\">"+snippet.replace('"', '\'')+"</font></span></td>");
                out.print("<td><span class=\"results_score\">"+(new Double(scores[i])).toString().substring(0,6)+"</span></td>");
       		out.print("</tr>");
                out.print("<tr>");
                out.print("<td>"+""+"</td>");
                out.print("<td align=\"left\"><span class=\"results_url\"><font size=\"2\" color=\"#0E774A\">"+url.replace("http://", "")+"</font>");
                out.print("<font size=\"2\"> - </font><font color=\"#3366CC\" size=\"2\">"+time+"</font></span></td>");
                out.print("<td>"+""+"</td>");
                out.print("</tr><tr><td>&nbsp;</td></tr>\n");

	}
	out.print("</table>");
}

protected static void displayPageNumbers(
		
		SearchRequest srq, ResultSet rs, 
		int iStart, javax.servlet.jsp.JspWriter out)
	throws IOException
{
	String sQuery = srq.getOriginalQuery();
	String sQuery_URLEncoded = java.net.URLEncoder.encode(sQuery, "UTF-8");
	int maxResults = Math.min(rs.getExactResultSize(), 1000);//we dont let anyone go deeper than 1000
	int numPages = (maxResults / NUM_RESULTS_PER_PAGE) +1; 
	
	if (SHOW_NEXT_PREV && iStart > 0)
	{
		int prevStart = iStart - NUM_RESULTS_PER_PAGE;
		if (prevStart < 0)
			prevStart = 0;
		out.print("<a href=\"?query="+sQuery_URLEncoded+ "&start="+prevStart+"\">Previous</a> &nbsp;");
	}
	
	for(int i=0;i<numPages;i++)
	{
		int thisStart = (i * NUM_RESULTS_PER_PAGE);
		if (Math.abs(thisStart - iStart) > NUM_RESULTS_PER_PAGE * NEAREST_PAGES)
			continue;
		if (thisStart != iStart)
			out.print("<a href=\"?query="+sQuery_URLEncoded+ "&start="+ thisStart+"\">"+(i+1)+"</a>");
		else
			out.print(i+1);
		out.print("&nbsp;");
	}
	
	if (SHOW_NEXT_PREV)
	{
		int nextStart = iStart + NUM_RESULTS_PER_PAGE;
		if (nextStart < maxResults)
			out.print("<a href=\"?query="+sQuery_URLEncoded+ "&start="+nextStart+"\">Next</a>");
	}
}

%>

<%

ApplicationSetup.setProperty("metaindex.crop",cropmeta);

String query = request.getParameter("query");
String pageToCrawl = request.getParameter("pageToCrawl");
String depthToCrawl = request.getParameter("depthToCrawl");
String indexpath = request.getParameter("indexpath");
String indexprefix = request.getParameter("indexprefix");
int idepthToCrawl;
if (depthToCrawl == null || depthToCrawl.length() == 0)
{
  depthToCrawl = "0";
  idepthToCrawl=0;
} else {
  idepthToCrawl = Integer.parseInt(depthToCrawl);
}
String sStart = request.getParameter("start");
int iStart;
if (sStart == null || sStart.length() == 0)
{
	sStart = "0";
	iStart = 0;
}
else
{
	iStart = Integer.parseInt(sStart);
	if (iStart > 1000)
	{
		iStart = 1000;
		sStart = "1000";
	}
}

WebsiteSearch index = (WebsiteSearch)application.getAttribute("terrier.jsp.index");
if (index == null)
{
	index = new WebsiteSearch();
	application.setAttribute("terrier.jsp.index", index);
}	


if (pageToCrawl != null && pageToCrawl.length() > 0) {
  // we are crawling a page
  if (!pageToCrawl.startsWith("http")) {
    pageToCrawl = "http://"+pageToCrawl;
  }
  index.crawlWebsite(pageToCrawl.trim(),idepthToCrawl);
}

if (indexpath != null && indexpath.length() > 0 ){

  if (indexprefix==null || indexprefix.length()==0) {
    indexprefix="data";
  }
  
  index.writeIndex(indexpath,indexprefix);

}






%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="java.io.IOException"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Terrier Search results for <%=query%></title>
<link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>
<div align=left>
<div class="header">
  <div class="header_interior" align=center>
<a href="/"><img border="0" src="/images/terrier-logo-web.jpg"></a>

<form action="results.jsp" id="queryform">
<input type="text" size="50" name="query" value="" />
<input type="submit"  value="Search" / >
</form>
</div>
</div>
</div>

<div id="main">

<div class="left_column"><div class="navigation">

<h2>Index Statistics</h2>

 <ul class="markermenu">
 
 <%
CollectionStatistics collStats = index.getCollectionStatistics();
%>
 
<li class="markermenu"><h3>Documents Indexed = <%=collStats.getNumberOfDocuments() %></h3></li>
<li class="markermenu"><h3># Unique Terms = <%=collStats.getNumberOfUniqueTerms() %></h3></li>
</ul></br>

<h2>Crawl New Website?</h2>
</br>
<form action="results.jsp" id="indexform">
<table align=center>
<tr>
<th>Website Top-level URL</th>
<th></th>
</tr>
<tr>
<td><input type="text" size="40" name="pageToCrawl" value="" /></td>
</tr>
</table>
</br>
<table align=center>
<tr>
<th>Crawl Depth</th>
</tr>
<tr>
<td><input type="text" size="5" name="depthToCrawl" value="1" /></td>
<td></td>
<td><input type="submit"  value="Crawl Website" / ></td>
</tr>
</table>
</form>
</br>

<h2>Save Index?</h2>
</br>
<form action="results.jsp" id="indexform">
<table align=center>
<tr>
<th>Index Location</th>
<th></th>
</tr>
<tr>
<td><input type="text" size="40" name="indexpath" value="" /></td>
</tr>
</table>
</br>
<table align=center>
<tr>
<th>Index Name</th>
</tr>
<tr>
<td><input type="text" size="20" name="indexprefix" value="" /></td>
<td></td>
<td><input type="submit"  value="Write Index to Disk" / ></td>
</tr>
</table>
</form>
</br>

<div class="navigationbtm"></div>
 <br />
 <div id="poweredby" align=center>
<a href="http://terrier.org"><img src="/images/terrier-desktop-search.gif" border="0"/> Powered by Terrier</a>
</div>
 </div>
  </div>
 <div class="right_column">

<ol id="results">
<%

if (query != null && query.length() > 0) {

SearchRequest srq = index.search(query.trim(),1000);
ResultSet rs = srq.getResultSet(); 

//int firstDisplayRank = iStart +1;
//int lastDisplayRank = 1+ Math.min(rs.getExactResultSize() -1, iStart + NUM_RESULTS_PER_PAGE);

out.print("<div id=\"summary\">");
out.print("<font size=\"4\" color=\"#6699CC\">Results for query '"+query+"'</font>");
out.print("</div>");

displayResults(rs, iStart, out);

out.print("<div id=\"pages\">");
displayPageNumbers(srq, rs, iStart, out);
out.print("</div>");

} else {

if (indexpath != null && indexpath.length() > 0) {
  
%>
<h2> Writting current index to <%=indexpath %> with name <%=indexprefix %></h2>
<%
  
} else {

%>
<h2> Attempting to crawl and index <%=pageToCrawl %> </h2>
<%
}

}
%>

</ol>

</body>
</html>