package src;

import java.util.ArrayList;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class HTTPServer
{
	protected ServerSocket server;
	protected AccepterThread accepter;
	protected ArrayList<ConnectionThread> connectionThreads;

	public HTTPServer(int port)
		throws IOException
	{
		server = new ServerSocket(port);
		accepter = new AccepterThread();
		connectionThreads = new ArrayList<ConnectionThread>();
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
		private PrintWriter out;
		private BufferedReader in;
		// private ObjectOutputStream out;
		// private ObjectInputStream in;

		private boolean running = true;

		public ConnectionThread(Socket socket)
			throws IOException
		{
			connection = socket;
			out = new PrintWriter(connection.getOutputStream());
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		}
		
		@Override
		public void run()
		{
			while(running)
			{
				try
				{
					HTTPMessage request = readRequest();
					HTTPMessage response = buildSimpleHTMLResponse();
					System.out.printf(response.toResponse());
					out.printf(response.toResponse());	
					out.flush();
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

		private HTTPMessage readRequest()
			throws IOException
		{
			String header = in.readLine();
			HTTPMessage request = new HTTPMessage(header);
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

		public HTTPMessage buildSimpleHTMLResponse()
		{
			HTTPMessage response = new HTTPMessage();
			response.setHTTPVersion("1.1");
			response.setStatus(200);
			response.add("Date", "Thur, 23 March 2017 23:59:59 GMT");
			response.add("Server", "Java SimpleHTTPServer");
			response.add("Connection", "closed");
			response.add("Content-type", "text/html");
			String html = "<html><head><title>~Welcome~</title></head><body><div>Hello visitor! What can I do for you?</div></body></html>";
			response.add("Content-length", "" + html.length());
			response.setData(html);

			return response;
		}	

		public void halt()
			throws IOException
		{
			running = false;
			out.close();
			in.close();
			connection.close();
		}

		public boolean isRunning()
		{
			return running;
		}
	}
}
