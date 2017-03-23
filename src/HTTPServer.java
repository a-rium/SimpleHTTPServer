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
				catch(IOException ie)
				{
					ie.printStackTrace();
				}
			}
		}

		public void halt()
		{
			running = false;
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
			// @Temp testing the connection and printing a message
		}
		
		@Override
		public void run()
		{
			while(running)
			{
				try
				{
					String request = readRequest();
					System.out.println("Arrivata richiesta:\n" + request);
				}
				catch(IOException ie)
				{
					ie.printStackTrace();
				}
			}
		}

		private String readRequest()
			throws IOException
		{
			String request = "";
			String currentLine = "";
			do
			{
				currentLine = in.readLine();
				request += currentLine;	

			} while(!currentLine.equals(""));
			
			return request;
		}

		public void halt()
		{
			running = false;
		}

		public boolean isRunning()
		{
			return running;
		}
	}
}
