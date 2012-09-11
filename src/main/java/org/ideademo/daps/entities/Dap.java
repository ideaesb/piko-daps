package org.ideademo.daps.entities;

import java.lang.Comparable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import org.apache.tapestry5.beaneditor.NonVisual;


@Entity @Indexed
public class Dap implements Comparable<Dap>
{
	
	//////////////////////////////////////////
	//Reserved indexing id 
	
	@Id @GeneratedValue @DocumentId @NonVisual
	private Long id;
	
	
	//////////////////////////////////////////////
	//String fields (being a keyword for Lucene)
	//
	
	@Field
	private String code="";

	@Field @Column (length=1024)
	private String name="";
	
	@Field @Column (length=2048)
	private String organization="";
	
	@Field @Column (length=2048)
	private String contact="";
	
	@Field 
	private String url="";
	
	@Field @Column (length=4096)
	private String description="";
	
	@Field @Column (length=4096)
	private String keywords="";
	
	@Field 
	private String worksheet="";
	
	
	/////////////////////////////////////////////
	// Booleans 
	//
	
    
	//Capability Area
    private boolean variability = false; 
    private boolean impacts = false; 
	
	//Focus Area
	
    private boolean water = false;
	private boolean coastal = false; 
	private boolean ecosystem = false;

	 //Region/Locale
	private boolean centralNorthPacific = false;
	    private boolean stateOfHawaii = false;
	      private boolean northWestHawaiianIslands = false;
	      private boolean pacificRemoteIslands = false;

	private boolean westernNorthPacific = false;
	    private boolean cnmi = false;
	    private boolean fsm = false;
	    private boolean guam = false;
	    private boolean palau = false;
	    private boolean rmi = false;
	    private boolean otherWesternNorthPacific = false;
	    
	private boolean southPacific = false;
	    private boolean americanSamoa = false;
	    private boolean australia = false;
	    private boolean cookIslands = false; 
	    private boolean fiji = false;
	    private boolean frenchPolynesia = false;
	    private boolean kiribati = false; 
	    private boolean newZealand = false;
	    private boolean png = false; 
	    private boolean samoa = false;
	    private boolean solomonIslands = false; 
	    private boolean tonga = false;
	    private boolean tuvalu = false; 
	    private boolean vanuatu = false; 
	    private boolean otherSouthPacific = false;
	    
	private boolean pacificBasin = false;
	private boolean global = false;
	  
	///////////////////////////////////////////////////////  
	// Data and Products
	
	// Data 
	private boolean dataPhysical = false;
	// Types 
	private boolean insitu = false;
	private boolean remote = false;
	private boolean model = false;
	private boolean reanalysis = false;
	private boolean topo = false;
	private boolean imagery = false;
	// ECV
	private boolean atmosphericData = false;
	private boolean oceanicData = false;
	private boolean terrestrialData = false;
	
	// Products
	private boolean productsPhysical = false;
	// Types
	private boolean hindcasts = false;
	private boolean outlooks = false;
	    private boolean impactOutlooks = false;
	    private boolean drought = false;
	    private boolean flooding = false;
	    private boolean erosion = false;
	    private boolean bleaching  = false;
	    private boolean spatial  = false;
	    private boolean regionNation  = false;
	    private boolean locationSite = false;
	    private boolean timeScale = false;
	    private boolean past = false;
	    private boolean current = false;
	    private boolean future = false;
	    private boolean methodology = false;
	    private boolean obsInsitu = false;
	    private boolean obsRemote = false;
	    private boolean modelStatic = false;
	    private boolean modelDynamic = false;
    private boolean projections = false;
    private boolean guidance = false;
    private boolean applications = false;
	// ECV
	private boolean atmosphericProducts = false;
	private boolean oceanicProducts = false;
	private boolean terrestrialProducts = false;
	
	// Non-physical
	private boolean nonPhysical = false; 
	private boolean biological = false; 
	private boolean socioEconomic = false; 
	private boolean cultural = false; 
	private boolean otherNonPhysical = false; 
	
