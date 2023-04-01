package tcp;

/**
 * TCPClient: Cliente para conexao TCP
 * Descricao: Envia uma informacao ao servidor e recebe confirmações ECHO
 * Ao enviar "PARAR", a conexão é finalizada.
 */

import java.net.*;
import java.io.*;
import java.util.Scanner;

import javax.xml.crypto.Data;

public class TCPClient {
	public static void main (String args[]) {
	    Socket clientSocket = null; // socket do cliente
            
            
            try{
                /* Endereço e porta do servidor */
                int serverPort = 6666;   
                InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
                
                /* conecta com o servidor */  
                clientSocket = new Socket(serverAddr, serverPort);  
                
                /* cria objetos de leitura e escrita */
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            
                /* protocolo de comunicação */
                Scanner reader = new Scanner(System.in); // ler mensagens via teclado
                String buffer = "";
                while (true) {
                    System.out.print("jhonatan$ ");
                    buffer = reader.nextLine(); // lê mensagem via teclado
                
                    out.writeUTF(buffer);      	// envia a mensagem para o servidor
		
                    if (buffer.equals("PARAR")) break;
                    
                    buffer = in.readUTF();      // aguarda resposta do servidor
                    System.out.println(buffer);
                } 

	    }catch (Exception ue){
            System.out.println("Socket:" + ue.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);;
            }
        }
     } //main
} //class

