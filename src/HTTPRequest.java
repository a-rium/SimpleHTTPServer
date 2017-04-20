package src;

/** Classe che rappresenta una richiesta HTTP */
public class HTTPRequest extends HTTPMessage
{
	private String method;
	private String requestedResource;

	public HTTPRequest()
	{
		method = null;
		requestedResource = null;
	}

	public HTTPRequest(String header)
	{
		super();
		setHeader(header);
	}

	/** Modifica method e httpVersion dato un header di messaggio HTTP */
	public void setHeader(String header)
	{
		String[] tokens = header.split(" ");
		method = tokens[0].trim();
		requestedResource = tokens[1].trim(); 
		httpVersion = tokens[2].trim(); 
	}

	/** Ritorna il metodo utilizzato per il passaggio di parametri con PHP(se non specificato il metodo ritorna null) */
	public String getMethod()
	{
		return method;
	}

	/** Ritorna la risorsa richiesta(se non e' stata richiesta nessuna risorsa allora ritorna null) */
	public String getRequestedResource()
	{
		return requestedResource;
	}

	/** Modifica il metodo di invio dei parametri */
	public void setMethod(String method)
	{
		this.method = method;
	}

	//* Modifica la risorsa richiesta */
	public void setRequestedResource(String requestedResource)
	{
		this.requestedResource = requestedResource;
	}

	/** Converte in stringa il messaggio, impostando l'header come fosse una richiesta HTTP */
	public String toString()
	{
		String content = method + " / HTTP/" + httpVersion;
		for(String attribute : keySet())
			content += "\n" + attribute + ": " + get(attribute);
		content += "\n\n";
		// content += data;
		// content += "\n\n";
		return content;
	}
}
