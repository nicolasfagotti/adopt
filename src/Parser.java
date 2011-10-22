import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// TODO	Permitir a los agentes tener un nombre String y manejar el identificador internamente.
// TODO CONTROLAR VALORES DEL XML.

public class Parser {

	private Document doc;
	private Vector<String> agents;
	private Hashtable<String,Vector<Integer>> domains; 
	Hashtable<String,String> varsAgent;
	Hashtable<String,String> varsDomain;
	Hashtable<String,Hashtable<String,Container>> constraints;
	ConstraintVar globalVar;
	private Vector<String> mcaVars;
	private int gBudget;
	private String root;
    protected static final Logger logger = Logger.getLogger(Dcop.class);


	/**
	 * Constructor de la clase. Se encarga de parsear el archivo que especifica la 
	 * ruta indicada en el parámetro "path".
	 */
	public Parser(String path) {
		logger.info("Parseando el archivo: "+path);

		try {
			
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc  = docBuilder.parse(new File(path));
    		root = null;
    		gBudget = 0;

            // Normalize text representation.
            doc.getDocumentElement().normalize();

            // Parseo y obtengo los agentes.
    		agents = new Vector<String>();
    		parseAgents();

            // Parseo y obtengo los dominios.
    		domains = new Hashtable<String, Vector<Integer>>();
    		parseDomains();    		

            // Parseo y obtengo las variables y sus relaciones con los agentes y dominios.
    		varsAgent  = new Hashtable<String, String>();
    		varsDomain = new Hashtable<String, String>();
    		parseVariables();

            // Parseo y obtengo los constraints.
    		constraints = new Hashtable<String,Hashtable<String,Container>>();
    		parseConstraints();

    		mcaVars = new Vector<String>();
    		
    		// Parseo y obtengo los constraints globales para el MCA.
    		globalVar = new ConstraintVar();
    		parseGlobalConstraints();

		} catch (SAXParseException err) {
			logger.error("** Parsing error" + ", line " 
	             + err.getLineNumber () + ", uri " + err.getSystemId ());
			logger.error(" " + err.getMessage ());

        } catch (SAXException e) {
        	Exception x = e.getException ();
        	((x == null) ? e : x).printStackTrace ();
        } catch (Throwable t) {
	        t.printStackTrace ();
        }
	}

	
	/**
	 * Devuelve la raíz que se utilizará en la generación del árbol.
	 */
	public String getRoot() {
		return root;
	}


	/**
	 * Parsea los agentes definidos en el archivo de entrada.
	 */
	public void parseAgents() {
        NodeList nodeList;
        Node node;
        Element element;
            
        nodeList = doc.getElementsByTagName("agents");
        node     = nodeList.item(0);
        element  = (Element)node;
        nodeList = element.getElementsByTagName("agent");
        for (int s = 0; s < nodeList.getLength(); s++) {
        	node    = nodeList.item(s);
        	element = (Element)node;
        	agents.add(element.getAttribute("name"));
        }
	}


	/**
	 * Obtiene los agentes definidos en el archivo de entrada.
	 */
	public Vector<String> getAgents() {
		return agents;
	}


	/**
	 * Parsea los dominios definidos en el archivo de entrada.
	 */
	public void parseDomains() {
		Vector<Integer> domain = new Vector<Integer>();
        NodeList nodeListParent, nodeList;
        Node node;
        Element element, mainElement;
        int intNumber;

        nodeListParent = doc.getElementsByTagName("domains");
        node           = nodeListParent.item(0);
        mainElement    = (Element)node;
        nodeListParent = mainElement.getElementsByTagName("domain");
        for (int s = 0; s < nodeListParent.getLength(); s++) {
        	node        = nodeListParent.item(s);
        	mainElement = (Element)node;

        	nodeList = mainElement.getElementsByTagName("value");
        	for (int c = 0; c < nodeList.getLength(); c++) {
	        	node    = nodeList.item(c);
	        	element = (Element)node;
	        	intNumber = Integer.parseInt(element.getTextContent());
	            domain.addElement(intNumber);
        	}
        	domains.put(mainElement.getAttribute("name"), domain);
        }
	}


	/**
	 * Obtiene los dominios definidos en el archivo de entrada.
	 */
	public Hashtable<String,Vector<Integer>> getDomains() {
        return domains;
	}

	
	/**
	 * Parsea las variables definidas en el archivo de entrada con su relación
	 * respecto al agente que las contiene.
	 */
	public void parseVariables() {
        Node node;
        Element element;
        NodeList nodeList;
        String name, agent, domain;

        nodeList = doc.getElementsByTagName("variable");
    	node     = nodeList.item(0);
    	element  = (Element)node;
    	if (element.hasAttribute("root")) {
    		root = element.getAttribute("root");
    	}
    	nodeList = element.getElementsByTagName("var");
    	for (int s = 0; s < nodeList.getLength(); s++) {
    		node    = nodeList.item(s);
        	element = (Element)node;
    		name    = element.getAttribute("name");
    		agent   = element.getAttribute("agent");
    		domain  = element.getAttribute("domain");
    		varsAgent.put(name, agent);
    		varsDomain.put(name, domain);

    		// Si no se definió la raíz en el archivo, tomo la primer variable.
    		if (root == null) { root = name; }
    	}
	}

