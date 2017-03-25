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
	private static boolean isInstanced;
	private static final String UNKNOWN_TYPE_IDENTIFIER = "text/plain";
	private static final String CONFIG_PATH = "MIME.conf";

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
		}
		catch(IOException ie)
		{
			ie.printStackTrace();  // @Todo scrivere su terminale un errore esplicativo e non lo stacktrace
		}
	}

	// Dato il nome di un file viene ritornato il MIME Type corrispondente alla sua estensione
	public static String getTypeFromFilename(String filename)
	{
		if(types == null)
			new MimeType();
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
