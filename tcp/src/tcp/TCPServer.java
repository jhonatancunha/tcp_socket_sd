package tcp;

/**
 * TCPServer: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao
 */
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class TCPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666; // porta do servidor

            
            
            /* cria um socket e mapeia a porta para aguardar conexao */
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Servidor aguardando conexao ...");

                /* aguarda conexoes */
                Socket clientSocket = listenSocket.accept();

                System.out.println("Cliente conectado ... Criando thread ...");

                /* cria um thread para atender a conexao */
                ClientThread c = new ClientThread(clientSocket);

                /* inicializa a thread */
                c.start();
            } //while

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } //catch
    } //main
} //class

/**
 * Classe ClientThread: Thread responsavel pela comunicacao
 * Descricao: Rebebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 */
class ClientThread extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    
    String currentPath;

    public ClientThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        } //catch
    } //construtor
    
    private void getOrCreateDir() {
    	File theDir = new File("./jhonatan");
    	if (!theDir.exists()){
    	    theDir.mkdirs();
    	}
    }
    
    private void pwdCommand() {
    	String pwd = "";    	
    	try {
	    	String[] commands = new String[] {"/bin/bash", "-c", "pwd"};
	    	Process proc = new ProcessBuilder(commands).start();
	    	
	    	// Read the output
	
	        BufferedReader reader =  
	              new BufferedReader(new InputStreamReader(proc.getInputStream()));
	

	        pwd = reader.readLine();
	       
	        proc.waitFor();
	    				
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	try {
			this.out.writeUTF(pwd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    
    private void lsCommand() {
    	String file = "";    	
    	try {
	    	String[] commands = new String[] {"/bin/bash", "-c", "find . -maxdepth 1 -type f"};
	    	Process proc = new ProcessBuilder(commands).start();
	    	
	    	
	    	// create a new ArrayList
	          List<String> fileList= new ArrayList<String>();
	    	
	    	// Read the output
	        BufferedReader reader =  
	              new BufferedReader(new InputStreamReader(proc.getInputStream()));
	       
	        while((file = reader.readLine()) != null) {
	        	fileList.add(file.substring(2, file.length()));
	        }

	        
	        Integer numberOfFiles = fileList.size();
	        this.out.writeUTF(numberOfFiles.toString());
	        
	        for (String fileItem : fileList) {
	        	System.out.println(fileItem);
 	        	this.out.writeUTF(fileItem);
	    	}

	       
	        proc.waitFor();
	    				
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void getDirs() {
    	String file = "";    	
    	try {
	    	String[] commands = new String[] {"/bin/bash", "-c", "ls -d */"};
	    	Process proc = new ProcessBuilder(commands).start();
	    	
	    	
	    	// create a new ArrayList
	          List<String> fileList= new ArrayList<String>();
	    	
	    	// Read the output
	        BufferedReader reader =  
	              new BufferedReader(new InputStreamReader(proc.getInputStream()));
	       
	        while((file = reader.readLine()) != null) {
	        	fileList.add(file);
	        }

	        
	        Integer numberOfFiles = fileList.size();
	        this.out.writeUTF(numberOfFiles.toString());
	        
	        for (String fileItem : fileList) {
	        	System.out.println(fileItem);
 	        	this.out.writeUTF(fileItem);
	    	}

	       
	        proc.waitFor();
	    				
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    }
    
    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
        	
        	this.getOrCreateDir();
        	
        	 
        	 
            String buffer = "";
            loop: while (true) {
                buffer = in.readUTF();   /* aguarda o envio de dados */

                
                switch(buffer) {
	                case "PWD":
	                	System.out.println("Comando pwd");
	                	this.pwdCommand();
	                	break;
	                case "GETFILES":
	                	this.lsCommand();
	                	break;
	                case "GETDIRS":
	                	this.getDirs();
	                	break;
	                case "EXIT":
	                	break loop;
	            	default:
	            		buffer = "Comando não encontrado";
	            		out.writeUTF(buffer);
	            		break;
                }
                
                
                
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        
        System.out.println("Thread comunicação cliente finalizada.");
    } //run
} //class