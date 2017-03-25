package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;

// Classe che offre metodi relative al MIME Type
public class MimeType
{
	private static HashMap<String, String> types;
	private static boolean isInstanced = false;
	private static final String UNKNOWN_TYPE_IDENTIFIER = "text/plain";
	private static final String CONFIG_PATH = "MIME.conf";

	// Il costruttore viene chiamato una sola volta, cioe' quando uno dei metodi viene chiamato per la prima volta.
	// Leggendo il file indicato da CONFIG_PATH costrusce un dizionario che ha come chiavi le estensioni dei file e come
	// valore il tipo MIME associato
	// Il file e' cosi' costruito:
	// <estensione> <mime-type>
	// <estensione> <mime-type>
	// ...
	private MimeType()
	{
		try
		{
			types = new HashMap<String, String>();
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CONFIG_PATH))));
			while(true)
			{
				String line = in.readLine();
				if(line == null)
					break;
				if(line.equals(""))
					continue;
				String[] token = line.split(" ");
				types.put(token[0], token[1]);
			}
			isInstanced = true;
		}
		catch(IOException ie)
		{
			ie.printStackTrace();  // @Todo scrivere su terminale un errore esplicativo e non lo stacktrace
		}
	}

	// Dato il nome di un file viene ritornato il MIME Type corrispondente alla sua estensione
	// Il metodo e' sincronizzato per evitare il caso che 2 entita' eseguono il controllo per verificare
	// se la classe e' gia' stata instanziata in precedenza allo stesso tempo, generando un problema di concorrenza.
	// Sebbene l'unico effetto negativo sarebbe che le risorse verrebbero' caricate 2 volte, rendendo il metodo sincronizzato
	// si elimina il problema alla radice.
	public synchronized static String getTypeFromFilename(String filename)
	{
		// Si controlla che la classe sia stata instanziata precedentemente, che significherebbe che tutte le risorse sono gia' state ricaricate.
		// Se non lo e' viene instanziata e le risorse caricate.
		if(!isInstanced)
			new MimeType();
		// Per poter ricavare l'estensione dal nome del file si cerca l'indice dell'ultimo punto all'interno della stringa,
		// dopodiche' si estrapola una sottostringa che inizia dall'indice trovato (+ 1 per saltare il punto) alla fine della stringa
		int lastDotIndex = filename.indexOf(".");
		if(lastDotIndex < 0)
			return UNKNOWN_TYPE_IDENTIFIER;
		int nextDotIndex = filename.indexOf(".", lastDotIndex+1);
		while(nextDotIndex >= 0)
		{
			lastDotIndex = nextDotIndex;
			nextDotIndex = filename.indexOf(".", lastDotIndex+1);
		}
		String extension = filename.substring(lastDotIndex+1, filename.length());
		String mimeType = types.get(extension);
		if(mimeType == null)
			return UNKNOWN_TYPE_IDENTIFIER;
		return mimeType;
	}
}
