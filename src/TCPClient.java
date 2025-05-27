// TCPClient.java

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner; // Para ler a entrada do teclado

public class TCPClient {
    public static void main(String[] args) {
        Socket s = null;
        Scanner scanner = new Scanner(System.in); // Para ler do console

        try {
            int serverPort = 7896;
            String serverAddress = "localhost"; // Ou o IP do seu servidor

            System.out.print("Digite seu nome de usuário: ");
            String username = scanner.nextLine();

            final Socket finalSocket = new Socket(serverAddress, serverPort);
            s = finalSocket;
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeUTF(username); // Envia o nome de usuário primeiro

            // Thread para RECEBER mensagens do servidor continuamente
            new Thread(() -> {
                try {
                    while (true) {
                        String data = in.readUTF();
                        System.out.println(data); // Imprime a mensagem recebida
                    }
                } catch (EOFException e) {
                    System.out.println("Servidor desconectou.");
                } catch (IOException e) {
                    System.out.println("Erro ao receber mensagem: " + e.getMessage());
                } finally {
                    try {
                        if(!finalSocket.isClosed()) {
                            finalSocket.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Erro ao fechar socket de leitura: " + e.getMessage());
                    }
                }
            }).start();

            // Loop para ENVIAR mensagens para o servidor
            System.out.println("Conectado ao chat. Digite 'sair' para desconectar.");
            System.out.println("Comandos: 'todos <mensagem>', 'amigo <nome_do_amigo> <mensagem>', 'sair'");

            while (true) {
                String commandLine = scanner.nextLine();
                String messageToSend = "";
                if(commandLine.equalsIgnoreCase("sair")) {
                    break;
                } else if(commandLine.toLowerCase().startsWith("amigo ")) {
                    String[] parts = commandLine.split(" ", 3);
                    if(parts.length >= 3) {
                        String friendName = parts[1];
                        String actualMessage = parts[2];
                        messageToSend = "AMIGO:" + friendName + ":" + actualMessage;
                    } else {
                        System.out.println("Uso: amigo <nome_do_amigo> <mensagem>");
                        continue;
                    }
                } else if(commandLine.toLowerCase().startsWith("todos")) {
                    String[] parts = commandLine.split(" ", 2);
                    if(parts.length >= 2) {
                        String actualMessage = parts[1];
                        messageToSend = "TODOS:" + actualMessage;
                    } else {
                        System.out.println("Uso: todos <mensagem>");
                        continue;
                    }
                } else {
                    System.out.println("Comando inválido. Tente 'todos <mensagem>', 'amigo <nome_do_amigo> <mensagem>', ou 'sair'.");
                    continue;
                }
                out.writeUTF(messageToSend);
            }
        } catch (UnknownHostException e) {
            System.out.println("Erro: Host desconhecido - " + e.getMessage());
        } catch (EOFException e) {
            System.out.println("Erro EOF: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
        } finally {
            if( s != null ) {
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar conexão: " + e.getMessage());
                }
            }
            scanner.close();
        }
    }
}