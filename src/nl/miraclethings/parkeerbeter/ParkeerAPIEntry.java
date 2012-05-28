package nl.miraclethings.parkeerbeter;

import java.io.Serializable;

public class ParkeerAPIEntry implements Serializable {

	private static final long serialVersionUID = 1123L;
	
	public String id;
	public String telefoon;
	public String gebruiker;
	public String kenteken;
	public String zonecode;
	public String locatie;
	public String stad;
	public String gestart;
	
	public ParkeerAPIEntry(String id) {
		this.id = id;
	}
	
	public ParkeerAPIEntry(String id, String telefoon, String gebruiker, 
			String kenteken, String zonecode, 
			String locatie, String stad, String gestart) 
	{
		this.id = id;
		this.telefoon = telefoon;
		this.gebruiker = gebruiker;
		this.kenteken = kenteken.toUpperCase();
		this.zonecode = zonecode;
		this.locatie = locatie;
		this.stad = stad;
		this.gestart = gestart;
	}
}
