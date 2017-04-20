package src;

/** Classe che rappresenta una risposta HTTP */
public class HTTPResponse extends HTTPMessage
{
	private int status;

	public HTTPResponse()
	{
		super();
		status = -1;
	}

	public HTTPResponse(String header)
	{
		super();
		setHeader(header);
	}

	public void setHeader(String header)
	{
		int startVersionIndex = header.indexOf("/");
		int endVersionIndex = header.indexOf(" ", startVersionIndex);
		httpVersion = header.substring(startVersionIndex+1, endVersionIndex);
		int endStatusIndex = header.indexOf(" ", endVersionIndex+1);
		// tra la versione e lo status possono esserci molteplici spazi. In questo modo ci si assicura di prendere lo spazio posto dopo lo status
		while(endStatusIndex-endVersionIndex == 1) 
		{
			endVersionIndex = endStatusIndex;
			endStatusIndex = header.indexOf(" ", endVersionIndex+1);
		}
		status = Integer.parseInt(header.substring(endVersionIndex+1, endStatusIndex));
	}
	
	/** Ritorna lo status. Se non e' stato mai selezionato uno stato o il messaggio e' una richiesta il valore ritornato sara' -1 */
	public int getStatus()
	{
		return status;
	}

	/** Modifica lo status */
	public void setStatus(int status)
	{
		this.status = status;
	}

	/** Converte in stringa il messaggio, impostando l'header come fosse una risposta HTTP */
	public String toString()
	{
		String content = "HTTP/" + httpVersion + " " + status + " OK";
		for(String attribute : keySet())
			content += "\n" + attribute + ": " + get(attribute);
		content += "\n\n";
		return content;
	}
}
