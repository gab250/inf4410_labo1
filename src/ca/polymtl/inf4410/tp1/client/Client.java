package ca.polymtl.inf4410.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Set;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Client 
{
	public static void main(String[] args) throws RemoteException 
	{
		String command = null;

		if (args.length > 0) 
		{
			command = args[0];
		}
		else
		{
			System.err.println("Error invalid arguments");
			System.exit(-1);
		}

		Client client = new Client();
		
		if(command.equals("create"))
		{
			String fileName = null;
			
			if (args.length >= 2)
			{	
				fileName = args[1];
				int errorCode = client.create(fileName);
				
				if(errorCode == 0)
				{
					System.out.println(fileName + " ajoute.");
				}
				else
				{
					System.out.println("File already exits");
				}
				
			}
			else
			{
				System.err.println("Error invalid arguments");
				System.exit(-1);
			}
			
			
		}
		else if(command.equals("list"))
		{
			Set<String> list;
			list = client.list();
			
			System.out.println(list.toString());
		}
	}

	private ServerInterface serverStub = null;

	public Client() 
	{
		super();

		if (System.getSecurityManager() == null) 
		{
			System.setSecurityManager(new SecurityManager());
		}

		//load server
		serverStub = loadServerStub("127.0.0.1");
	}

	private ServerInterface loadServerStub(String hostname) 
	{
		ServerInterface stub = null;

		try 
		{
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} 
		catch (NotBoundException e) 
		{
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} 
		catch (AccessException e) 
		{
			System.out.println("Erreur: " + e.getMessage());
		} 
		catch (RemoteException e) 
		{
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	
	}
	
	private int create(String nom)
	{
		int returnValue=-2;
		
		if(serverStub!=null)
		{
			try
			{
				returnValue = serverStub.create(nom);
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur: " + e.getMessage());
			}
		}
		
		return returnValue;
	}
	
	private Set<String> list() 
	{
		Set<String> list=null;
		
		if(serverStub!=null)
		{
			try
			{
				list = serverStub.list();
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur: " + e.getMessage());
			}
		}
		
		return list;
	}
	
	/*
	private void run() throws RemoteException 
	{
		appelNormal();

		if (localServerStub != null) {
			appelRMILocal();
		}

		if (distantServerStub != null) {
			appelRMIDistant();
		}
	}

	

	private void fill(byte[] a) 
	{
		Random r = new Random();
		
		r.nextBytes(a);
	}
	
	private void appelNormal() throws RemoteException 
	{
		
		byte[] a = new byte[size];
		fill(a);
		long start = System.nanoTime();
		
		localServer.execute(a);
		long end = System.nanoTime();

		System.out.println("Temps écoulé appel normal: " + (end - start)
				+ " ns");
	}

	private void appelRMILocal() throws RemoteException 
	{
		try 
		{
			byte[] a = new byte[size];
			fill(a);
			long start = System.nanoTime();
			
			localServerStub.execute(a);
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI local: " + (end - start)
					+ " ns");
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private void appelRMIDistant() throws RemoteException 
	{
		try {
			byte[] a = new byte[size];
			fill(a);
			long start = System.nanoTime();
			
			distantServerStub.execute(a);
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI distant: "
					+ (end - start) + " ns");
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}
	*/
}
