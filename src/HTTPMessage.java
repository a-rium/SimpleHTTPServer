package src;

import java.util.HashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

public abstract class HTTPMessage extends HashMap<String, String> 
{
	protected String httpVersion;
	protected byte[] data;

	private static HTTPMessage parseCache = null;
	private static boolean isCached = false;

	protected HTTPMessage()
	{
		super();
	}

	// In base all'header fornito viene costruito o un HTTPResponse o un HTTPRequest, i quali sono entrambi HTTPMessage
	public static HTTPMessage getInstanceFromHeader(String header)
	{
		if(header.startsWith("HTTP"))
			return new HTTPResponse(header);
		else
			return new HTTPRequest(header);
	}

	// Metodo statico che permette di ottenere un messaggio HTTP in base al contenuto del file.
	// Il file dovra' essere cosi' impostato:
	// HTTP/<ver> <status> OK			// per risposta
	// <metodo> <risorsa richiesta> HTTP/<ver>	// per richiesta
	// <attributo>: <valore>
	// <attributo>: <valore>
	// <attributo>: <valore>
	// ...
	//
	// @Think E' veramente necessario "isCached"?
	public static HTTPMessage parseFromFile(String filename)
		throws IOException
	{
		if(!isCached)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String header = in.readLine();
			parseCache = HTTPMessage.getInstanceFromHeader(header);
			String line;
			while((line = in.readLine()) != null)
				parseCache.add(line);
			in.close();
			isCached = true;
		}
		return parseCache;
	}

	public abstract void setHeader(String header);

	public void add(String httpLine)
	{
		int endNameIndex = httpLine.indexOf(":");
		String attribute = httpLine.substring(0, endNameIndex).trim();
		String value = httpLine.substring(endNameIndex+1, httpLine.length()).trim();
		super.put(attribute, value);
	}

	public void add(String attribute, String value)
	{
		super.put(attribute, value);
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
	
	// Modifica la versione di HTTP utilizzata
	public void setHTTPVersion(String httpVersion)
	{
		this.httpVersion = httpVersion;
	}
	
	// Modifica il blocco dati con quello dato come parametro
	public void setData(byte[] data)
	{
		this.data = data;
	}
}
