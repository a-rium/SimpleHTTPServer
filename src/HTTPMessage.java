package src;

import java.util.HashMap;

// Classe che rappresenta un messaggio http
public class HTTPMessage extends HashMap<String, String>
{
	// se il metodo e la versione non sono conosciuti avranno come valore null
	private String method;
	private String httpVersion;

	// Se si utilizza il construttore senza parametri verra' creato un messaggio nullo con method e versione impostati a null 
	public HTTPMessage()
	{
		super();
		method = null;
		httpVersion = null;
	}

	// Inizializza method e httpVersion con la prima riga di un messaggio http, comunemente chiamata header
	public HTTPMessage(String header)
	{
		super();
		setHeader(header);
	}
	
	// Modifica method e httpVersion dato un header di messaggio http
	public void setHeader(String header)
	{
		int endMethodIndex = header.find("/");
		int startVersionIndex = header.find("/", endMethodIndex) + 1;
		method = header.substring(0, endMethodIndex).trim();
		httpVersion = header.substring(startVersionIndex, header.length).trim();
	}

	// Data una riga di un messaggio HTTP aggiunge un valore alla HashMap interna		
	public void add(String httpLine)
	{
		int endNameIndex = httpLine.find(":");
		String name = httpLine.substring(0, endNameIndex).trim();
		String value = httpLine.substring(endNameIndex+1, httpLine.length).trim();
		super.put(name, value);
	}

	// Ritorna il metodo utilizzato per il passaggio di parametri con PHP(se non specificato il metodo ritorna null)
	public String getMethod()
	{
		return method;
	}

	// Ritorna la versione di HTTP utilizzata per lo scambio dei messaggi(se non specificato il metodo ritorna null)
	public String getHttpVersion()
	{
		return httpVersion;
	}
}
