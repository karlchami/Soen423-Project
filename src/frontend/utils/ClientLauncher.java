package frontend.utils;

import frontend.implementation.frontendImpl;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import frontend.corba.*;

public class ClientLauncher {
	
	public static frontend getFEInterface(ORB orb, String store) throws NotFound, CannotProceed, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName {
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");  	 
    	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
    	frontend FE = frontendHelper.narrow(ncRef.resolve_str(store));
    	return FE;
	}
	
	public static void initializeORB(String[] args, String frontend_id)  {
		new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
            		ORB orb = ORB.init(args, null);
    				POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    				rootPOA.the_POAManager().activate();
    				frontendImpl frontEnd = new frontendImpl(orb, frontend_id);
					org.omg.CORBA.Object ref = rootPOA.servant_to_reference(frontEnd);
					frontend href = frontendHelper.narrow(ref);
					org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
					NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
					NameComponent path[] = ncRef.to_name(frontend_id);
					ncRef.rebind(path, href);
    		    	orb.run();
            	}
            	catch(Exception ex) {
            		
            	}
            }
            
        }).start();
		System.out.println("Frontend built");
	}
	
	// Handles logging
	public Logger startLogger(String userID) {
	    Logger logger = Logger.getLogger("client-log");
	    FileHandler fh;
	    try {
	        fh = new FileHandler("frontend/logs/client/" + userID + ".log");
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();
	        fh.setFormatter(formatter);
	
	    } catch (SecurityException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return logger;
	}
}
