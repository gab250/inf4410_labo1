package ca.polymtl.inf4410.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


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
	public Set<String> list() throws RemoteException 
	{
		return files.keySet();
	}
	
	@Override
	public RemoteFile sync(String nom, int sommeDeControle) throws RemoteException 
	{
		return new RemoteFile(0,new byte[4]);
	}
	
	@Override
	public int push(String nom, byte[] contenu, int sommeDeControle) throws RemoteException 
	{
		return 0;
	}
		
}
