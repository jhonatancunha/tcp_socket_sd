- Como compilar
  execute:
    cd tcp_socket_sd/tcp/src
          
      e depois 

    javac ex2/*.java

- Como executar
  execute: 
    java ex2/TCPServer

      e depois

    java ex2/TCPClient
  
- Bibliotecas usadas (descrever as não padrões)
    - java.net.*
    - java.nio.ByteBuffer
    - java.nio.ByteOrder
    - java.util.ArrayList
    - java.util.Arrays
    - java.util.List
    - java.io.*
    - java.util.logging.*
    - java.util.Scanner

- Exemplo de uso

    - Como executar ADDFILE

      execute: ADDFILE <nome_do_arquivo>

    - Como executar DELETE

      execute: DELETE <nome_do_arquivo>

    - Como executar GETFILELIST

      execute: GETFILELIST
    
    - Como executar GETFILE <nome_do_Arquivo>

      execute: GETFILE <nome_do_arquivo>