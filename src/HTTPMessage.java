package src;

import java.util.HashMap;

// Classe che rappresenta un messaggio http
public class HTTPMessage extends HashMap<String, String>
{
	// se il metodo e la versione non sono conosciuti avranno come valore null
	private String method;
	private String httpVersion;
	private byte[] data;
	private String requestedResource;
	private int status;

	// Se si utilizza il construttore senza parametri verra' creato un messaggio nullo con method, data e versione impostati a null 
	public HTTPMessage()
	{
		super();
		method = null;
		httpVersion = null;
		data = null;
		requestedResource = null;
		status = -1;
	}

	// Inizializza method e httpVersion con la prima riga di un messaggio http, comunemente chiamata header
	public HTTPMessage(String header)
	{
		super();
		setHeader(header);
	}

	// Modifica la versione di HTTP utilizzata
	public void setHTTPVersion(String httpVersion)
	{
		this.httpVersion = httpVersion;
	}
	
	// Modifica method e httpVersion dato un header di messaggio http
	public void setHeader(String header)
	{
		int endMethodIndex = header.indexOf("/");	// endMethodIndex e' anche l'indice di inizio della stringa corrispondente alla risorsa richiesta
		int endRequestedResourceIndex = header.indexOf(" HTTP", endMethodIndex);
		requestedResource = header.substring(endMethodIndex, endRequestedResourceIndex);
		int startRequestedResourceIndex = header.indexOf("/", endRequestedResourceIndex);
		int startVersionIndex = header.indexOf("/", startRequestedResourceIndex) + 1;
		method = header.substring(0, endMethodIndex).trim();
		httpVersion = header.substring(startVersionIndex, header.length()).trim();
	}

	// Modifica il blocco dati con quello dato come parametro
	public void setData(byte[] data)
	{
		this.data = data;
	}

	// Modifica lo status
	public void setStatus(int status)
	{
		this.status = status;
	}

	// Data una riga di un messaggio HTTP aggiunge un valore alla HashMap interna		
	public void add(String httpLine)
	{
		int endNameIndex = httpLine.indexOf(":");
		String attribute = httpLine.substring(0, endNameIndex).trim();
		String value = httpLine.substring(endNameIndex+1, httpLine.length()).trim();
		super.put(attribute, value);
	}

	// Data un attributo e il valore associato viene aggiunta un'entrata alla HashMap interna		
	public void add(String attribute, String value)
	{
		super.put(attribute, value);
	}

	// Ritorna il metodo utilizzato per il passaggio di parametri con PHP(se non specificato il metodo ritorna null)
	public String getMethod()
	{
		return method;
	}

	// Ritorna la risorsa richiesta(se non e' stata richiesta nessuna risorsa allora ritorna null)
	public String getRequestedResource()
	{
		return requestedResource;
	}

	// Ritorna la versione di HTTP utilizzata per lo scambio dei messaggi(se non specificato il metodo ritorna null)
	public String getHttpVersion()
	{
		return httpVersion;
	}

	// Ritorna il blocco dati del messaggio HTTP(sotto forma di testo)(se non e' presente ritorn null)
	public byte[] getData()
	{
		return data;
	}

	// Ritorna lo status. Se non e' stato mai selezionato uno stato o il messaggio e' una richiesta il valore ritornato sara' -1
	public int getStatus()
	{
		return status;
	}

	// Converte in stringa il messaggio, impostando l'header come fosse una richiesta HTTP
	public String toRequest()
	{
		String content = method + " / HTTP/" + httpVersion;
		for(String attribute : keySet())
			content += "\n" + attribute + ": " + get(attribute);
		content += "\n\n";
		// content += data;
		// content += "\n\n";
		return content;
	}

	// Converte in stringa il messaggio, impostando l'header come fosse una risposta HTTP
	public String toResponse()
	{
		String content = "HTTP/" + httpVersion + " " + status + " OK";
		for(String attribute : keySet())
			content += "\n" + attribute + ": " + get(attribute);
		content += "\n\n";
		// content += data;
		// content += "\n\n";
		return content;
	}
}
