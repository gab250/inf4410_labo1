package ca.polymtl.inf4410.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


import ca.polymtl.inf4410.tp1.shared.ServerInterface; 

public class Server implements ServerInterface {

	private Map<String,byte[]> files;
		
	public static void main(String[] args) throws Exception
	{
		Server server = new Server();
		server.run();
	}

	public Server() 
	{
		super();
		files = new HashMap<String, byte[]>();
	}

	private void run() throws Exception 
	{
		if (System.getSecurityManager() == null) 
		{
			System.setSecurityManager(new SecurityManager());
		}

		try 
		{
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} 
		catch (ConnectException e) 
		{
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} 
		catch (Exception e) 
		{
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	@Override
	public int execute(int a, int b) throws RemoteException 
	{
		return a + b;
	}

	
	@Override
	public int execute(byte[] arg) throws RemoteException 
	{
		return Arrays.hashCode(arg);
	}
	
	@Override
	public int create(String nom) throws RemoteException 
	{
		//Check if file exists
		if(files.containsKey(nom))
		{
			return -1;
		}
		else
		{
			//Create empty file
			files.put(nom,new byte[0]);
			return 0;
		}
	}
	
	@Override
	public List<String> list() throws RemoteException 
	{
		ArrayList<String> list = new ArrayList<String>(files.keySet());
		
		return list;
	}
	
	@Override
	public RemoteFile sync(String nom, long sommeDeControle) throws RemoteException 
	{
		//Create checksum classes
		Checksum serverChecksum = new CRC32();
		serverChecksum.update(files.get(nom), 0, files.get(nom).length);
		
		//Check if client is requesting file for first time
		if(sommeDeControle == -1)
		{
			RemoteFile file = new RemoteFile(serverChecksum.getValue(),files.get(nom));
			return file;
		}
				
		//Check if file is up to date
		if(serverChecksum.getValue() == sommeDeControle)
		{
			return null;
		}
		else
		{
			RemoteFile file = new RemoteFile(serverChecksum.getValue(),files.get(nom));
			return file;
		}
		
		
		
	}
	
	@Override
	public List<Long> push(String nom, byte[] contenu, long sommeDeControle) throws RemoteException 
	{
		//Create checksum classes
		Checksum serverChecksum = new CRC32();
		serverChecksum.update(files.get(nom), 0, files.get(nom).length);
		
		//Create the array list to return 
		ArrayList<Long> pair = new ArrayList<Long>();
		
		//Check if is up to date
		if(sommeDeControle == serverChecksum.getValue())
		{
			files.get(nom) = contenu;
			serverChecksum.update(files.get(nom), 0, files.get(nom).length);
			pair.add(0,0);
			pair.add(1,serverChecksum.getValue());
			return pair;
			
		}
		else
		{
			pair.add(0,-1);
			pair.add(1,serverChecksum.getValue());
			return pair;
		}
	}
		
}
