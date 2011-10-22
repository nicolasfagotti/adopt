
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
 

    
public class Dcop {
	// Parsea el archivo de entrada. 
	static Tree dcop;
    static Parser parser;
    private static final Logger logger = Logger.getLogger(Dcop.class);
    private static JSAP jsap;
    
	/**
	 * Método principal.
	 * @throws JSAPException 
	 */
	public static void main(String args[]) throws JSAPException {
		
	   	PropertyConfigurator.configure("log4j.properties");
	    jsap = new JSAP();

	    FlaggedOption opt1 = new FlaggedOption("file")
	    .setStringParser(JSAP.STRING_PARSER)
	    .setRequired(true) 
	    .setShortFlag('f') 
	    .setLongFlag("file");
	    jsap.registerParameter(opt1);

	    FlaggedOption opt2 = new FlaggedOption("debugLevel")
	    .setStringParser(JSAP.STRING_PARSER)
	    .setDefault("ERROR") 
	    .setRequired(false) 
	    .setShortFlag('d') 
	    .setLongFlag("debug");
	    jsap.registerParameter(opt2);

	    FlaggedOption opt3 = new FlaggedOption("heuristic")
	    .setStringParser(JSAP.STRING_PARSER)
	    .setDefault("") 
	    .setRequired(false) 
	    .setShortFlag('h') 
	    .setLongFlag("heuristic");
	    jsap.registerParameter(opt3);
	    
	    JSAPResult config = jsap.parse(args);
	    
        String file = config.getString("file");
        String debugLevel = config.getString("debugLevel");
        String heuristic = config.getString("heuristic");

        if (!config.success() || (!heuristic.isEmpty() && !heuristic.matches("(DP0|DP1|DP2)"))) {
        	logger.error("Se requiere ingresar parametros");
        	logger.error("MODO DE USO: java -jar adopt.jar "+ jsap.getUsage());
            System.exit(1);
        }

        logger.setLevel(Level.toLevel(debugLevel));
		logger.info("debugLevel: "+debugLevel);
		Heuristic hrst = null;
		
		if (heuristic.equals("DP0")) {
			hrst = new DP0();
			logger.info("Se ha aplicado la heuristica: "+heuristic);
		}
		if (heuristic.equals("DP1")) {
			hrst = new DP1();
			logger.info("Se ha aplicado la heuristica: "+heuristic);
		}
		if (heuristic.equals("DP2")) {
			hrst = new DP2();
			logger.info("Se ha aplicado la heuristica: "+heuristic);
		}
		
		parser = new Parser(file);

		// Genera el arbol y ejecuta el DCOP.
		dcop = new Tree(parser, hrst);
		dcop.run();
	}
}
