package com.isep.needsEnergyProject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

/**
 * The object representation of a LCA dataset. Allows to make searches for 
 * the input and output values of an element in the dataset from its name.
 * 
 * @author William Aboucaya
 *
 */
public class LCADataset {

	private Model model;
	
	/**
	 * Instantiates a new {@link LCADataset} for the indicated RDF file. Defines its ontology and stores its {@link Model}
	 * 
	 * @param path The relative path to the RDF file we want to get.
	 */
	
	public LCADataset(String path) {
		OntModelSpec s = new OntModelSpec(OntModelSpec.RDFS_MEM);
		OntDocumentManager mgr = new OntDocumentManager();
		s.setDocumentManager(mgr);
		OntModel ontModel = ModelFactory.createOntologyModel(s);
		model = ontModel.getBaseModel();

		InputStream in = FileManager.get().open(path);
		if (in == null) {
			throw new IllegalArgumentException("File: " + path + "biomass_power.xml not found");
		}
		model.read(in, null);
	}
	
	/**
	 * Searches through the dataset to find the input and output quantities of the requested element.
	 * 
	 * @param name the name of the element.
	 * @return the {@link InOutValue} object containing the produced and needed amount of the chosen element.
	 * @throws NoSuchElementException if the entry doesn't exist.
	 */
	
	public InOutValue getInOutValue(String name) throws NoSuchElementException {
		InOutValue inOut = new InOutValue(name);
		
		for (String urlIdentifier : getUrlIdentifiers(name)) {
			String nodeId = getNodeId(urlIdentifier);
			
			if(isInput(nodeId)) {
				inOut.addToInAmount(getAmountUsed(nodeId));
			} else {
				inOut.addToOutAmount(getAmountUsed(nodeId));
			}
			
			inOut.setUnit(getUnit(urlIdentifier));
		}
			
		return inOut;
	}
	
	/**
	 * 
	 * @param name the name of the element.
	 * @return the fixed identifier of an element.
	 * @throws NoSuchElementException NoSuchElementException if the entry doesn't exist.
	 */
	private String[] getUrlIdentifiers(String name) throws NoSuchElementException {
		StringBuilder queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {?s ?p \"")
				.append(name)
				.append("\"}");

		Query query = QueryFactory.create(queryString.toString());

		QueryExecution queryExec = QueryExecutionFactory.create(query, model);

		ResultSet rs = queryExec.execSelect();

		List<String> urlIdentifiersList = new ArrayList<>();

		if (!rs.hasNext()) {
			throw new NoSuchElementException("This dataset has no entry for name \"" + name + "\"");
		}
		
		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			urlIdentifiersList.add(sol.getResource("s").toString());
		}
		
		return urlIdentifiersList.toArray(new String[1]);
	}
	
	/**
	 * 
	 * @param urlIdentifier the fixed identifier of an element.
	 * @return the generated node ID
	 */
	
	private String getNodeId(String urlIdentifier) {
		String nodeId = "";
		
		StringBuilder queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {?s <http://openlca.org/schema/v1.0/flow> <")
				.append(urlIdentifier).append(">}");

		Query query = QueryFactory.create(queryString.toString());
		QueryExecution queryExec = QueryExecutionFactory.create(query, model);
		ResultSet rs = queryExec.execSelect();

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			nodeId = sol.getResource("s").toString();
		}
		
		return nodeId;
	}
	
	/**
	 * @param nodeId the generated node ID of this specific flow
	 * @return the amount related to this flow.
	 */
	public double getAmountUsed(String nodeId) {
		double res = 0;

		StringBuilder queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {<_:").append(nodeId)
				.append("> <http://openlca.org/schema/v1.0/amount> ?o }");

		Query query = QueryFactory.create(queryString.toString());
		QueryExecution queryExec = QueryExecutionFactory.create(query, model);
		ResultSet rs = queryExec.execSelect();

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			res = sol.getLiteral("o").getDouble();
			break;
		}
		
		return res;
	}
	
	/**
	 * @param nodeId the generated node ID of this specific flow
	 * @return true if the amount described is needed for the production in this flow, false if it is produced.
	 */
	public boolean isInput(String nodeId) {
		boolean input = false;
		
		StringBuilder queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {<_:").append(nodeId)
				.append("> <http://openlca.org/schema/v1.0/input> ?o }");

		Query query = QueryFactory.create(queryString.toString());
		QueryExecution queryExec = QueryExecutionFactory.create(query, model);
		ResultSet rs = queryExec.execSelect();

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			input = sol.getLiteral("o").getBoolean();
			break;
		}
		
		return input;
	}
	
	/**
	 * @param urlIdentifier the fixed identifier of an element.
	 * @return the unit in which the amounts are quantified for this element
	 */
	private String getUnit(String urlIdentifier) {
		StringBuilder queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {")
				.append("<")
				.append(urlIdentifier)
				.append("> <http://openlca.org/schema/v1.0/refUnit> ?o }");

		Query query = QueryFactory.create(queryString.toString());
		QueryExecution queryExec = QueryExecutionFactory.create(query, model);
		ResultSet rs = queryExec.execSelect();

		String unit = "";

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			unit = sol.getLiteral("o").toString();
			break;
		}
		
		return unit;
	}
}