import src.HTTPServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main
{
	public static void main(String[] args)
	{
		// @Todo: check if --home flag has been passed, then check next token to get the http server home location
		String homeLocation = ".";
		int httpServerPort = 80;
		if(args.length > 0)
		{
			if(args[0].equals("--port") && args.length > 1)
			{
				try
				{
					httpServerPort = Integer.parseInt(args[1]);
				}
				catch(NumberFormatException nfe)
				{
					System.out.println("Numero di porta specificato non valido.");
					System.out.println("Si ricorda che deve essere un valore compreso tra 0 e 65535.");
					System.exit(1);
				}
			}
		}
		try
		{
			HTTPServer server = new HTTPServer(httpServerPort);
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
