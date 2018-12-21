import java.io.InputStream;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

public class Main {
	
	public static void main(String[] args) {

		OntModelSpec s = new OntModelSpec(OntModelSpec.RDFS_MEM);
		OntDocumentManager mgr = new OntDocumentManager();
		s.setDocumentManager(mgr);
		OntModel ontModel = ModelFactory.createOntologyModel(s);
		Model model = ontModel.getBaseModel();

		InputStream in = FileManager.get().open("resources/biomass_power.xml");
		if (in == null) {
			throw new IllegalArgumentException("File: biomass_power.xml not found");
		}
		model.read(in, null);

		String name = "Cobalt-57";

		StringBuilder queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {?s ?p \"").append(name)
				.append("\"}");

		Query query = QueryFactory.create(queryString.toString());

		QueryExecution queryExec = QueryExecutionFactory.create(query, model);

		ResultSet rs = queryExec.execSelect();

		String urlIdentifier = "";

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			urlIdentifier = sol.getResource("s").toString();
			break;
		}

		queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {").append("<").append(urlIdentifier.toString())
				.append("> <http://openlca.org/schema/v1.0/refUnit> ?o }");

		query = QueryFactory.create(queryString.toString());
		queryExec = QueryExecutionFactory.create(query, model);
		rs = queryExec.execSelect();

		String unit = "";

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			unit = sol.getLiteral("o").toString();
			break;
		}

		queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {?s <http://openlca.org/schema/v1.0/flow> <")
				.append(urlIdentifier).append(">}");

		query = QueryFactory.create(queryString.toString());
		queryExec = QueryExecutionFactory.create(query, model);
		rs = queryExec.execSelect();
		String nodeId = "";

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			nodeId = sol.getResource("s").toString();
		}

		queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {<_:").append(nodeId)
				.append("> <http://openlca.org/schema/v1.0/amount> ?o }");

		query = QueryFactory.create(queryString.toString());
		queryExec = QueryExecutionFactory.create(query, model);
		rs = queryExec.execSelect();

		String amount = "";

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			amount = String.valueOf(sol.getLiteral("o").getDouble());
			break;
		}
		
		double[] inOut = getInOutValues(name, model);
		
		System.out.println(name + " : \ninput : " + inOut[0] + " " + unit + "\noutput : " + inOut[1] + " " + unit);
	}

	private static double[] getInOutValues(String name, Model model) {
		double[] res = {0, 0};
		
		StringBuilder queryString = new StringBuilder("SELECT DISTINCT ?s ?p ?o WHERE {?s ?p \"")
				  .append(name)
				  .append("\"}");

		Query query = QueryFactory.create(queryString.toString());

		QueryExecution queryExec = QueryExecutionFactory.create(query, model);

		ResultSet rs = queryExec.execSelect();

		String urlIdentifier = "";

		while (rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			urlIdentifier = sol.getResource("s").toString();
			
			String nodeId = getNodeId(urlIdentifier, model);
			
			res[isInput(nodeId, model) ? 0 : 1] += getAmountUsed(nodeId, model);
		}
		
		return res;
	}
	
	private static String getNodeId(String urlIdentifier, Model model) {
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
	
	private static double getAmountUsed(String nodeId, Model model) {
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
	
	private static boolean isInput(String nodeId, Model model) {
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
}