<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"
    import="org.terrier.querying.*"
    import="org.terrier.structures.Index"
    import="org.terrier.structures.MetaIndex"
    import="org.terrier.matching.ResultSet"
    import="org.terrier.utility.ApplicationSetup"
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
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
static int NUM_RESULTS_PER_PAGE = 10;
static int NEAREST_PAGES = 5;
static boolean SHOW_NEXT_PREV = true;
String defaultMatching = ApplicationSetup.getProperty("terrier.jsp.matching", "Matching");
String defaultModel = ApplicationSetup.getProperty("terrier.jsp.model", "PL2");

protected static void displayResults(ResultSet rs, int iStart, javax.servlet.jsp.JspWriter out) throws IOException
{
	String[] displayKeys = rs.getMetaKeys();
	String[][] meta = new String[displayKeys.length][];
	
	for(int j=0;j<displayKeys.length;j++)
	{
		meta[j] = rs.getMetaItems(displayKeys[j]);
	}
	double[] scores = rs.getScores();
	for(int i=0;i<rs.getResultSize();i++)
	{
		final int rank = iStart + i + 1;
		out.print("<li value=\""+rank+"\" class=\"result\">");		
		out.print("<span class=\"results_rank\">"+ rank + "</span>");			
		for(int j=0;j<displayKeys.length;j++)
		{
			out.print("<span class=\"results_"+displayKeys[j]+"\">"+meta[j][i]+"</span>");
		}
		out.print("<span class=\"results_score\">"+ scores[i] + "</span>");	
		out.println("</li>");
	}
}

protected static void displayPageNumbers(
		
		SearchRequest srq, ResultSet rs, 
		int iStart, javax.servlet.jsp.JspWriter out)
	throws IOException
{
	String sQuery = srq.getOriginalQuery();
	String sQuery_URLEncoded = java.net.URLEncoder.encode(sQuery, "UTF-8");
	int maxResults = Math.min(rs.getExactResultSize(), 1000);//we dont let anyone go deeper than 1000
	int numPages = maxResults / NUM_RESULTS_PER_PAGE; 
	
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

String query = request.getParameter("query");
if (query == null || query.length() == 0)
	response.sendRedirect("./");
query = query.trim();
if (query == null || query.length() == 0)
	response.sendRedirect("./");
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

Index index = (Index)application.getAttribute("terrier.jsp.index");
if (index == null)
{
	index = Index.createIndex();
	application.setAttribute("terrier.jsp.index", index);
}	
Manager queryingManager = (Manager)application.getAttribute("terrier.jsp.manager");
if (queryingManager == null)
{
	queryingManager = new Manager(index);
	application.setAttribute("terrier.jsp.manager", queryingManager);
}

SearchRequest srq = queryingManager.newSearchRequest("results.jsp.query", query);
srq.setOriginalQuery(query);
srq.setControl("start", sStart);
srq.setControl("decorate", "on");
srq.setControl("end", String.valueOf(iStart + NUM_RESULTS_PER_PAGE -1));
srq.addMatchingModel(defaultMatching, defaultModel);
queryingManager.runPreProcessing(srq);
queryingManager.runMatching(srq);
queryingManager.runPostProcessing(srq);
queryingManager.runPostFilters(srq);
ResultSet rs = srq.getResultSet();
int firstDisplayRank = iStart +1;
int lastDisplayRank = 1+ Math.min(rs.getExactResultSize() -1, iStart + NUM_RESULTS_PER_PAGE);
int possibleRanks = rs.getExactResultSize();

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
<a href="/"><img border="0" src="/images/terrier-logo-web.jpg"></a>
<form action="results.jsp" id="queryform">
<input type="text" size="50" name="query" value="<%=query %>" />
<input type="submit"  value="Search" / >
</form>
<div id="summary">
Results for <%=query%>, displaying <%=firstDisplayRank%>-<%=lastDisplayRank%> of <%=possibleRanks %>
</div>
<ol id="results">
<%
displayResults(rs, iStart, out);
%>
</ol>
<div id="pages">
<%
displayPageNumbers(srq, rs, iStart, out);
%>
</div>	
<hr width="50%">
<div id="poweredby">
<a href="http://terrier.org"><img src="/images/terrier-desktop-search.gif" border="0"/> Powered by Terrier</a>
</div>
</body>
</html>
