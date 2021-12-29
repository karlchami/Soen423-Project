package frontend.utils;

public class Tuple<Inet, Port, Name> {
	
	private final Inet inet_address;
	private final Port port;
	private final Name name;
	
	public Tuple(Inet inet_address, Port port, Name name) {
		assert inet_address != null;
		this.inet_address = inet_address;

		assert port != null;
		this.port = port;
		
		assert name != null;
		this.name = name;		
	}
	
	public Inet getInetAddress() {
		return inet_address;
	}
	
	public Port getPort() {
		return port;
	}
	
	public Name getName() {
		return name;
	}
	
	public int hashCode() { 
		return inet_address.hashCode() ^ port.hashCode() ^ name.hashCode();
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Tuple)) {
			return false;
		}
		else {
			Tuple<?, ?, ?> tuple_obj = (Tuple<?, ?, ?>) obj;
			Boolean comparison = this.inet_address.equals(tuple_obj.getInetAddress()) && this.port.equals(tuple_obj.getPort()) && this.name.equals(tuple_obj.getName());
			return comparison;
		}
	}
	
}