	// Sector
	private boolean health = false; 
	private boolean freshWater = false;
	private boolean energy = false;
	private boolean transportation = false;
	private boolean planning = false;
	private boolean socioCultural = false;
	private boolean agriculture = false;
	private boolean recreation = false;
	private boolean ecological = false;
	private boolean otherSector= false;
	
	
	
	  
	//////////////////////////////////////////////
	//  Internal 
	//
	private boolean worksheetExists = false;


	
	////////////////////////////
	// getters, setters
	


	public Long getId() {
		return id;
	}




	public void setId(Long id) {
		this.id = id;
	}




	public String getCode() {
		return code;
	}




	public void setCode(String code) {
		this.code = code;
	}




	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}




	public String getOrganization() {
		return organization;
	}




	public void setOrganization(String organization) {
		this.organization = organization;
	}




	public String getContact() {
		return contact;
	}




	public void setContact(String contact) {
		this.contact = contact;
	}




	public String getUrl() {
		return url;
	}




	public void setUrl(String url) {
		this.url = url;
	}




	public String getDescription() {
		return description;
	}




	public void setDescription(String description) {
		this.description = description;
	}




	public String getKeywords() {
		return keywords;
	}




	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}




	public String getWorksheet() {
		return worksheet;
	}




	public void setWorksheet(String worksheet) {
		this.worksheet = worksheet;
	}




	public boolean isVariability() {
		return variability;
	}




	public void setVariability(boolean variability) {
		this.variability = variability;
	}




	public boolean isImpacts() {
		return impacts;
	}




	public void setImpacts(boolean impacts) {
		this.impacts = impacts;
	}




	public boolean isWater() {
		return water;
	}




	public void setWater(boolean water) {
		this.water = water;
	}




	public boolean isCoastal() {
		return coastal;
	}




	public void setCoastal(boolean coastal) {
		this.coastal = coastal;
	}




	public boolean isEcosystem() {
		return ecosystem;
	}




	public void setEcosystem(boolean ecosystem) {
		this.ecosystem = ecosystem;
	}




	public boolean isCentralNorthPacific() {
		return centralNorthPacific;
	}




	public void setCentralNorthPacific(boolean centralNorthPacific) {
		this.centralNorthPacific = centralNorthPacific;
	}




	public boolean isStateOfHawaii() {
		return stateOfHawaii;
	}




	public void setStateOfHawaii(boolean stateOfHawaii) {
		this.stateOfHawaii = stateOfHawaii;
	}




	public boolean isNorthWestHawaiianIslands() {
		return northWestHawaiianIslands;
	}




	public void setNorthWestHawaiianIslands(boolean northWestHawaiianIslands) {
		this.northWestHawaiianIslands = northWestHawaiianIslands;
	}




	public boolean isPacificRemoteIslands() {
		return pacificRemoteIslands;
	}




	public void setPacificRemoteIslands(boolean pacificRemoteIslands) {
		this.pacificRemoteIslands = pacificRemoteIslands;
	}




	public boolean isWesternNorthPacific() {
		return westernNorthPacific;
	}




	public void setWesternNorthPacific(boolean westernNorthPacific) {
		this.westernNorthPacific = westernNorthPacific;
	}




	public boolean isCnmi() {
		return cnmi;
	}




	public void setCnmi(boolean cnmi) {
		this.cnmi = cnmi;
	}




	public boolean isFsm() {
		return fsm;
	}




	public void setFsm(boolean fsm) {
		this.fsm = fsm;
	}




	public boolean isGuam() {
		return guam;
	}




	public void setGuam(boolean guam) {
		this.guam = guam;
	}




	public boolean isPalau() {
		return palau;
	}




	public void setPalau(boolean palau) {
		this.palau = palau;
	}




	public boolean isRmi() {
		return rmi;
	}




	public void setRmi(boolean rmi) {
		this.rmi = rmi;
	}




	public boolean isOtherWesternNorthPacific() {
		return otherWesternNorthPacific;
	}




	public void setOtherWesternNorthPacific(boolean otherWesternNorthPacific) {
		this.otherWesternNorthPacific = otherWesternNorthPacific;
	}




	public boolean isSouthPacific() {
		return southPacific;
	}




	public void setSouthPacific(boolean southPacific) {
		this.southPacific = southPacific;
	}




	public boolean isAmericanSamoa() {
		return americanSamoa;
	}




	public void setAmericanSamoa(boolean americanSamoa) {
		this.americanSamoa = americanSamoa;
	}




	public boolean isAustralia() {
		return australia;
	}




	public void setAustralia(boolean australia) {
		this.australia = australia;
	}




	public boolean isCookIslands() {
		return cookIslands;
	}




	public void setCookIslands(boolean cookIslands) {
		this.cookIslands = cookIslands;
	}




	public boolean isFiji() {
		return fiji;
	}




	public void setFiji(boolean fiji) {
		this.fiji = fiji;
	}




	public boolean isFrenchPolynesia() {
		return frenchPolynesia;
	}




	public void setFrenchPolynesia(boolean frenchPolynesia) {
		this.frenchPolynesia = frenchPolynesia;
	}




	public boolean isKiribati() {
		return kiribati;
	}




	public void setKiribati(boolean kiribati) {
		this.kiribati = kiribati;
	}




	public boolean isNewZealand() {
		return newZealand;
	}




	public void setNewZealand(boolean newZealand) {
		this.newZealand = newZealand;
	}




	public boolean isPng() {
		return png;
	}




	public void setPng(boolean png) {
		this.png = png;
	}




	public boolean isSamoa() {
		return samoa;
	}




	public void setSamoa(boolean samoa) {
		this.samoa = samoa;
	}




	public boolean isSolomonIslands() {
		return solomonIslands;
	}




	public void setSolomonIslands(boolean solomonIslands) {
		this.solomonIslands = solomonIslands;
	}




	public boolean isTonga() {
		return tonga;
	}




	public void setTonga(boolean tonga) {
		this.tonga = tonga;
	}




	public boolean isTuvalu() {
		return tuvalu;
	}




	public void setTuvalu(boolean tuvalu) {
		this.tuvalu = tuvalu;
	}




	public boolean isVanuatu() {
		return vanuatu;
	}




	public void setVanuatu(boolean vanuatu) {
		this.vanuatu = vanuatu;
	}




	public boolean isOtherSouthPacific() {
		return otherSouthPacific;
	}




	public void setOtherSouthPacific(boolean otherSouthPacific) {
		this.otherSouthPacific = otherSouthPacific;
	}




	public boolean isPacificBasin() {
		return pacificBasin;
	}




	public void setPacificBasin(boolean pacificBasin) {
		this.pacificBasin = pacificBasin;
	}




	public boolean isGlobal() {
		return global;
	}




	public void setGlobal(boolean global) {
		this.global = global;
	}




	public boolean isDataPhysical() {
		return dataPhysical;
	}




	public void setDataPhysical(boolean dataPhysical) {
		this.dataPhysical = dataPhysical;
	}




	public boolean isInsitu() {
		return insitu;
	}




	public void setInsitu(boolean insitu) {
		this.insitu = insitu;
	}




	public boolean isRemote() {
		return remote;
	}




	public void setRemote(boolean remote) {
		this.remote = remote;
	}




	public boolean isModel() {
		return model;
	}




	public void setModel(boolean model) {
		this.model = model;
	}




	public boolean isReanalysis() {
		return reanalysis;
	}




	public void setReanalysis(boolean reanalysis) {
		this.reanalysis = reanalysis;
	}




	public boolean isTopo() {
		return topo;
	}




	public void setTopo(boolean topo) {
		this.topo = topo;
	}




	public boolean isImagery() {
		return imagery;
	}




	public void setImagery(boolean imagery) {
		this.imagery = imagery;
	}




	public boolean isAtmosphericData() {
		return atmosphericData;
	}




	public void setAtmosphericData(boolean atmosphericData) {
		this.atmosphericData = atmosphericData;
	}




	public boolean isOceanicData() {
		return oceanicData;
	}




	public void setOceanicData(boolean oceanicData) {
		this.oceanicData = oceanicData;
	}




	public boolean isTerrestrialData() {
		return terrestrialData;
	}




	public void setTerrestrialData(boolean terrestrialData) {
		this.terrestrialData = terrestrialData;
	}




	public boolean isProductsPhysical() {
		return productsPhysical;
	}




	public void setProductsPhysical(boolean productsPhysical) {
		this.productsPhysical = productsPhysical;
	}




	public boolean isHindcasts() {
		return hindcasts;
	}




	public void setHindcasts(boolean hindcasts) {
		this.hindcasts = hindcasts;
	}




	public boolean isOutlooks() {
		return outlooks;
	}




	public void setOutlooks(boolean outlooks) {
		this.outlooks = outlooks;
	}




	public boolean isImpactOutlooks() {
		return impactOutlooks;
	}




	public void setImpactOutlooks(boolean impactOutlooks) {
		this.impactOutlooks = impactOutlooks;
	}




	public boolean isDrought() {
		return drought;
	}




	public void setDrought(boolean drought) {
		this.drought = drought;
	}




	public boolean isFlooding() {
		return flooding;
	}




	public void setFlooding(boolean flooding) {
		this.flooding = flooding;
	}




	public boolean isErosion() {
		return erosion;
	}




	public void setErosion(boolean erosion) {
		this.erosion = erosion;
	}




	public boolean isBleaching() {
		return bleaching;
	}




	public void setBleaching(boolean bleaching) {
		this.bleaching = bleaching;
	}




	public boolean isSpatial() {
		return spatial;
	}




	public void setSpatial(boolean spatial) {
		this.spatial = spatial;
	}




	public boolean isRegionNation() {
		return regionNation;
	}




	public void setRegionNation(boolean regionNation) {
		this.regionNation = regionNation;
	}




	public boolean isLocationSite() {
		return locationSite;
	}




	public void setLocationSite(boolean locationSite) {
		this.locationSite = locationSite;
	}




	public boolean isTimeScale() {
		return timeScale;
	}




	public void setTimeScale(boolean timeScale) {
		this.timeScale = timeScale;
	}




	public boolean isPast() {
		return past;
	}




	public void setPast(boolean past) {
		this.past = past;
	}




	public boolean isCurrent() {
		return current;
	}




	public void setCurrent(boolean current) {
		this.current = current;
	}




	public boolean isFuture() {
		return future;
	}




	public void setFuture(boolean future) {
		this.future = future;
	}




	public boolean isMethodology() {
		return methodology;
	}




	public void setMethodology(boolean methodology) {
		this.methodology = methodology;
	}




	public boolean isObsInsitu() {
		return obsInsitu;
	}




	public void setObsInsitu(boolean obsInsitu) {
		this.obsInsitu = obsInsitu;
	}




	public boolean isObsRemote() {
		return obsRemote;
	}




	public void setObsRemote(boolean obsRemote) {
		this.obsRemote = obsRemote;
	}




	public boolean isModelStatic() {
		return modelStatic;
	}




	public void setModelStatic(boolean modelStatic) {
		this.modelStatic = modelStatic;
	}




	public boolean isModelDynamic() {
		return modelDynamic;
	}




	public void setModelDynamic(boolean modelDynamic) {
		this.modelDynamic = modelDynamic;
	}




	public boolean isProjections() {
		return projections;
	}




	public void setProjections(boolean projections) {
		this.projections = projections;
	}




	public boolean isGuidance() {
		return guidance;
	}




	public void setGuidance(boolean guidance) {
		this.guidance = guidance;
	}




	public boolean isApplications() {
		return applications;
	}




	public void setApplications(boolean applications) {
		this.applications = applications;
	}




	public boolean isAtmosphericProducts() {
		return atmosphericProducts;
	}




	public void setAtmosphericProducts(boolean atmosphericProducts) {
		this.atmosphericProducts = atmosphericProducts;
	}




	public boolean isOceanicProducts() {
		return oceanicProducts;
	}




	public void setOceanicProducts(boolean oceanicProducts) {
		this.oceanicProducts = oceanicProducts;
	}




	public boolean isTerrestrialProducts() {
		return terrestrialProducts;
	}




	public void setTerrestrialProducts(boolean terrestrialProducts) {
		this.terrestrialProducts = terrestrialProducts;
	}




	public boolean isNonPhysical() {
		return nonPhysical;
	}




	public void setNonPhysical(boolean nonPhysical) {
		this.nonPhysical = nonPhysical;
	}




	public boolean isBiological() {
		return biological;
	}




	public void setBiological(boolean biological) {
		this.biological = biological;
	}




	public boolean isSocioEconomic() {
		return socioEconomic;
	}




	public void setSocioEconomic(boolean socioEconomic) {
		this.socioEconomic = socioEconomic;
	}




	public boolean isCultural() {
		return cultural;
	}




	public void setCultural(boolean cultural) {
		this.cultural = cultural;
	}




	public boolean isOtherNonPhysical() {
		return otherNonPhysical;
	}




	public void setOtherNonPhysical(boolean otherNonPhysical) {
		this.otherNonPhysical = otherNonPhysical;
	}




	public boolean isHealth() {
		return health;
	}




	public void setHealth(boolean health) {
		this.health = health;
	}




	public boolean isFreshWater() {
		return freshWater;
	}




	public void setFreshWater(boolean freshWater) {
		this.freshWater = freshWater;
	}




	public boolean isEnergy() {
		return energy;
	}




	public void setEnergy(boolean energy) {
		this.energy = energy;
	}




	public boolean isTransportation() {
		return transportation;
	}




	public void setTransportation(boolean transportation) {
		this.transportation = transportation;
	}




	public boolean isPlanning() {
		return planning;
	}




	public void setPlanning(boolean planning) {
		this.planning = planning;
	}




	public boolean isSocioCultural() {
		return socioCultural;
	}




	public void setSocioCultural(boolean socioCultural) {
		this.socioCultural = socioCultural;
	}




	public boolean isAgriculture() {
		return agriculture;
	}




	public void setAgriculture(boolean agriculture) {
		this.agriculture = agriculture;
	}




	public boolean isRecreation() {
		return recreation;
	}




	public void setRecreation(boolean recreation) {
		this.recreation = recreation;
	}




	public boolean isEcological() {
		return ecological;
	}




	public void setEcological(boolean ecological) {
		this.ecological = ecological;
	}




	public boolean isOtherSector() {
		return otherSector;
	}




	public void setOtherSector(boolean otherSector) {
		this.otherSector = otherSector;
	}




	public boolean isWorksheetExists() {
		return worksheetExists;
	}




	public void setWorksheetExists(boolean worksheetExists) {
		this.worksheetExists = worksheetExists;
	}
	
	
	////////////////////////////////////////////////
	/// default/natural sort order - String  - names
	
	public int compareTo(Dap ao) 
	{
	    boolean thisIsEmpty = false;
	    boolean aoIsEmpty = false; 
	    
	    if (this.getName() == null || this.getName().trim().length() == 0) thisIsEmpty = true; 
	    if (ao.getName() == null || ao.getName().trim().length() == 0) aoIsEmpty = true;
	    
	    if (thisIsEmpty && aoIsEmpty) return 0;
	    if (thisIsEmpty && !aoIsEmpty) return -1;
	    if (!thisIsEmpty && aoIsEmpty) return 1; 
	    return this.getName().compareToIgnoreCase(ao.getName());
    }
	
	
}
