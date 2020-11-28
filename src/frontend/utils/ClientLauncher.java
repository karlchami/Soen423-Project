package frontend.utils;

import frontend.corba.frontend;
import frontend.corba.frontendHelper;
import frontend.implementation.frontendImpl;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class ClientLauncher {
	
	public static frontend getFEInterface(ORB orb) throws NotFound, CannotProceed, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName {
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");  	 
    	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		return frontendHelper.narrow(ncRef.resolve_str("FrontEnd"));
	}

	public static void initializeORB(String[] args) {
		try {
			ORB orb = ORB.init(args, null);
			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();
			frontendImpl frontEnd = new frontendImpl(orb);
			org.omg.CORBA.Object ref = rootPOA.servant_to_reference(frontEnd);
			frontend href = frontendHelper.narrow(ref);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			NameComponent[] path = ncRef.to_name("FrontEnd");
			ncRef.rebind(path, href);
			System.out.println("Frontend is running...");
			orb.run();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
