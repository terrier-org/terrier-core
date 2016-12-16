<span>\[</span>[Previous: Evaluation of Experiments](evaluation.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: Web-based Terrier](terrier_http.html)<span>\]</span>

Using the Desktop Search example application:
=============================================

Desktop Terrier is an example application we have provided with Terrier for two purposes:

-   To provide a Desktop Search application that will allow users to quickly test out features of Terrier such as for example the Terrier query language.

-   To give developers an example of using Terrier in an interactive setting.

Importantly, Desktop Terrier is only a sample application to help users become used to the functionality that Terrier provides. We do not recommend Desktop Terrier to perform large or complex indexing jobs. Instead, once you are comfortable with the Terrier functionality, indexing and batch retrieval should be performed using the command line. You have been warned.

Starting Desktop Terrier
------------------------

-   **Windows**: Double click on bin\\desktop\_terrier.bat to start Desktop Terrier - on some versions of Windows you may receive a warning about the file being suspicious, but you can safely ignore this. Note that you will need at least Java 1.7 or later to run Desktop Terrier.

-   **MacOS X**: Double click on bin/desktop\_terrier.sh to start Desktop Terrier. Should this fail:

    1.  Make sure you have Java 1.7

    2.  Select bin/desktop\_terrier.sh in Finder

    3.  In File menu, select Get Info (Command-I)

    4.  Select “Terminal” application with “Open with”. Terminal is in the Folder Applications/Utilities.

-   **Unix/Linux**: execute the bin/desktop\_terrier.sh shell script to start Desktop Terrier. You can do this from an Xterm environment (or similar), or by double clicking bin/desktop\_terrier.sh in a Konqueror or Nautilus window (KDE or Gnome).

Running Desktop Terrier
-----------------------

The application window of the Desktop Search features two main tabs: “Search” and “Index”. In the following, we will explain how you can use the application to index and search documents on your computer.

### Indexing

Here we will explain how you can specify which documents you want Desktop Terrier to index.

Indexing is the process where Terrier examines all the files in the folders you specified, reads the documents if it can, and creates an index. There are only two buttons on the “Index” tab. The “Select Folders…” button opens a dialog that will allow you to select which folders should be indexed. The application will examine these folders recursively, and will index all the supported document types. Based on the file extension, the application will try to find a corresponding parser. If no appropriate parser can be found, the file will be ignored. At the moment Terrier supports parsing of Simple text, PDF, MS Word, MS PowerPoint, MS Excel, HTML, XML, XHTML, Tex, and Bib documents. Importantly, Desktop Terrier uses SimpleFileCollection, hence each file counts as a single document. More complex formats like those used at TREC are not detected by default. We recommend using Terrier from the command line to process these types of collection.

The “Create Index” button will initiate the indexing process. At the moment the Desktop Terrier does not support the updatable indices. That means that every time you press the “Create Index” button Terrier will remove the old index and index all specified folders from scratch. Once you have selected the folders to index, you may click the “Create Index” button in order to start the indexing process. The progress of the indexing is documented in the text field at the bottom of the window, After the indexing has finished, the application will automatically switch to the “Search tab”.

You can now use the Search tab of Desktop Terrier to search for documents. Enter terms that you think your document may contain in the text box beside the Search button, and press Search. Documents Terrier thinks are relevant will be displayed in the list below. You can open a document by double clicking on that row in the table. The type of the document is shown in the second column.

### Searching

In the searching tab, you can enter a query in the text field and press the button “Search” to obtain the retrieval results. The results are shown in the table below the search field, as a ranked list of documents. The table has four columns. The first one contains the rank of a document, the second one contains the file name of a document. The third one contains the full path to the document and finally the fourth one contains the score of the document.

To formulate a query, you can incorporate the [query language of Terrier](querylanguage.html). For example:

-   The query `information retrieval` should retrieve documents where the two query terms are either in the same, or in consecutive blocks.

