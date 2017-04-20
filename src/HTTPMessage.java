package src;

import java.util.HashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

/** Classe che rappresenta un messaggio di scambio utilizzato nel protocollo HTTP.<br>
 *  Contiene inoltre una serie di metodi statici per creare istanze di messaggio dato un modello contenuto in un file esterno
 */
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

	/** In base all'header fornito viene costruito o un HTTPResponse o un HTTPRequest, i quali sono entrambi HTTPMessage*/
	public static HTTPMessage getInstanceFromHeader(String header)
	{
		if(header.startsWith("HTTP"))
			return new HTTPResponse(header);
		else
			return new HTTPRequest(header);
	}

	/** Metodo statico che permette di ottenere un messaggio HTTP in base al contenuto del file.<br>
	 *  Il file dovra' essere cosi' impostato:<br>
	 *  HTTP/&lt;ver&gt; &lt;status&gt; OK			// per risposta<br>
	 *  &lt;metodo&gt; &lt;risorsa richiesta&gt; HTTP/&lt;ver&gt;	// per richiesta<br>
	 *  &lt;attributo&gt;: &lt;valore&gt;<br>
         *  &lt;attributo&gt;: &lt;valore&gt;<br>
	 *  &lt;attributo&gt;: &lt;valore&gt;<br>
      	 *  ...
	 */
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
		String[] tokens = httpLine.split(":");
		super.put(tokens[0].trim(), tokens[1].trim());
	}

	public void add(String attribute, String value)
	{
		super.put(attribute, value);
	}

	/** Ritorna la versione di HTTP utilizzata per lo scambio dei messaggi(se non specificato il metodo ritorna null) */
	public String getHttpVersion()
	{
		return httpVersion;
	}

	/** Ritorna il blocco dati del messaggio HTTP(sotto forma di testo)(se non e' presente ritorn null) */
	public byte[] getData()
	{
		return data;
	}
	
	/** Modifica la versione di HTTP utilizzata */
	public void setHTTPVersion(String httpVersion)
	{
		this.httpVersion = httpVersion;
	}
	
	/** Modifica il blocco dati con quello dato come parametro */
	public void setData(byte[] data)
	{
		this.data = data;
	}
}
