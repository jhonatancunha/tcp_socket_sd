package ex2;

/**
 * TCPClient: Cliente para conexao TCP
 * Descricao: Envia uma informacao ao servidor e recebe confirmações ECHO
 * Ao enviar "PARAR", a conexão é finalizada.
 */

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;
import java.util.Scanner;




public class TCPClient {
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static String user = "user";

	
	public static ByteBuffer createHeader(byte messageType, byte commandId, byte filenameSize, String filename) {
        ByteBuffer header = ByteBuffer.allocate(259); // tamanho do cabeçalho de solicitação
        header.order(ByteOrder.BIG_ENDIAN);

        header.put(0, messageType);
        header.put(1, commandId);
        header.put(2, filenameSize);
        header.position(3);
        header.put(filename.getBytes());
        
        return header;
	}

    public static void printResponseStatusCode(byte commandId, byte statusCode){
        switch (commandId){
            case 1: // ADDFILE
                if(statusCode == 2) System.out.println("Erro ao tentar criar arquivo :(");
                else System.out.println("Arquivo criado com sucesso :)");
                break;
            case 2: // DELETE
                if(statusCode == 2) System.out.println("Erro ao tentar deletar arquivo :(");
                else System.out.println("Arquivo deletar com sucesso :)");
                break;
            case 3: // GETFILESLIST
                break;
            case 4: // GETFILE
                break;
        }
    }
	
	public static void main (String args[]) {
	    Socket clientSocket = null; // socket do cliente
        Scanner reader = new Scanner(System.in);
            
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
                String keyboardBuffer = "";
                byte[] bytes = null;
                
                loop:while (true) {
                    System.out.print(ANSI_GREEN+user+"$ "+ANSI_RESET);
                    keyboardBuffer = reader.nextLine(); // lê mensagem via teclado
                    String[] argCommand = keyboardBuffer.split(" ");
                    
                    
                    String command = argCommand[0];
                    String filename = "";
                    byte messageType = 1;
                    byte commandId = 0;

                    switch(command){
                        case "PARAR":
                            break loop;
                        case "ADDFILE":
                            filename = argCommand[1];
                            commandId = 1;                            
                            break;
                        case "DELETE":
                            filename = argCommand[1];    
                            commandId = 2;
                            break;
                        case "GETFILELIST":
                            commandId = 3;
                            break;
                        case "GETFILE":
                            filename = argCommand[1];    
                            commandId = 4;
                            break;
                        default:
                            System.out.println("Comando desconhecido");
                            continue;
                    }

                    byte filenameSize = (byte) filename.length();
                    ByteBuffer buffer = createHeader(messageType, commandId, filenameSize, filename);

                    bytes = buffer.array();
                    int size = buffer.limit();
                    out.write(bytes, 0, size);      	// envia o header para o servidor
                    out.flush();
                    

                    // ***********************************
                    // Aguardando resposta do servidor
                    // ***********************************
                    in.read(bytes);    
                    
                    buffer = ByteBuffer.wrap(bytes);
                    buffer.order(ByteOrder.BIG_ENDIAN);
                    byte responseMessageType = buffer.get(0);
                    byte responseCommandId = buffer.get(1);
                    byte responseStatusCode = buffer.get(2);

                    
                    // ***********************************
                    // Aguardando resposta do conteudo dos arquivos
                    // ***********************************
                    int sizeOfContent = 0;
                    switch(command){
                        case "GETFILELIST":
                            in.read(bytes);
                            buffer = ByteBuffer.wrap(bytes);
                            buffer.order(ByteOrder.BIG_ENDIAN);
                            sizeOfContent = buffer.getInt();
                            break;
                        case "GETFILE":
                            in.read(bytes);
                            buffer = ByteBuffer.wrap(bytes);
                            buffer.order(ByteOrder.BIG_ENDIAN);
                            sizeOfContent = buffer.getInt();

                            System.out.println("sizeOfContent: "+sizeOfContent);
                            
                            bytes = new byte[1];
                            byte[] contentByte = new byte[sizeOfContent];
                            for(int i = 0; i < sizeOfContent; i++){
                                in.read(bytes);
                                byte b = bytes[0];
                                contentByte[i] = b;
                            }

                            String content = new String(contentByte);
                            System.out.println(content);
                            break;
                        default:
                            printResponseStatusCode(responseCommandId, responseStatusCode);
                            break;
                    }   

                    
                    
                    // boolean isNumber = buffer.matches("[0-9]+");
                    
                    // de acordo com a especificação da atividade alguns comandos retornam a quantidade de mensagens que irao enviar em seguida
                    // if(isNumber) {
                    // 	int amountOfMessages = Integer.parseInt(buffer);
                    // 	while(amountOfMessages > 0) {
                    // 		buffer = in.readUTF();
                    // 		System.out.println(buffer);
                    // 		amountOfMessages--;
                    // 	}
                    	
                    // }else {
                    // 	System.out.println(buffer);                    	
                    // }    
                } 

	    }catch (Exception ue){
            System.out.println("Socket:" + ue.getMessage());
        } 
        // finally {
        //     reader.close();
        //     try {
        //         clientSocket.close();
        //     } catch (IOException ioe) {
        //         System.out.println("IO: " + ioe);;
        //     }
        // }
     }
}

