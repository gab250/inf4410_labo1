package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.List;
import java.io.Serializable;

public interface ServerInterface extends Remote 
{
	int execute(int a, int b) throws RemoteException;
	int execute(byte[] arg) throws RemoteException;
	int create(String nom) throws RemoteException;
	List<String> list() throws RemoteException;
	RemoteFile sync(String nom, int sommeDeControle) throws RemoteException;
	int push(String nom, byte[] contenu, int sommeDeControle) throws RemoteException;
	
	public class RemoteFile implements Serializable 
	{
		public int checksum_;
		public byte[] file_;
		
		public RemoteFile(int checksum, byte[] file)
		{
			checksum_ = checksum;
			file_ = file;
		}
	}
}


