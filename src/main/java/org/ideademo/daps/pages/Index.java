package org.ideademo.daps.pages;

import java.io.StringReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.ideademo.daps.entities.Dap;
import org.apache.log4j.Logger;

import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.ideademo.daps.services.util.PDFStreamResponse;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Index 
{
	 
  private static Logger logger = Logger.getLogger(Index.class);
  private static final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_31); 

  
  /////////////////////////////
  //  Drives QBE Search
  @Persist
  private Dap example;
  
  
  //////////////////////////////////////////////////////////////
  // Used in rendering within Loop just as in Grid (Table) Row
  @SuppressWarnings("unused")
  @Property 
  private Dap row;

    
  @Property
  @Persist
  private String searchText;

  @Inject
  private Session session;
  
  @Inject
  private HibernateSessionManager sessionManager;
  
  @Property 
  @Persist
  int retrieved; 
  @Property 
  @Persist
  int total;
  
  
  @Inject
  @Path("context:layout/images/image067.gif")
  private Asset logoAsset;
  
  @Inject
  private AssetSource assetSource;
  
  @Inject
  Messages messages;

  
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Select Boxes - Enumaration values - the user-visible labels are externalized in Index.properties 
  
  
  // the ECV select box
  @Property
  @Persist
  private Ecv ecv;
  
  public enum Ecv
  {
	  // Atmospheric, Oceanic, Terrestrial
	  ATM, OCE, TER
  }

  // the regions select box
  @Property
  @Persist
  @PageActivationContext 
  private Regions regions;
  
  public enum Regions
  {
	 // BAS = Pacific Basin, GLB = global - see the properties file 
	 CNP, WNP, SP, BAS, GLB
  }
  
  
  // the TIMESCALE select box
  @Property
  @Persist
  private Daptype daptype;
  
  public enum Daptype
  {
	 PHYSICAL, HINDCAST, OUTLOOK, PROJECTION, GUIDANCE, APPS, NONPHY
  }

  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Entity List generator - QBE, Text Search or Show All 
  //

  @SuppressWarnings("unchecked")
  public List<Dap> getList()
  {
	//////////////////////////////////
	// first interpret search criteria
	  
	// text search string 
	logger.info("Search Text = " + searchText);
	
	// Construct example for QBE Search by recording what selections have been in the choice boxes on this page  
	if (ecv != null)  onValueChangedFromEcv(ecv.toString());
	if (daptype != null) onValueChangedFromDaptype(daptype.toString());
	if (regions != null) onValueChangedFromRegions(regions.toString());
	// at this point all the booleans in example have been set.
	// NOTE/MAY BE TODO: Lucene dependency may be removed by setting the text search criteria into various text fields of the example. 
    // Get all records anyway - for showing total at bottom of presentation layer
    List <Dap> alst = session.createCriteria(Dap.class).list();
    total = alst.size();

	
    // then makes lists and sublists as per the search criteria 
    List<Dap> xlst=null; // xlst = Query by Example search List
    if(example != null)
    {
       Example ex = Example.create(example).excludeFalse().ignoreCase().enableLike(MatchMode.ANYWHERE);
       
       xlst = session.createCriteria(Dap.class).add(ex).list();
       
       
       if (xlst != null)
       {
    	   logger.info("Dap Example Search Result List Size  = " + xlst.size() );
    	   Collections.sort(xlst);
       }
       else
       {
         logger.info("Dap Example Search result did not find any results...");
       }
    }
    
    List<Dap> tlst=null;
    if (searchText != null && searchText.trim().length() > 0)
    {
      FullTextSession fullTextSession = Search.getFullTextSession(sessionManager.getSession());  
      try
      {
        fullTextSession.createIndexer().startAndWait();
       }
       catch (java.lang.InterruptedException e)
       {
         logger.warn("Lucene Indexing was interrupted by something " + e);
       }
      
       QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity( Dap.class ).get();
       
       // fields being covered by text search 
       TermMatchingContext onFields = qb
		        .keyword()
		        .onFields("code","name","description", "keywords","contact", "organization", "url", "worksheet");
       
       BooleanJunction<BooleanJunction> bool = qb.bool();
       /////// Tokenize the search string for default AND logic ///
       TokenStream stream = analyzer.tokenStream(null, new StringReader(searchText));
       CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
       try
       {
        while (stream.incrementToken()) 
         {
    	   String token = cattr.toString();
    	   logger.info("Adding search token " +  token + " to look in Daps database");
    	   bool.must(onFields.matching(token).createQuery());
         }
        stream.end(); 
        stream.close(); 
       }
       catch (IOException ioe)
       {
    	   logger.warn("Daps Text Search: Encountered problem tokenizing search term " + searchText);
    	   logger.warn(ioe);
       }
       
       /////////////  the lucene query built from non-simplistic English words 
       org.apache.lucene.search.Query luceneQuery = bool.createQuery();
       
       tlst = fullTextSession.createFullTextQuery(luceneQuery, Dap.class).list();
       if (tlst != null) 
       {
    	   logger.info("TEXT Search for " + searchText + " found " + tlst.size() + " Daps records in database");
    	   Collections.sort(tlst);
       }
       else
       {
          logger.info("TEXT Search for " + searchText + " found nothing in Daps");
       }
    }
    
    
    // organize what type of list is returned...either total, partial (subset) or intersection of various search results  
    if (example == null && (searchText == null || searchText.trim().length() == 0))
    {
    	// Everything...
    	if (alst != null && alst.size() > 0)
    	{
    		logger.info ("Returing all " + alst.size() + " Daps records");
        	Collections.sort(alst);
    	}
    	else
    	{
    		logger.warn("No Dap records found in the database");
    	}
    	retrieved = total;
        return alst; 
    }
    else if (xlst == null && tlst != null)
    {
    	// just text search results
    	logger.info("Returing " + tlst.size() + " Daps records as a result of PURE text search (no QBE) for " + searchText);
    	retrieved = tlst.size();
    	return tlst;
    }
    else if (xlst != null && tlst == null)
    {
    	// just example query results
    	logger.info("Returning " + xlst.size() + " Daps records as a result of PURE Query-By-Example (QBE), no text string");
    	retrieved = xlst.size();
    	return xlst;
    }
    else 
    {

        ////////////////////////////////////////////
    	// get the INTERSECTION of the two lists
    	
    	// TRIVIAL: if one of them is empty, return the other
    	// if one of them is empty, return the other
    	if (xlst.size() == 0 && tlst.size() > 0)
    	{
        	logger.info("Returing " + tlst.size() + " Daps records as a result of ONLY text search, QBE pulled up ZERO records for " + searchText);
        	retrieved = tlst.size();
    		return tlst;
    	}

    	if (tlst.size() == 0 && xlst.size() > 0)
    	{
        	logger.info("Returning " + xlst.size() + " Daps records as a result of ONLY Query-By-Example (QBE), text search pulled up NOTHING for string " + searchText);
        	retrieved = xlst.size();
	        return xlst;
    	}
    	
    	
    	List <Dap> ivec = new Vector<Dap>();
    	// if both are empty, return this Empty vector. 
    	if (xlst.size() == 0 && tlst.size() == 0)
    	{
        	logger.info("Neither QBE nor text search for string " + searchText +  " pulled up ANY Daps Records.");
        	retrieved = 0;
    		return ivec;
    	}
    	


    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// now deal with BOTH text and QBE being non-empty lists - implementing intersection by Database Primary Key -  Id
    	Iterator<Dap> xiterator = xlst.iterator();
    	while (xiterator.hasNext()) 
    	{
    		Dap x = xiterator.next();
    		Long xid = x.getId();
    		
        	Iterator<Dap> titerator = tlst.iterator();
    		while(titerator.hasNext())
    		{
        		Dap t = titerator.next();
        		Long tid = t.getId();
    			
        		if (tid == xid)
        		{
        			ivec.add(t); break;
        		}
        		
    		}
    			
    	}
    	// sort again - 
    	if (ivec.size() > 0)  Collections.sort(ivec);
    	logger.info("Returning " + ivec.size() + " Daps records from COMBINED (text, QBE) Search");
    	retrieved = ivec.size();
    	return ivec;
    }
    
  }
  

  public StreamResponse onReturnStreamResponse(long id) 
  {

      Dap dap =  (Dap) session.load(Dap.class, id);


      // step 1: creation of a document-object
      Document document = new Document();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
              // step 2:
              // we create a writer that listens to the document
              // and directs a PDF-stream to a file
              PdfWriter writer = PdfWriter.getInstance(document, baos);
              // step 3: we open the document
              document.open();
              
              java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(logoAsset.getResource().toURL());
              if (awtImage != null)
              {
            	  com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(awtImage, null); 
            	  logo.scalePercent(50);
            	  if (logo != null) document.add(logo);
              }

              DateFormat formatter = new SimpleDateFormat
                      ("EEE MMM dd HH:mm:ss zzz yyyy");
                  Date date = new Date(System.currentTimeMillis());
                  TimeZone eastern = TimeZone.getTimeZone("Pacific/Honolulu");
                  formatter.setTimeZone(eastern);

              document.add(new Paragraph("Data and Products " + formatter.format(date)));
              
              
              document.add(Chunk.NEWLINE);document.add(Chunk.NEWLINE);
              
              // create table, 2 columns
              String acronym = StringUtils.trimToEmpty(dap.getCode());
              String name = StringUtils.trimToEmpty(dap.getName());
              String description = StringUtils.trimToEmpty(dap.getDescription());
              String leadAgencies = StringUtils.trimToEmpty(dap.getOrganization());
              String contacts = StringUtils.trimToEmpty(dap.getContact());
              String url = StringUtils.trimToEmpty(dap.getUrl());
          	  
          	  
                PdfPTable table = new PdfPTable(2);
                table.setWidths(new int[]{1, 4});
                table.setSplitRows(false);
                
                PdfPCell nameTitle = new PdfPCell(new Phrase("Name")); 
                
                if (StringUtils.isNotBlank(acronym)) name = name + " (" + acronym + ")";
                PdfPCell nameCell = new PdfPCell(new Phrase(name));
                
                nameTitle.setBackgroundColor(BaseColor.CYAN);  nameCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                
                table.addCell(nameTitle);  table.addCell(nameCell);          		          		
          		
                // capabilities
                com.itextpdf.text.List capabilities = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isVariability()) 
          		{
          			ListItem item = new ListItem(getLabel("variability")); capabilities.add(item);
          		}
          		if (dap.isImpacts()) 
          		{
          			ListItem item = new ListItem(getLabel("impacts")); capabilities.add(item);
          		}
                
          		
          		if(capabilities.size() > 0)
          		{
          		  PdfPCell varsCell = new PdfPCell(); varsCell.addElement(capabilities);
          		  table.addCell(new PdfPCell(new Phrase("Capability Area")));  table.addCell(varsCell);
          		}

          		
          		// focus area
          		com.itextpdf.text.List fa = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if(dap.isWater())
          		{
          			ListItem item = new ListItem(getLabel("water")); fa.add(item);
          		}
          		if(dap.isCoastal())
          		{
          			ListItem item = new ListItem(getLabel("coastal")); fa.add(item);
          		}
          		if(dap.isEcosystem())
          		{
          			ListItem item = new ListItem(getLabel("ecosystem")); fa.add(item);
          		}
          		
          		if (fa.size() > 0)
          		{
           		  PdfPCell faCell = new PdfPCell(); faCell.addElement(fa);
           		  table.addCell(new PdfPCell(new Phrase("Focus Area")));  table.addCell(faCell);
          		}


          		

                
          		//region
          		com.itextpdf.text.List regions = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isCentralNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("centralNorthPacific")); regions.add(item);
          		}
          		if (dap.isStateOfHawaii())
          		{
          			ListItem item = new ListItem(getLabel("stateOfHawaii")); regions.add(item);
          		}
          		if (dap.isNorthWestHawaiianIslands())
          		{
          			ListItem item = new ListItem(getLabel("northWesternHawaiianIslands")); regions.add(item);
          		}
          		if (dap.isPacificRemoteIslands())
          		{
          			ListItem item = new ListItem(getLabel("pacificRemoteIslands")); regions.add(item);
          		}
          		if (dap.isWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("westernNorthPacific")); regions.add(item);
          		}
          		if (dap.isCnmi())
          		{
          			ListItem item = new ListItem(getLabel("cnmi")); regions.add(item);
          		}
          		if (dap.isFsm())
          		{
          			ListItem item = new ListItem(getLabel("fsm")); regions.add(item);
          		}
          		if (dap.isGuam())
          		{
          			ListItem item = new ListItem(getLabel("guam")); regions.add(item);
          		}
          		if (dap.isPalau())
          		{
          			ListItem item = new ListItem(getLabel("palau")); regions.add(item);
          		}
          		if (dap.isRmi())
          		{
          			ListItem item = new ListItem(getLabel("rmi")); regions.add(item);
          		}
          		if (dap.isOtherWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherWesternNorthPacific")); regions.add(item);
          		}
          		if (dap.isSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("southPacific")); regions.add(item);
          		}
          		if (dap.isAmericanSamoa())
          		{
          			ListItem item = new ListItem(getLabel("americanSamoa")); regions.add(item);
          		}
          		if (dap.isAustralia())
          		{
          			ListItem item = new ListItem(getLabel("australia")); regions.add(item);
          		}
          		if (dap.isCookIslands())
          		{
          			ListItem item = new ListItem(getLabel("cookIslands")); regions.add(item);
          		}
          		if (dap.isFiji())
          		{
          			ListItem item = new ListItem(getLabel("fiji")); regions.add(item);
          		}
          		if (dap.isFrenchPolynesia())
          		{
          			ListItem item = new ListItem(getLabel("frenchPolynesia")); regions.add(item);
          		}
          		if (dap.isKiribati())
          		{
          			ListItem item = new ListItem(getLabel("kiribati")); regions.add(item);
          		}
          		if (dap.isNewZealand())
          		{
          			ListItem item = new ListItem(getLabel("newZealand")); regions.add(item);
          		}
          		if (dap.isPng())
          		{
          			ListItem item = new ListItem(getLabel("png")); regions.add(item);
          		}
          		if (dap.isSamoa())
          		{
          			ListItem item = new ListItem(getLabel("samoa")); regions.add(item);
          		}
          		if (dap.isSolomonIslands())
          		{
          			ListItem item = new ListItem(getLabel("solomonIslands")); regions.add(item);
          		}
          		if (dap.isTonga())
          		{
          			ListItem item = new ListItem(getLabel("tonga")); regions.add(item);
          		}
          		if (dap.isTuvalu())
          		{
          			ListItem item = new ListItem(getLabel("tuvalu")); regions.add(item);
          		}
          		if (dap.isVanuatu())
          		{
          			ListItem item = new ListItem(getLabel("vanuatu")); regions.add(item);
          		}
          		if (dap.isOtherSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherSouthPacific")); regions.add(item);
          		}
          		if (dap.isPacificBasin())
          		{
          			ListItem item = new ListItem(getLabel("pacificBasin")); regions.add(item);
          		}
          		if (dap.isGlobal())
          		{
          			ListItem item = new ListItem(getLabel("global")); regions.add(item);
          		}
          		
        		
          		if (regions.size() > 0)
          		{
           		  PdfPCell rCell = new PdfPCell(); rCell.addElement(regions);
           		  table.addCell(new PdfPCell(new Phrase("Regions")));  table.addCell(rCell);
          		}
          		
          		// data physical
          		com.itextpdf.text.List dataphysicals = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isDataPhysical())
          		{
          			ListItem item = new ListItem(getLabel("dataPhysical")); dataphysicals.add(item);
          		}
          		if (dap.isInsitu())
          		{
          			ListItem item = new ListItem(getLabel("insitu")); dataphysicals.add(item);
          		}
          		if (dap.isRemote())
          		{
          			ListItem item = new ListItem(getLabel("remote")); dataphysicals.add(item);
          		}
          		if (dap.isModel())
          		{
          			ListItem item = new ListItem(getLabel("model")); dataphysicals.add(item);
          		}
          		if (dap.isReanalysis())
          		{
          			ListItem item = new ListItem(getLabel("reanalysis")); dataphysicals.add(item);
          		}
          		if (dap.isTopo())
          		{
          			ListItem item = new ListItem(getLabel("topo")); dataphysicals.add(item);
          		}
          		if (dap.isImagery())
          		{
          			ListItem item = new ListItem(getLabel("imagery")); dataphysicals.add(item);
          		}
          		if (dap.isAtmosphericData())
          		{
          			ListItem item = new ListItem(getLabel("atmosphericData")); dataphysicals.add(item);
          		}
          		if (dap.isOceanicData())
          		{
          			ListItem item = new ListItem(getLabel("oceanicData")); dataphysicals.add(item);
          		}
          		if (dap.isTerrestrialData())
          		{
          			ListItem item = new ListItem(getLabel("terrestrialData")); dataphysicals.add(item);
          		}

          		if (dataphysicals.size() > 0)
          		{
           		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(dataphysicals);
           		  table.addCell(new PdfPCell(new Phrase("Data/Physical")));  table.addCell(pdfCell);
          		}
          		
          		
                // products - physical
          		com.itextpdf.text.List productsPhysicals = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isProductsPhysical()) 
          		{
          			ListItem item = new ListItem(getLabel("productsPhysical")); productsPhysicals.add(item);
          		}
          		if (dap.isHindcasts()) 
          		{
          			ListItem item = new ListItem(getLabel("hindcasts")); productsPhysicals.add(item);
          		}
          		if (dap.isOutlooks()) 
          		{
          			ListItem item = new ListItem(getLabel("outlooks")); productsPhysicals.add(item);
          		}
          		if (dap.isImpactOutlooks()) 
          		{
          			ListItem item = new ListItem(getLabel("impactOutlooks")); productsPhysicals.add(item);
          		}
          		if (dap.isDrought()) 
          		{
          			ListItem item = new ListItem(getLabel("drought")); productsPhysicals.add(item);
          		}
          		if (dap.isFlooding()) 
          		{
          			ListItem item = new ListItem(getLabel("flooding")); productsPhysicals.add(item);
          		}
          		if (dap.isErosion()) 
          		{
          			ListItem item = new ListItem(getLabel("eroison")); productsPhysicals.add(item);
          		}
          		if (dap.isBleaching()) 
          		{
          			ListItem item = new ListItem(getLabel("bleaching")); productsPhysicals.add(item);
          		}
          		if (dap.isSpatial()) 
          		{
          			ListItem item = new ListItem(getLabel("spatial")); productsPhysicals.add(item);
          		}
          		if (dap.isRegionNation()) 
          		{
          			ListItem item = new ListItem(getLabel("regionNation")); productsPhysicals.add(item);
          		}
          		if (dap.isLocationSite()) 
          		{
          			ListItem item = new ListItem(getLabel("locationSite")); productsPhysicals.add(item);
          		}
          		if (dap.isTimeScale()) 
          		{
          			ListItem item = new ListItem(getLabel("timeScale")); productsPhysicals.add(item);
          		}
          		if (dap.isPast()) 
          		{
          			ListItem item = new ListItem(getLabel("past")); productsPhysicals.add(item);
          		}
          		if (dap.isCurrent()) 
          		{
          			ListItem item = new ListItem(getLabel("current")); productsPhysicals.add(item);
          		}
          		if (dap.isFuture()) 
          		{
          			ListItem item = new ListItem(getLabel("future")); productsPhysicals.add(item);
          		}
          		if (dap.isMethodology()) 
          		{
          			ListItem item = new ListItem(getLabel("methodology")); productsPhysicals.add(item);
          		}
          		if (dap.isObsInsitu()) 
          		{
          			ListItem item = new ListItem(getLabel("obsInsitu")); productsPhysicals.add(item);
          		}
          		if (dap.isObsRemote()) 
          		{
          			ListItem item = new ListItem(getLabel("obsRemote")); productsPhysicals.add(item);
          		}
          		if (dap.isModelStatic()) 
          		{
          			ListItem item = new ListItem(getLabel("modelStatic")); productsPhysicals.add(item);
          		}
          		if (dap.isModelDynamic()) 
          		{
          			ListItem item = new ListItem(getLabel("modelDynamic")); productsPhysicals.add(item);
          		}
          		if (dap.isProjections()) 
          		{
          			ListItem item = new ListItem(getLabel("projections")); productsPhysicals.add(item);
          		}
          		if (dap.isGuidance()) 
          		{
          			ListItem item = new ListItem(getLabel("guidance")); productsPhysicals.add(item);
          		}
          		if (dap.isApplications()) 
          		{
          			ListItem item = new ListItem(getLabel("applications")); productsPhysicals.add(item);
          		}
          		if (dap.isAtmosphericProducts()) 
          		{
          			ListItem item = new ListItem(getLabel("atmosphericProducts")); productsPhysicals.add(item);
          		}
          		if (dap.isOceanicProducts()) 
          		{
          			ListItem item = new ListItem(getLabel("oceanicProducts")); productsPhysicals.add(item);
          		}
          		if (dap.isTerrestrialProducts()) 
          		{
          			ListItem item = new ListItem(getLabel("terrestrialProducts")); productsPhysicals.add(item);
          		}

          		if (productsPhysicals.size() > 0)
          		{
           		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(productsPhysicals);
           		  table.addCell(new PdfPCell(new Phrase("Products/Physical")));  table.addCell(pdfCell);
          		}
          		
          	    // non-physical
          		com.itextpdf.text.List nonPhysicals = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isNonPhysical()) 
          		{
          			ListItem item = new ListItem(getLabel("nonPhysical")); nonPhysicals.add(item);
          		}
          		if (dap.isBiological()) 
          		{
          			ListItem item = new ListItem(getLabel("biological")); nonPhysicals.add(item);
          		}
          		if (dap.isSocioEconomic()) 
          		{
          			ListItem item = new ListItem(getLabel("socioEconomic")); nonPhysicals.add(item);
          		}
          		if (dap.isCultural()) 
          		{
          			ListItem item = new ListItem(getLabel("cultural")); nonPhysicals.add(item);
          		}
          		if (dap.isOtherNonPhysical()) 
          		{
          			ListItem item = new ListItem(getLabel("otherNonPhysical")); nonPhysicals.add(item);
          		}

          		if (nonPhysicals.size() > 0)
          		{
           		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(nonPhysicals);
           		  table.addCell(new PdfPCell(new Phrase("Non Physical")));  table.addCell(pdfCell);
          		}

          	    // sectors
          		com.itextpdf.text.List sectors = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isHealth()) 
          		{
          			ListItem item = new ListItem(getLabel("health")); sectors.add(item);
          		}
          		if (dap.isFreshWater()) 
          		{
          			ListItem item = new ListItem(getLabel("freshWater")); sectors.add(item);
          		}
          		if (dap.isEnergy()) 
          		{
          			ListItem item = new ListItem(getLabel("energy")); sectors.add(item);
          		}
          		if (dap.isTransportation()) 
          		{
          			ListItem item = new ListItem(getLabel("transportation")); sectors.add(item);
          		}
          		if (dap.isPlanning()) 
          		{
          			ListItem item = new ListItem(getLabel("planning")); sectors.add(item);
          		}
          		if (dap.isSocioCultural()) 
          		{
          			ListItem item = new ListItem(getLabel("socioCultural")); sectors.add(item);
          		}
          		if (dap.isAgriculture()) 
          		{
          			ListItem item = new ListItem(getLabel("agriculture")); sectors.add(item);
          		}
          		if (dap.isRecreation()) 
          		{
          			ListItem item = new ListItem(getLabel("recreation")); sectors.add(item);
          		}
          		if (dap.isEcological()) 
          		{
          			ListItem item = new ListItem(getLabel("ecological")); sectors.add(item);
          		}
          		if (dap.isOtherSector()) 
          		{
          			ListItem item = new ListItem(getLabel("otherSector")); sectors.add(item);
          		}


          		if(sectors.size() > 0)
          		{
          		  PdfPCell typesCell = new PdfPCell(); typesCell.addElement(sectors);
          		  table.addCell(new PdfPCell(new Phrase("Sectors")));  table.addCell(typesCell);
          		}
          		
	
          		
          		
          		
          		
          		// text fields
          		
          		if (StringUtils.isNotBlank(description))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Description")));  table.addCell(new PdfPCell(new Phrase(description)));
          		}
          		
          		if (StringUtils.isNotBlank(url))
          		{
                  Anchor link = new Anchor(StringUtils.trimToEmpty(url)); link.setReference(StringUtils.trimToEmpty(url));
          		  table.addCell(new PdfPCell(new Phrase("Url")));  table.addCell(new PdfPCell(link));
          		}

                if (StringUtils.isNotBlank(leadAgencies))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Lead Agencies")));  table.addCell(new PdfPCell(new Phrase(leadAgencies)));
          		}

          		if (StringUtils.isNotBlank(contacts))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Contacts")));  table.addCell(new PdfPCell(new Phrase(contacts)));
          		}
         		

          		
          		document.add(table);
          		document.add(Chunk.NEWLINE);
      		
              
              
      } catch (DocumentException de) {
              logger.fatal(de.getMessage());
      }
      catch (IOException ie)
      {
    	 logger.warn("Could not find NOAA logo (likely)");
    	 logger.warn(ie);
      }

      // step 5: we close the document
      document.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return new PDFStreamResponse(bais,"PaCISDataProduct" + System.currentTimeMillis());
  }	  
  public StreamResponse onSelectedFromPdf() 
  {
      // Create PDF
      InputStream is = getPdfTable(getList());
      // Return response
      return new PDFStreamResponse(is,"PaCISDataProducts" + System.currentTimeMillis());
  }

  private InputStream getPdfTable(List list) 
  {

      // step 1: creation of a document-object
      Document document = new Document();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
              // step 2:
              // we create a writer that listens to the document
              // and directs a PDF-stream to a file
              PdfWriter writer = PdfWriter.getInstance(document, baos);
              // step 3: we open the document
              document.open();
              
              java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(logoAsset.getResource().toURL());
              if (awtImage != null)
              {
            	  com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(awtImage, null); 
            	  logo.scalePercent(50);
            	  if (logo != null) document.add(logo);
              }

              DateFormat formatter = new SimpleDateFormat
                      ("EEE MMM dd HH:mm:ss zzz yyyy");
                  Date date = new Date(System.currentTimeMillis());
                  TimeZone eastern = TimeZone.getTimeZone("Pacific/Honolulu");
                  formatter.setTimeZone(eastern);

              document.add(new Paragraph("Data and Products " + formatter.format(date)));
              
              String subheader = "Printing " + retrieved + " of total " + total + " records.";
              if (StringUtils.isNotBlank(searchText))
              {
            	  subheader += "  Searching for \"" + searchText + "\""; 
              }
              
              document.add(new Paragraph(subheader));
              document.add(Chunk.NEWLINE);document.add(Chunk.NEWLINE);
              
              // create table, 2 columns
           	Iterator<Dap> iterator = list.iterator();
           	int count=0;
       		while(iterator.hasNext())
      		{
       			count++;
          		Dap dap = iterator.next();
          		              // create table, 2 columns
              String acronym = StringUtils.trimToEmpty(dap.getCode());
              String name = StringUtils.trimToEmpty(dap.getName());
              String description = StringUtils.trimToEmpty(dap.getDescription());
              String leadAgencies = StringUtils.trimToEmpty(dap.getOrganization());
              String contacts = StringUtils.trimToEmpty(dap.getContact());
              String url = StringUtils.trimToEmpty(dap.getUrl());
          	  
          	  
                PdfPTable table = new PdfPTable(2);
                table.setWidths(new int[]{1, 4});
                table.setSplitRows(false);
                
                PdfPCell nameTitle = new PdfPCell(new Phrase("Name")); 
                
                if (StringUtils.isNotBlank(acronym)) name = name + " (" + acronym + ")";
                PdfPCell nameCell = new PdfPCell(new Phrase(name));
                
                nameTitle.setBackgroundColor(BaseColor.CYAN);  nameCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                
                table.addCell(nameTitle);  table.addCell(nameCell);          		          		
          		
                // capabilities
                com.itextpdf.text.List capabilities = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isVariability()) 
          		{
          			ListItem item = new ListItem(getLabel("variability")); capabilities.add(item);
          		}
          		if (dap.isImpacts()) 
          		{
          			ListItem item = new ListItem(getLabel("impacts")); capabilities.add(item);
          		}
                
          		
          		if(capabilities.size() > 0)
          		{
          		  PdfPCell varsCell = new PdfPCell(); varsCell.addElement(capabilities);
          		  table.addCell(new PdfPCell(new Phrase("Capability Area")));  table.addCell(varsCell);
          		}

          		
          		// focus area
          		com.itextpdf.text.List fa = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if(dap.isWater())
          		{
          			ListItem item = new ListItem(getLabel("water")); fa.add(item);
          		}
          		if(dap.isCoastal())
          		{
          			ListItem item = new ListItem(getLabel("coastal")); fa.add(item);
          		}
          		if(dap.isEcosystem())
          		{
          			ListItem item = new ListItem(getLabel("ecosystem")); fa.add(item);
          		}
          		
          		if (fa.size() > 0)
          		{
           		  PdfPCell faCell = new PdfPCell(); faCell.addElement(fa);
           		  table.addCell(new PdfPCell(new Phrase("Focus Area")));  table.addCell(faCell);
          		}


          		

                
          		//region
          		com.itextpdf.text.List regions = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isCentralNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("centralNorthPacific")); regions.add(item);
          		}
          		if (dap.isStateOfHawaii())
          		{
          			ListItem item = new ListItem(getLabel("stateOfHawaii")); regions.add(item);
          		}
          		if (dap.isNorthWestHawaiianIslands())
          		{
          			ListItem item = new ListItem(getLabel("northWesternHawaiianIslands")); regions.add(item);
          		}
          		if (dap.isPacificRemoteIslands())
          		{
          			ListItem item = new ListItem(getLabel("pacificRemoteIslands")); regions.add(item);
          		}
          		if (dap.isWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("westernNorthPacific")); regions.add(item);
          		}
          		if (dap.isCnmi())
          		{
          			ListItem item = new ListItem(getLabel("cnmi")); regions.add(item);
          		}
          		if (dap.isFsm())
          		{
          			ListItem item = new ListItem(getLabel("fsm")); regions.add(item);
          		}
          		if (dap.isGuam())
          		{
          			ListItem item = new ListItem(getLabel("guam")); regions.add(item);
          		}
          		if (dap.isPalau())
          		{
          			ListItem item = new ListItem(getLabel("palau")); regions.add(item);
          		}
          		if (dap.isRmi())
          		{
          			ListItem item = new ListItem(getLabel("rmi")); regions.add(item);
          		}
          		if (dap.isOtherWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherWesternNorthPacific")); regions.add(item);
          		}
          		if (dap.isSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("southPacific")); regions.add(item);
          		}
          		if (dap.isAmericanSamoa())
          		{
          			ListItem item = new ListItem(getLabel("americanSamoa")); regions.add(item);
          		}
          		if (dap.isAustralia())
          		{
          			ListItem item = new ListItem(getLabel("australia")); regions.add(item);
          		}
          		if (dap.isCookIslands())
          		{
          			ListItem item = new ListItem(getLabel("cookIslands")); regions.add(item);
          		}
          		if (dap.isFiji())
          		{
          			ListItem item = new ListItem(getLabel("fiji")); regions.add(item);
          		}
          		if (dap.isFrenchPolynesia())
          		{
          			ListItem item = new ListItem(getLabel("frenchPolynesia")); regions.add(item);
          		}
          		if (dap.isKiribati())
          		{
          			ListItem item = new ListItem(getLabel("kiribati")); regions.add(item);
          		}
          		if (dap.isNewZealand())
          		{
          			ListItem item = new ListItem(getLabel("newZealand")); regions.add(item);
          		}
          		if (dap.isPng())
          		{
          			ListItem item = new ListItem(getLabel("png")); regions.add(item);
          		}
          		if (dap.isSamoa())
          		{
          			ListItem item = new ListItem(getLabel("samoa")); regions.add(item);
          		}
          		if (dap.isSolomonIslands())
          		{
          			ListItem item = new ListItem(getLabel("solomonIslands")); regions.add(item);
          		}
          		if (dap.isTonga())
          		{
          			ListItem item = new ListItem(getLabel("tonga")); regions.add(item);
          		}
          		if (dap.isTuvalu())
          		{
          			ListItem item = new ListItem(getLabel("tuvalu")); regions.add(item);
          		}
          		if (dap.isVanuatu())
          		{
          			ListItem item = new ListItem(getLabel("vanuatu")); regions.add(item);
          		}
          		if (dap.isOtherSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherSouthPacific")); regions.add(item);
          		}
          		if (dap.isPacificBasin())
          		{
          			ListItem item = new ListItem(getLabel("pacificBasin")); regions.add(item);
          		}
          		if (dap.isGlobal())
          		{
          			ListItem item = new ListItem(getLabel("global")); regions.add(item);
          		}
          		
        		
          		if (regions.size() > 0)
          		{
           		  PdfPCell rCell = new PdfPCell(); rCell.addElement(regions);
           		  table.addCell(new PdfPCell(new Phrase("Regions")));  table.addCell(rCell);
          		}
          		
          		// data physical
          		com.itextpdf.text.List dataphysicals = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isDataPhysical())
          		{
          			ListItem item = new ListItem(getLabel("dataPhysical")); dataphysicals.add(item);
          		}
          		if (dap.isInsitu())
          		{
          			ListItem item = new ListItem(getLabel("insitu")); dataphysicals.add(item);
          		}
          		if (dap.isRemote())
          		{
          			ListItem item = new ListItem(getLabel("remote")); dataphysicals.add(item);
          		}
          		if (dap.isModel())
          		{
          			ListItem item = new ListItem(getLabel("model")); dataphysicals.add(item);
          		}
          		if (dap.isReanalysis())
          		{
          			ListItem item = new ListItem(getLabel("reanalysis")); dataphysicals.add(item);
          		}
          		if (dap.isTopo())
          		{
          			ListItem item = new ListItem(getLabel("topo")); dataphysicals.add(item);
          		}
          		if (dap.isImagery())
          		{
          			ListItem item = new ListItem(getLabel("imagery")); dataphysicals.add(item);
          		}
          		if (dap.isAtmosphericData())
          		{
          			ListItem item = new ListItem(getLabel("atmosphericData")); dataphysicals.add(item);
          		}
          		if (dap.isOceanicData())
          		{
          			ListItem item = new ListItem(getLabel("oceanicData")); dataphysicals.add(item);
          		}
          		if (dap.isTerrestrialData())
          		{
          			ListItem item = new ListItem(getLabel("terrestrialData")); dataphysicals.add(item);
          		}

          		if (dataphysicals.size() > 0)
          		{
           		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(dataphysicals);
           		  table.addCell(new PdfPCell(new Phrase("Data/Physical")));  table.addCell(pdfCell);
          		}
          		
          		
                // products - physical
          		com.itextpdf.text.List productsPhysicals = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isProductsPhysical()) 
          		{
          			ListItem item = new ListItem(getLabel("productsPhysical")); productsPhysicals.add(item);
          		}
          		if (dap.isHindcasts()) 
          		{
          			ListItem item = new ListItem(getLabel("hindcasts")); productsPhysicals.add(item);
          		}
          		if (dap.isOutlooks()) 
          		{
          			ListItem item = new ListItem(getLabel("outlooks")); productsPhysicals.add(item);
          		}
          		if (dap.isImpactOutlooks()) 
          		{
          			ListItem item = new ListItem(getLabel("impactOutlooks")); productsPhysicals.add(item);
          		}
          		if (dap.isDrought()) 
          		{
          			ListItem item = new ListItem(getLabel("drought")); productsPhysicals.add(item);
          		}
          		if (dap.isFlooding()) 
          		{
          			ListItem item = new ListItem(getLabel("flooding")); productsPhysicals.add(item);
          		}
          		if (dap.isErosion()) 
          		{
          			ListItem item = new ListItem(getLabel("eroison")); productsPhysicals.add(item);
          		}
          		if (dap.isBleaching()) 
          		{
          			ListItem item = new ListItem(getLabel("bleaching")); productsPhysicals.add(item);
          		}
          		if (dap.isSpatial()) 
          		{
          			ListItem item = new ListItem(getLabel("spatial")); productsPhysicals.add(item);
          		}
          		if (dap.isRegionNation()) 
          		{
          			ListItem item = new ListItem(getLabel("regionNation")); productsPhysicals.add(item);
          		}
          		if (dap.isLocationSite()) 
          		{
          			ListItem item = new ListItem(getLabel("locationSite")); productsPhysicals.add(item);
          		}
          		if (dap.isTimeScale()) 
          		{
          			ListItem item = new ListItem(getLabel("timeScale")); productsPhysicals.add(item);
          		}
          		if (dap.isPast()) 
          		{
          			ListItem item = new ListItem(getLabel("past")); productsPhysicals.add(item);
          		}
          		if (dap.isCurrent()) 
          		{
          			ListItem item = new ListItem(getLabel("current")); productsPhysicals.add(item);
          		}
          		if (dap.isFuture()) 
          		{
          			ListItem item = new ListItem(getLabel("future")); productsPhysicals.add(item);
          		}
          		if (dap.isMethodology()) 
          		{
          			ListItem item = new ListItem(getLabel("methodology")); productsPhysicals.add(item);
          		}
          		if (dap.isObsInsitu()) 
          		{
          			ListItem item = new ListItem(getLabel("obsInsitu")); productsPhysicals.add(item);
          		}
          		if (dap.isObsRemote()) 
          		{
          			ListItem item = new ListItem(getLabel("obsRemote")); productsPhysicals.add(item);
          		}
          		if (dap.isModelStatic()) 
          		{
          			ListItem item = new ListItem(getLabel("modelStatic")); productsPhysicals.add(item);
          		}
          		if (dap.isModelDynamic()) 
          		{
          			ListItem item = new ListItem(getLabel("modelDynamic")); productsPhysicals.add(item);
          		}
          		if (dap.isProjections()) 
          		{
          			ListItem item = new ListItem(getLabel("projections")); productsPhysicals.add(item);
          		}
          		if (dap.isGuidance()) 
          		{
          			ListItem item = new ListItem(getLabel("guidance")); productsPhysicals.add(item);
          		}
          		if (dap.isApplications()) 
          		{
          			ListItem item = new ListItem(getLabel("applications")); productsPhysicals.add(item);
          		}
          		if (dap.isAtmosphericProducts()) 
          		{
          			ListItem item = new ListItem(getLabel("atmosphericProducts")); productsPhysicals.add(item);
          		}
          		if (dap.isOceanicProducts()) 
          		{
          			ListItem item = new ListItem(getLabel("oceanicProducts")); productsPhysicals.add(item);
          		}
          		if (dap.isTerrestrialProducts()) 
          		{
          			ListItem item = new ListItem(getLabel("terrestrialProducts")); productsPhysicals.add(item);
          		}

          		if (productsPhysicals.size() > 0)
          		{
           		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(productsPhysicals);
           		  table.addCell(new PdfPCell(new Phrase("Products/Physical")));  table.addCell(pdfCell);
          		}
          		
          	    // non-physical
          		com.itextpdf.text.List nonPhysicals = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isNonPhysical()) 
          		{
          			ListItem item = new ListItem(getLabel("nonPhysical")); nonPhysicals.add(item);
          		}
          		if (dap.isBiological()) 
          		{
          			ListItem item = new ListItem(getLabel("biological")); nonPhysicals.add(item);
          		}
          		if (dap.isSocioEconomic()) 
          		{
          			ListItem item = new ListItem(getLabel("socioEconomic")); nonPhysicals.add(item);
          		}
          		if (dap.isCultural()) 
          		{
          			ListItem item = new ListItem(getLabel("cultural")); nonPhysicals.add(item);
          		}
          		if (dap.isOtherNonPhysical()) 
          		{
          			ListItem item = new ListItem(getLabel("otherNonPhysical")); nonPhysicals.add(item);
          		}

          		if (nonPhysicals.size() > 0)
          		{
           		  PdfPCell pdfCell = new PdfPCell(); pdfCell.addElement(nonPhysicals);
           		  table.addCell(new PdfPCell(new Phrase("Non Physical")));  table.addCell(pdfCell);
          		}

          	    // sectors
          		com.itextpdf.text.List sectors = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (dap.isHealth()) 
          		{
          			ListItem item = new ListItem(getLabel("health")); sectors.add(item);
          		}
          		if (dap.isFreshWater()) 
          		{
          			ListItem item = new ListItem(getLabel("freshWater")); sectors.add(item);
          		}
          		if (dap.isEnergy()) 
          		{
          			ListItem item = new ListItem(getLabel("energy")); sectors.add(item);
          		}
          		if (dap.isTransportation()) 
          		{
          			ListItem item = new ListItem(getLabel("transportation")); sectors.add(item);
          		}
          		if (dap.isPlanning()) 
          		{
          			ListItem item = new ListItem(getLabel("planning")); sectors.add(item);
          		}
          		if (dap.isSocioCultural()) 
          		{
          			ListItem item = new ListItem(getLabel("socioCultural")); sectors.add(item);
          		}
          		if (dap.isAgriculture()) 
          		{
          			ListItem item = new ListItem(getLabel("agriculture")); sectors.add(item);
          		}
          		if (dap.isRecreation()) 
          		{
          			ListItem item = new ListItem(getLabel("recreation")); sectors.add(item);
          		}
          		if (dap.isEcological()) 
          		{
          			ListItem item = new ListItem(getLabel("ecological")); sectors.add(item);
          		}
          		if (dap.isOtherSector()) 
          		{
          			ListItem item = new ListItem(getLabel("otherSector")); sectors.add(item);
          		}


          		if(sectors.size() > 0)
          		{
          		  PdfPCell typesCell = new PdfPCell(); typesCell.addElement(sectors);
          		  table.addCell(new PdfPCell(new Phrase("Sectors")));  table.addCell(typesCell);
          		}
          		
	
          		
          		
          		
          		
          		// text fields
          		
          		if (StringUtils.isNotBlank(description))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Description")));  table.addCell(new PdfPCell(new Phrase(description)));
          		}
          		
          		if (StringUtils.isNotBlank(url))
          		{
                  Anchor link = new Anchor(StringUtils.trimToEmpty(url)); link.setReference(StringUtils.trimToEmpty(url));
          		  table.addCell(new PdfPCell(new Phrase("Url")));  table.addCell(new PdfPCell(link));
          		}

                if (StringUtils.isNotBlank(leadAgencies))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Lead Agencies")));  table.addCell(new PdfPCell(new Phrase(leadAgencies)));
          		}

          		if (StringUtils.isNotBlank(contacts))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Contacts")));  table.addCell(new PdfPCell(new Phrase(contacts)));
          		}
         		

          		
          		document.add(table);
          		document.add(Chunk.NEWLINE);

      		}
              
              
      } catch (DocumentException de) {
              logger.fatal(de.getMessage());
      }
      catch (IOException ie)
      {
    	 logger.warn("Could not find NOAA logo (likely)");
    	 logger.warn(ie);
      }

      // step 5: we close the document
      document.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return bais;
}

  ///////////////////////////////////////////////////////////////
  //  Action Event Handlers 
  //
  
  Object onSelectedFromSearch() 
  {
    return null; 
  }

  Object onSelectedFromClear() 
  {
    this.searchText = "";
   
    // nullify selectors 
    ecv=null;
    daptype=null;
    regions=null;
    
    this.example = null;
    return null; 
  }
  
  // regions select box listener...may be hooked-up to some AJAX zone if needed (later)
  Object onValueChangedFromRegions(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Region Select:  Example is NULL");
		  this.example = new Dap(); 
	  }
	  else
	  {
		  logger.info("Region Select:  Example is NOT null");
	  }
	  logger.info("Region Choice = " + choice);
	  
	  clearRegions(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("CNP"))
      {
    	example.setCentralNorthPacific(true);
    	logger.info("Example setCentralNorthPacific");
      }
      else if (choice.equalsIgnoreCase("WNP"))
      {
    	example.setWesternNorthPacific(true);
      }
      else if (choice.equalsIgnoreCase("SP"))
      {
    	example.setSouthPacific(true);  
      }
      else if (choice.equalsIgnoreCase("BAS"))
      {
    	example.setPacificBasin(true);   
      }
      else if (choice.equalsIgnoreCase("GLB"))
      {
    	example.setGlobal(true);
      }
      else
      {
    	  // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
	
  // ECV select box listener
  Object onValueChangedFromEcv(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("ECV Select: Example is NULL");
		  this.example = new Dap(); 
	  }
	  else
	  {
		  logger.info("ECV Select: Example is NOT null");
	  }
	  logger.info("ECV Choice = " + choice);
	  
	  clearEcv(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("ATM"))
      {
    	example.setAtmosphericData(true);
      }
      else if (choice.equalsIgnoreCase("OCE"))
      {
    	example.setOceanicData(true);
      }
      else if (choice.equalsIgnoreCase("TER"))
      {
    	example.setTerrestrialData(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
  // Dap Type box listener
  Object onValueChangedFromDaptype(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Daptype Select Value Changed, Example is NULL");
		  this.example = new Dap(); 
	  }
	  else
	  {
		  logger.info("Dap Type Select Value Changed, Example is NOT null");
	  }
	  logger.info("DAP Type Chosen = " + choice);
	   
	  clearDaptype(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("PHYSICAL"))
      {
    	example.setDataPhysical(true);
      }
      else if (choice.equalsIgnoreCase("HINDCAST"))
      {
    	example.setHindcasts(true);
      }
      else if (choice.equalsIgnoreCase("OUTLOOK"))
      {
    	example.setOutlooks(true);  
      }
      else if (choice.equalsIgnoreCase("PROJECTION"))
      {
    	example.setProjections(true);  
      }
      else if (choice.equalsIgnoreCase("GUIDANCE"))
      {
    	example.setGuidance(true);  
      }
      else if (choice.equalsIgnoreCase("APPS"))
      {
        example.setApplications(true);
      }
      else if (choice.equalsIgnoreCase("NONPHY"))
      {
        example.setNonPhysical(true);
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
 
  
  ////////////////////////////////////////////////
  //  QBE Setter 
  //  

  public void setExample(Dap x) 
  {
    this.example = x;
  }

  
  
  ///////////////////////////////////////////////////////
  // private methods 
  
  private void clearRegions(Dap dap)
  {
   	dap.setCentralNorthPacific(false);
  	dap.setWesternNorthPacific(false);
  	dap.setSouthPacific(false);
  	dap.setPacificBasin(false);
  	dap.setGlobal(false);
  }
  
  private void clearEcv(Dap dap)
  {
	dap.setAtmosphericData(false);
	dap.setOceanicData(false);
	dap.setTerrestrialData(false);
  }
  
  private void clearDaptype(Dap dap)
  {
	dap.setDataPhysical(false);
	dap.setHindcasts(false);
	dap.setOutlooks(false);
	dap.setProjections(false);
	dap.setGuidance(false);
	dap.setApplications(false);
	dap.setNonPhysical(false);
  }
  private String getLabel (String varName)
  {
	   String key = varName + "-label";
	   String value = "";
	   if (messages.contains(key)) value = messages.get(key);
	   else value = TapestryInternalUtils.toUserPresentable(varName);
	   return StringUtils.trimToEmpty(value);
  }
}