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
import java.util.ArrayList;
import java.util.List;
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

    public static void printResponseStatusCode(byte commandId, byte statusCode) {
        switch (commandId) {
            case 1: // ADDFILE
                if (statusCode == 2)
                    System.out.println("Erro ao tentar criar arquivo :(");
                else
                    System.out.println("Arquivo criado com sucesso :)");
                break;
            case 2: // DELETE
                if (statusCode == 2)
                    System.out.println("Erro ao tentar deletar arquivo :(");
                else
                    System.out.println("Arquivo deletar com sucesso :)");
                break;
            case 3: // GETFILESLIST
                break;
            case 4: // GETFILE
                break;
        }
    }

    public static int saveFile(String filename, String content) throws IOException {
        String defaultPath = System.getProperty("user.dir") + "/src/ex2/downloads";
        File theDir = new File(defaultPath);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }

        String path = defaultPath + "/" + filename;
        File file = new File(path);

        if (file.createNewFile()) {
            FileWriter writer = new FileWriter(path, true);
            BufferedWriter buffer = new BufferedWriter(writer);
            buffer.write(content);
            buffer.flush();
            buffer.close();

            return 1;
        } else {
            return 0;
        }
    }

    public static void main(String args[]) {
        Socket clientSocket = null; // socket do cliente
        Scanner reader = new Scanner(System.in);

        try {
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

            loop: while (true) {
                System.out.print(ANSI_GREEN + user + "$ " + ANSI_RESET);
                keyboardBuffer = reader.nextLine(); // lê mensagem via teclado
                String[] argCommand = keyboardBuffer.split(" ");

                String command = argCommand[0];
                String filename = "";
                byte messageType = 1;
                byte commandId = 0;

                switch (command) {
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
                out.write(bytes, 0, size); // envia o header para o servidor
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
                switch (command) {
                    case "GETFILELIST":
                        bytes = new byte[1];
                        byte[] numberOfFilesInBytes = new byte[2];

                        for (int i = 0; i < 2; i++) {
                            in.read(bytes);
                            numberOfFilesInBytes[i] = bytes[0];
                        }

                        buffer = ByteBuffer.wrap(numberOfFilesInBytes);
                        buffer.order(ByteOrder.BIG_ENDIAN);
                        sizeOfContent = buffer.getShort();

                        // create a new ArrayList
                        List<String> fileList = new ArrayList<String>();

                        bytes = new byte[1];

                        for (int i = 0; i < sizeOfContent; i++) {
                            in.read(bytes);
                            byte filenameLength = bytes[0];
                            byte[] filenameNameInBytes = new byte[filenameLength];

                            for (int j = 0; j < filenameLength; j++) {
                                in.read(bytes);
                                filenameNameInBytes[j] = bytes[0];
                            }

                            String name = new String(filenameNameInBytes);
                            fileList.add(name);
                        }

                        System.out.printf("Quantidade de arquivos %d\n", fileList.size());

                        for (String name : fileList) {
                            System.out.println(name);
                        }

                        break;
                    case "GETFILE":
                        in.read(bytes);
                        buffer = ByteBuffer.wrap(bytes);
                        buffer.order(ByteOrder.BIG_ENDIAN);
                        sizeOfContent = buffer.getInt();

                        System.out.println("sizeOfContent: " + sizeOfContent);

                        bytes = new byte[1];
                        byte[] contentByte = new byte[sizeOfContent];
                        for (int i = 0; i < sizeOfContent; i++) {
                            in.read(bytes);
                            byte b = bytes[0];
                            contentByte[i] = b;
                        }

                        String content = new String(contentByte);
                        System.out.println(content);
                        saveFile(filename, content);

                        break;
                    default:
                        printResponseStatusCode(responseCommandId, responseStatusCode);
                        break;
                }

            }

        } catch (Exception ue) {
            System.out.println("Socket:" + ue.getMessage());
        }
    }
}