-   The query `information retrieval~5` should retrieve documents in which the query terms appear within 5 blocks, irrespectively of the query term order.

-   With the operators plus or minus, we may specify that a term should, or shouldn’t appear in the retrieved documents. For example, for the query `information retrieval +book`, the retrieved documents should at least contain the term book.

-   In the query `information retrieval^2.5`, the query term `retrieval` has a 2.5 times higher weight than what it would have normally.

-   The query `information retrieval c:7` will perform retrieval for the query terms `information` and `retrieval`, setting the value of the term frequency normalisation parameter `c` equal to 7.

By default, Terrier Desktop Search retrieves the documents that contain all the query terms. If there are no such documents, then it returns the documents that contain at least one of the query terms.

In order to open one of the retrieved documents, you may double-click on its filename, i.e. the corresponding cell of the second column. Opening the retrieved files is a platform-dependent function. In Windows and Mac OS X environments, the application uses the file associations used by the operating system, while in other environments, such as Linux the file associations need to be set by the user. In these cases, the associations are saved in a file with the default name desktop.fileassoc in the var directory of your installation.

If there is already an application associated with the file, then this application will start and open the file you double-clicked on. In the case when there is no application associated, a dialog will appear, in order to assist you with selecting an appropriate application.

Help
----

This documentation is also available from the Help menu of the Desktop Terrier version.

Advanced Options
----------------

Should you have trouble using Desktop Terrier, e.g. if the application is not running as expected, you can make use of the “–debug” option:

    bin/desktop_terrier.sh --debug (Linux, Mac OS X)
    bin\desktop_terrier.bat --debug (Windows)

If you use Desktop Terrier regularly, you may wish to have Terrier re-index your documents automatically at set times. You can do this by scheduling Terrier to run with the “–reindex” option:

    bin/desktop_terrier.sh --reindex (Linux, Mac OS X)
    bin\desktop_terrier.bat --reindex (Windows)

In order to schedule this command line for repetitive execution on Unix use the crontab utility. On Windows use the Scheduled Tasks functionality, which can be found in the Control Panel.

Advanced Configuration
----------------------

You can configure the Desktop using many of the properties listed elsewhere in the Terrier documentation. These can be set in the `etc/terrier.properties` file. Moreover, it is possible to configure the Desktop using the following properties:

**Properties:**

-   `desktop.directories.spec` - Where is the collection.spec for the desktop. Defaults to `var/desktop.spec`

-   `desktop.matching` - Which matching class to use for desktop. Defaults to org.terrier.matching.taat.Full.

-   `desktop.model` - Which weighting model to use for the desktop. Defaults to PL2.

-   `desktop.manager` - Which Manager class to use for the desktop. Defaults to Manager.

-   `desktop.indexing.singlepass` - Set to true to use the single-pass indexer.

-   `desktopsearch.filetype.colors` - Mapping of file type to colour. Default value `Text:(221 221 221),TeX:(221 221 221),Bib:(221 221 221),PDF:(236 67 69),HTML:(177 228 250),Word:(100 100 255),Powerpoint:(250 110 49),Excel:(38 183 78),XHTML:(177 228 250),XML:(177 228 250)`

-   `desktopsearch.filetype.types` - Comma-delimited mapping of file extensions to File types. Default value is `txt:Text,text:Text,tex:TeX,bib:Bib,pdf:PDF,html:HTML,htm:HTML,xhtml:XHTML,xml:XML,doc:Word,ppt:Powerpoint,xls:Excel`

<span>\[</span>[Previous: Evaluation of Experiments](evaluation.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: TREC Experiment Examples](trec_examples.html)<span>\]</span>

------------------------------------------------------------------------

Webpage: <http://terrier.org>
Contact: [](mailto:terrier@dcs.gla.ac.uk)
[School of Computing Science](http://www.dcs.gla.ac.uk/)
Copyright (C) 2004-2015 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
