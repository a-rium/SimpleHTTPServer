import src.HTTPServer;

import java.util.Arrays;
import java.util.List;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main
{
	public static void main(String[] args)
	{
		String homeLocation = ".";
		int httpServerPort = 80;
		if(args.length > 0)
		{
			// Si trasforma in una List il vettore degli argomenti, in modo da poter vedere l'indice delle varie flag
			// ed estrapolare facilmente ed efficacemente i valori dati come parametro da linea di comando
			List<String> argsList = Arrays.asList(args);
			// Se flagIndex e' l'indice del parametro "--port" allora il valore associato sara' nella posizione flagIndex+1
			int flagIndex = argsList.indexOf("--port");
			if(flagIndex >= 0 && args.length > 1 && flagIndex+1 < args.length)
			{
				try
				{
					httpServerPort = Integer.parseInt(args[flagIndex+1]);
				}
				catch(NumberFormatException nfe)
				{
					System.out.println("Numero di porta specificato non valido.");
					System.out.println("Si ricorda che deve essere un valore compreso tra 0 e 65535.");
					System.exit(1);
				}
			}
			flagIndex = argsList.indexOf("--home");
			if(flagIndex >= 0 && args.length > 1 && flagIndex+1 < args.length)
			{
				homeLocation = args[flagIndex+1];
				File explorer = new File(homeLocation);
				if(!explorer.exists())
				{
					System.out.println("Il path della cartella home dato non e' valido.");
					System.exit(2);
				}
				if(!explorer.isDirectory())
				{
					System.out.println("Il path dato indica una risorsa diversa da una cartella.");
					System.exit(2);
				}
			}
		}
		try
		{
			System.out.printf("Apertura server socket HTTP alla porta %d...", httpServerPort);
			HTTPServer server = new HTTPServer(httpServerPort, homeLocation);
			System.out.println("Fatto");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				System.out.printf("> ");
				String input = in.readLine();
				if(input.equals(""))
					break;
			}
			server.halt();
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
	}
}
