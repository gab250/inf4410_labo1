package ca.polymtl.inf4410.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;
import ca.polymtl.inf4410.tp1.shared.ServerInterface.RemoteFile;

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

		//Check if metaData file exists 
		File checksumFile = new File("ChecksumMetaData.xml");
		
		try
		{
			if(!checksumFile.exists())
			{
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("MetaData");
				doc.appendChild(rootElement);
				
				//write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(checksumFile);
				
				transformer.transform(source, result);
				
			}
		}
		catch (ParserConfigurationException pce) 
		{
			pce.printStackTrace();
		} 
		catch (TransformerException tfe) 
		{
			tfe.printStackTrace();
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
					System.out.println(fileName + " ajoute");
				}
				else
				{
					System.out.println("Le fichier existe deja");
				}
				
			}
			else
			{
				System.err.println("Erreur arguments invalides");
				System.exit(-1);
			}
		}
		else if(command.equals("list"))
		{
			List<String> list;
			list = client.list();
			
			for(int i=0; i<list.size(); ++i)
			{
				System.out.println(list.get(i));
			}
			
			System.out.println(Integer.toString(list.size()) + " fichiers(s)");
		}
		else if(command.equals("sync"))
		{
			String fileName = null;
			
			if (args.length >= 2)
			{	
				fileName = args[1];	
				
				File file = new File(fileName);
				
				try
				{
					if(file.exists())
					{
						//Sync
						long checkSum;
						
						//Read XML
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document doc = dBuilder.parse(checksumFile);
						
						NodeList nList = doc.getElementsByTagName(fileName);
						Node nNode = nList.item(0);
						
						Element eElement = (Element) nNode;
						checkSum = Long.valueOf(eElement.getAttribute("CRC32"));
												
						RemoteFile remoteFile = client.sync(fileName,checkSum);
						
						if(remoteFile != null)
						{
							//Update file and metaData
							try
							{
								//Write data
								FileWriter fw = new FileWriter(file.getAbsoluteFile());
								BufferedWriter bw = new BufferedWriter(fw);
								bw.write(new String(remoteFile.file_));
								bw.close();
								
								//Write new checksum
								eElement.setAttribute("CRC32", Long.toString(remoteFile.checksum_));
								
								TransformerFactory transformerFactory = TransformerFactory.newInstance();
								Transformer transformer = transformerFactory.newTransformer();
								DOMSource source = new DOMSource(doc);
								StreamResult result = new StreamResult(checksumFile);
								
								transformer.transform(source, result);
								
							}
							catch(IOException e)
							{
								System.out.println("Erreur : " + e.getMessage());
							}
						   	catch (TransformerException tfe) 
						    {
								tfe.printStackTrace();
						    }
							
							System.out.println(fileName + " synchronise");
							System.out.println("Somme de controle : " + Long.toString(remoteFile.checksum_));
						}
						else
						{
							System.out.println(fileName + " deja synchronise");
						}
					}
					else
					{
						RemoteFile remoteFile = client.sync(fileName,-1);
						
						if(remoteFile != null)
						{
							//Create file
							file.createNewFile();
							
							//Write data
							FileWriter fw = new FileWriter(file.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(new String(remoteFile.file_));
							bw.close();
							
							//Write
							DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
							Document doc = docBuilder.parse(checksumFile);
							
							doc.getDocumentElement().normalize();
							Element root = doc.getDocumentElement();
							
							Element fileTag = doc.createElement(fileName);
							root.appendChild(fileTag);
							
							Attr attr = doc.createAttribute("CRC32");
							attr.setValue(Long.toString(remoteFile.checksum_));
							fileTag.setAttributeNode(attr);
														
							// write the content into xml file
							TransformerFactory transformerFactory = TransformerFactory.newInstance();
							Transformer transformer = transformerFactory.newTransformer();
							DOMSource source = new DOMSource(doc);
							StreamResult result = new StreamResult(checksumFile);
				
							transformer.transform(source, result);
							
							System.out.println(fileName + " synchronise");
							System.out.println("Somme de controle : " + Long.toString(remoteFile.checksum_));
							
						}
				     }
				}
				catch (Exception e) 
				{
					e.printStackTrace();
			    }
			}
		}
		else if(command.equals("push"))
		{
			String fileName = null;
			
			if (args.length >= 2)
			{	
				fileName = args[1];	
				
				File file = new File(fileName);
				
				try
				{
					if(file.exists())
					{
						//push
						long checkSum;
						
						//Read XML
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document doc = dBuilder.parse(checksumFile);
						
						NodeList nList = doc.getElementsByTagName(fileName);
						Node nNode = nList.item(0);
						
						Element eElement = (Element) nNode;
						checkSum = Long.valueOf(eElement.getAttribute("CRC32"));
					
						List<Long> pair = push(fileName,	

						
						if(pair.get(0) == 0 )
						{
							//Update checksum number
							try
							{
								//Write new checksum
								eElement.setAttribute("CRC32", Long.toString(pair.get(1)));
								
								TransformerFactory transformerFactory = TransformerFactory.newInstance();
								Transformer transformer = transformerFactory.newTransformer();
								DOMSource source = new DOMSource(doc);
								StreamResult result = new StreamResult(checksumFile);
								
								transformer.transform(source, result);
								
							}
							catch(IOException e)
							{
								System.out.println("Erreur : " + e.getMessage());
							}
						   	catch (TransformerException tfe) 
						    {
								tfe.printStackTrace();
						    }
							
							System.out.println(fileName + " a ete envoye au serveur");
							System.out.println("Somme de controle : " + Long.toString(Long.toString(pair.get(1))));
						}
						else
						{
							System.out.println(fileName + " n'est pas a jour");
							System.out.println("Somme de controle client:" + checkSum );
							System.out.println("Somme de controle serveur:" + Long.toString(pair.get(1)));
						}
					}
					else
					{
						System.out.println("Erreur de push:" + fileName + " n'existe pas");
				    }
				}
				catch (Exception e) 
				{
					e.printStackTrace();
			    }
			}
		else
		{
			System.err.println("Erreur arguments invalides");
			System.exit(-1);
		}
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
				System.out.println("Erreur list : " + e.getMessage());
			}
		}
		
		return returnValue;
	}
	
	private List<String> list() 
	{
		List<String> list=null;
		
		if(serverStub!=null)
		{
			try
			{
				list = serverStub.list();
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur list: " + e.getMessage());
			}
		}
		return list;
	}

	private RemoteFile sync(String nom, long sommeDeControle)
	{
		RemoteFile remoteFile=null;
		
		if(serverStub!=null)
		{
			try
			{
				remoteFile = serverStub.sync(nom,sommeDeControle);
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur sync: " + e.getMessage());
			}
		}
		
		return remoteFile;
	}
	
	private List<Long> push(String nom, byte[] contenu, long sommeDeControle)
	{
		if(serverStub!=null)
		{
			try
			{
				ArrayList<Long> pair = new ArrayList<long>(serverStub.push(nom, contenu,sommeDeControle));
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur push: " + e.getMessage());
			}
		}
		
		return pair;
	}
}
