- Como compilar
  execute:
    cd tcp_socket_sd/tcp/src
          
      e depois 

    javac ex1/*.java

- Como executar
  execute: 
    java ex1/TCPServer

      e depois em outro terminal

    java ex1/TCPClient
  
- Bibliotecas usadas (descrever as não padrões)
    - java.net.*
    - java.nio.file.Files
    - java.nio.file.Path
    - java.nio.file.Paths
    - java.util.ArrayList
    - java.util.Arrays
    - java.util.List
    - java.io.*
    - java.math.BigInteger
    - java.security.MessageDigest
    - java.util.Scanner
    - javax.xml.crypto.Data

- Exemplo de uso

    - Como executar CONNECT

      execute: CONNECT <username>,<senha>

      Os usuários de teste existentes são:
        - jesse,123456
        - jhonatan,123mudar
        - rodrigo,sistemas operacionais

    - Como executar PWD

      execute: PWD

    - Como executar GETFILES

      execute: GETFILES
    
    - Como executar CHDIR

      execute: CHDIR <nome_do_diretorio>

    - Como executar GETDIRS

      execute: GETDIRS

    - Como executar EXIT

      execute: EXIT
      