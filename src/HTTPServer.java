package src;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import java.nio.file.Files;

import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class HTTPServer
{
	// @ForNow
	protected static int connectionNumber = 0;

	protected String root;
	protected final String DEFAULT_PAGE = "index.html";
	protected final String ERROR_PAGE = "404.html";
	protected final String SAMPLE_RESPONSE_FILEPATH = "response.fmt";
	protected final String ALTERNATIVE_RESOURCE_POSTFIX = "-alt";
	protected ServerSocket server;
	protected AccepterThread accepter;
	protected ArrayList<ConnectionThread> connectionThreads;

	public HTTPServer(int port, String homeLocation)
		throws IOException
	{
		server = new ServerSocket(port);
		connectionThreads = new ArrayList<ConnectionThread>();	
		// Per sicurezza viene ricontrollato che la cartella indicata esista e che sia effettivamente una cartella e non qualcos'altro
		File explorer = new File(homeLocation);
		if(!explorer.exists())
			throw new IOException("Home directory non valida: non e' una cartella");
		if(!explorer.isDirectory())
			throw new IOException("Home directory non valida: la risorsa indicata non e' una directory.");
		root = homeLocation;
		// @Temp @Debug Al momento il log viene fatto nella cartella indicata dall'utente da linea di comando, in futuro probabilmente tornera' ad essere
		// nella cartella principale dove e' contenuto l'eseguibile del server
		explorer = new File(root + "/log");
		if(!explorer.exists())
			explorer.mkdir();
		accepter = new AccepterThread();
		accepter.start();
	}

	public void halt()
		throws IOException
	{
		accepter.halt();
		for(ConnectionThread connection : connectionThreads)
			connection.halt();
	}
	
	// Thread che rimane in costante ascolto di richieste di handshaking.
	// Ricevuta una richiesta genera un ConnectionThread che si occupi della connessione, dopodiche' ritorna ad ascoltare
	private class AccepterThread extends Thread
	{
		private boolean running = true;

		public AccepterThread(){}

		@Override
		public void run()
		{
			while(running)
			{
				try
				{
					Socket newConnection = server.accept();
					ConnectionThread connectionThread = new ConnectionThread(newConnection);
					connectionThread.start();
					connectionThreads.add(connectionThread);
				}
				catch(SocketException ste)
				{
					if(!running) // eccezione scaturita dalla chiusura della ServerSocket, quindi uscire dal ciclo e terminare il thread
						break;
					else
						ste.printStackTrace();
				}
				catch(IOException ie)
				{
					ie.printStackTrace();
				}
			}
		}

		public void halt()
			throws IOException
		{
			running = false;
			server.close();
		}

		public boolean isRunning()
		{
			return running;
		}
	}
	
	// Ascolta le richieste provenienti dalle socket, quindi formula risposte idonee e le invia
	private class ConnectionThread extends Thread
	{
		private Socket connection;
		// la stream 'out' e' utilizzata per inviare messaggi testuali(gli header della risposta HTTP
		private PrintWriter out;
		// la stream 'dataOut' e' utilizzata per inviare messaggi in binario(cioe' le risorse richieste dal client, il body del messaggio HTTP)
		private OutputStream dataOut;
		private BufferedReader in;

		// stream per salvare le richieste e le risposte date in un log
		private PrintWriter log;

		private boolean running = true;

		public ConnectionThread(Socket socket)
			throws IOException
		{
			connection = socket;
			dataOut = connection.getOutputStream();
			out = new PrintWriter(connection.getOutputStream());
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			log = new PrintWriter(new FileOutputStream(root + "/log/" + connection.getLocalPort() + "-" + connectionNumber + ".txt"));
			connectionNumber++;
		}
		
		@Override
		public void run()
		{
			while(running)
			{
				try
				{
					HTTPRequest request = readRequest();
					String requestedResource = request.getRequestedResource().trim();
					if(requestedResource.equals("/"))
						requestedResource = "/" + DEFAULT_PAGE;
					// @Ugly E' bruttissimo il costante controllo get/set di requestResource
					request.setRequestedResource(requestedResource);
					checkCookies(request);
					requestedResource = request.getRequestedResource(); // potrebbe essere modificata da checkCookie
					File explorer = new File(root + requestedResource);
					HTTPMessage response;
					if(!explorer.exists())
					{
						System.out.println("Richiesta fallita: la risorsa " + requestedResource + " non esiste");
						response = buildErrorPage();
					}
					else if(explorer.isDirectory())
					{
						System.out.println("Richiesta fallita: la risorsa " + requestedResource + " e' una directory");
						response = buildErrorPage();
					}
					else
						response = buildSimpleHTMLResponse(requestedResource);
						// System.out.printf(response.toResponse());  // @Debug: stampa la risposta su terminale	
					out.printf(response.toString());  // invio header della richiesta
					out.flush();
					dataOut.write(response.getData());  // invio body della richiesta
					log.printf("--> Richiesta:\n");
					log.printf(request.toString());
					log.printf("\n--> Risposta:\n");
					log.printf(response.toString());
					// @ForNow
					out.close();
					dataOut.close();
					in.close();
					log.close();
					connection.close();
					running = false;	
				}
				catch(SocketException ste)
				{
					if(!running)
						break;
					else
						ste.printStackTrace();
				}
				catch(IOException ie)
				{
					ie.printStackTrace();
				}
			}
		}

		// Legge dalla stream di input della socket una richiesta HTTP e la ritorna
		private HTTPRequest readRequest()
			throws IOException
		{
			String header = in.readLine();
			System.out.println("Arrivata richiesta, header: " + header);
			HTTPRequest request = new HTTPRequest(header);
			String currentLine = "";
			while(true)
			{
				currentLine = in.readLine();
				if(currentLine.equals(""))
					break;
				request.add(currentLine);
			}

			return request;
		}

		public void checkCookies(HTTPRequest request)
		{
			// Si controlla se il client ha inviato il cookie "alternative"
			// Se si allora si guarda se il valore di questo cookie e' "yes"
			// Se lo e' si tenta di richiamare una versione alternativa della risorsa,
			// cioe' un file denominato "<nome file>ALTERNATIVE_RESOURCE_POSTFIX<estensione>"
			// Se tale file non esiste allora si ritorna la risorsa originale
			String requestedResource = request.getRequestedResource();
			String cookieFieldValue = request.get("Cookie");
			if(cookieFieldValue != null)
			{
				String[] cookies = cookieFieldValue.split(";");
				for(String cookie : cookies)
				{
					String[] cookiePair = cookie.split("=");
					if(cookiePair[0].equals("alternative"))
					{
						if(cookiePair[1].equals("yes"))
						{
							String alternativeResourceVersion = getAlternativeResource(requestedResource);
							if(alternativeResourceVersion != null)
								request.setRequestedResource(alternativeResourceVersion);
						}
						break;
					}
				}
			}
		}
		
		// Costruisce una semplice risposta HTTP appendendo come body il contenuto del file indicato come parametro
		public HTTPMessage buildSimpleHTMLResponse(String requestedResource)
			throws IOException
		{
			HTTPMessage response = HTTPMessage.parseFromFile(root + "/" + SAMPLE_RESPONSE_FILEPATH);
			response.add("Date", currentDate());
			response.add("Content-type", MimeType.getTypeFromFilename(requestedResource));
			
			byte[] content = Files.readAllBytes(new File(root + requestedResource).toPath());
			response.add("Content-length", "" + content.length);
			response.setData(content);

			return response;
		}	

		// Costruisce una schermata di errore
		public HTTPMessage buildErrorPage()
			throws IOException
		{
			HTTPResponse response = new HTTPResponse();
			response.setHTTPVersion("1.1");
			response.setStatus(404);
			response.add("Date", currentDate());
			response.add("Server", "Java SimpleHTTPServer");
			response.add("Connection", "closed");
			response.add("Content-type", "text/html");
			
			byte[] content = Files.readAllBytes(new File(root + "/" + ERROR_PAGE).toPath());
			response.add("Content-length", "" + content.length);
			response.setData(content);

			return response;
		}

		public void halt()
			throws IOException
		{
			running = false;
			out.close();
			dataOut.close();
			in.close();
			connection.close();
		}

		public boolean isRunning()
		{
			return running;
		}

		// Ritorna data e ora corrente formattata secondo lo standard del protocollo HTTP: <giorno>, <numero giorno> <mese> <anno> <ora>:<minuto>:<secondo>
		public String currentDate()
		{
			final String[] dayOfWeekString = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
			final String[] monthString = {"Jen", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
			Locale locale = new Locale("en", "EN");
			TimeZone timeZone = TimeZone.getTimeZone("Europe/Rome");
			Calendar currentDate = Calendar.getInstance(timeZone, Locale.US);
			int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) - 1;
			int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);
			int month = currentDate.get(Calendar.MONTH);
			int year = currentDate.get(Calendar.YEAR);
			int hours = currentDate.get(Calendar.HOUR);
			int minutes = currentDate.get(Calendar.MINUTE);
			int seconds = currentDate.get(Calendar.SECOND);
			return dayOfWeekString[dayOfWeek] + ", " + dayOfMonth + " " + monthString[month] + " " + year + " " + hours + ":" + minutes + ":" + seconds;
		}

		public String getAlternativeResource(String requestedResource)
		{
			int startExtension = requestedResource.lastIndexOf(".");
			String nameWithoutExtension = requestedResource.substring(0, startExtension);
			String extension = requestedResource.substring(startExtension, requestedResource.length());
			String alternativeResource = nameWithoutExtension + ALTERNATIVE_RESOURCE_POSTFIX + extension;
			File explorer = new File(root + alternativeResource);
			if(explorer.exists())
				return alternativeResource;
			else
				return null;
		}

		// Ritorna una risorsa testuale(html, css, js...) letta da file collocato alla posizione indicata
		public String readTextResource(String filepath)
			throws IOException
		{
			String content = "";
			BufferedReader resourceIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(root + filepath))));
			boolean firstLine = true;
			while(true)
			{
				String line = resourceIn.readLine();
				if(line == null)
					break;
				if(firstLine)
				{
					content += line;
					firstLine = false;
				}
				else
					content += "\n" + line;
			}
			return content;
		}
	}
}
