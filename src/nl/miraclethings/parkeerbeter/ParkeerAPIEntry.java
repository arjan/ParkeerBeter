package nl.miraclethings.parkeerbeter;

public class ParkeerAPIEntry {

	public String id;
	public String telefoon;
	public String gebruiker;
	public String kenteken;
	public String zonecode;
	public String locatie;
	public String stad;
	public String gestart;
	
	public ParkeerAPIEntry(String id, String telefoon, String gebruiker, String kenteken, String zonecode, 
			String locatie, String stad, String gestart) 
	{
		this.id = id;
		this.telefoon = telefoon;
		this.gebruiker = gebruiker;
		this.kenteken = kenteken;
		this.zonecode = zonecode;
		this.locatie = locatie;
		this.stad = stad;
		this.gestart = gestart;
	}
}