	/**
	 * Parsea las variables definidas en el archivo de entrada con su relación
	 * respecto al agente que las contiene.
	 */
	public void parseGlobalConstraints() {
        Node node;
        Element element;
        NodeList nodeList, nodeListChild;
        String name;
        int cost, value;
        Hashtable<String, Integer> vars = new Hashtable<String, Integer>();

        nodeList = doc.getElementsByTagName("g-constraints");
        if (nodeList.getLength() > 0) {
	    	node    = nodeList.item(0);
	    	element = (Element)node;
	    	if (element.hasAttribute("g-budget")) {
	    		gBudget = Integer.parseInt(element.getAttribute("g-budget"));
	    	}
	    	nodeList = element.getElementsByTagName("g-constraint");
	    	for (int s = 0; s < nodeList.getLength(); s++) {
	    		node    = nodeList.item(s);
	        	element = (Element)node;
	    		cost    = Integer.parseInt(element.getAttribute("cost"));
	
	    		nodeListChild = element.getElementsByTagName("var");
	    		for (int t = 0; t < nodeListChild.getLength(); t++) {
	        		node    = nodeListChild.item(t);
	            	element = (Element)node;
	        		name    = element.getAttribute("name");
	        		value   = Integer.parseInt(element.getAttribute("value"));
	        		vars.put(name, value);
	        		if (!mcaVars.contains(name)) {
	        			mcaVars.add(name);
	        		}
	    		}
	    		globalVar.setCost(vars, cost);
	    	}
        } else {
        	globalVar = null;
        }
	}


	/**
	 * Obtiene los constraints globales, utilizados en el MCA.
	 */
	public ConstraintVar getGlobalConstraint() {
       	return globalVar;
	}

	/**
	 * Obtiene las variables utilizadas en el MCA.
	 */
	public Vector<String> getMCAVars() {
       	return mcaVars;
	}
	
	/**
	 * Obtiene el límite del MCA parseado.
	 */
	public int getGBudget() {
		return gBudget;
	}


	/**
	 * Obtiene las relaciones de las variables el agente que las contiene.
	 */
	public Hashtable<String,String> getVariablesAgent() {
		return varsAgent;
	}


	/**
	 * Obtiene las relaciones de las variables con su respectivo dominio.
	 */
	public Hashtable<String,String> getVariablesDomain() {
		return varsDomain;
	}


	/**
	 * Obtiene los constraints definidos en el archivo de entrada, donde los índices
	 * de la estructura que la función retorna corresponden a los agentes involucrados
	 * en el constraint. 
	 */
	public void parseConstraints() {
        NodeList nodeListParent, nodeListParent2, nodeList;
        int agentValue1, agentValue2, cost;
        String variable1, variable2;
        Node node;
        Element element, mainElement;
        nodeListParent = doc.getElementsByTagName("constraints");

        for (int s = 0; s < nodeListParent.getLength(); s++) {
        	Container constraint        = new Container(0);
        	Container reverseConstraint = new Container(0);
        	node    = nodeListParent.item(s);
        	element = (Element)node;
        	variable1 = element.getAttribute("variable1");
        	variable2 = element.getAttribute("variable2");

        	nodeListParent2 = element.getElementsByTagName("constraint");

        	for (int c = 0; c < nodeListParent2.getLength(); c++) {
        		node         = nodeListParent2.item(c);
        		mainElement  = (Element)node;

        		nodeList = mainElement.getElementsByTagName("value");
        		node     = nodeList.item(0);
        		element  = (Element)node;
        		agentValue1 = Integer.parseInt(element.getTextContent());

        		node     = nodeList.item(1);
        		element  = (Element)node;
        		agentValue2 = Integer.parseInt(element.getTextContent());

        		cost = Integer.parseInt(mainElement.getAttribute("cost"));
        		
        		constraint.addValue(agentValue1, agentValue2, cost);
        		reverseConstraint.addValue(agentValue2, agentValue1, cost);
        	}

			if (!constraints.containsKey(variable1)) {
				constraints.put(variable1, new Hashtable<String,Container>());
			}
			constraints.get(variable1).put(variable2, constraint);

			if (!constraints.containsKey(variable2)) {
				constraints.put(variable2, new Hashtable<String,Container>());
			}
			constraints.get(variable2).put(variable1, reverseConstraint);
        }
	}


	/**
	 * Obtiene los constraints definidos en el archivo de entrada, donde los índices
	 * de la estructura que la función retorna corresponden a los agentes involucrados
	 * en el constraint. 
	 */
	public Hashtable<String,Hashtable<String,Container>> getConstraints() {
		return constraints;
	}
}	