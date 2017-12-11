package com.vboiko.cluster_dispatcher_server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Valeriy Boiko
 *
 * @version 1.1
 *
 * The main class that starts the Server program
 * and represents the logic of reading commands from Dispatcher.
 *
 * Main class: {@link Server}
 *
 */

public class Server {

	private ServerSocket	serverSocket;
	private Socket			server;
	private Runtime			runtime;
	private boolean			connected;
	InputStream				serverInputStream;
	OutputStream			serverOutputStream;

	public Server() throws IOException {

		this.serverSocket = new ServerSocket(8090);
		this.server = this.serverSocket.accept();
		serverInputStream = this.server.getInputStream();
		serverOutputStream = this.server.getOutputStream();
		this.runtime = Runtime.getRuntime();
	}

	private void 		serverRuntime() throws IOException {

		while (true) {

			System.out.println("Someone connected...");

			DataInputStream		in = new DataInputStream(this.serverInputStream);
			DataOutputStream 	out = new DataOutputStream(this.serverOutputStream);

			while (true) {

				try
				{
					Process			process;
					String			command = in.readUTF();
					StringBuilder	response = new StringBuilder("");
					if (command.equals("disconnect")) {

						System.out.println("Client disconnected...");
						this.server.close();
						in.close();
						out.close();
						this.serverSocket.close();
						break;
					}
					try {
						process = this.runtime.exec(command);
						process.waitFor();
					}
					catch (InterruptedException e) {

						System.out.println("Process has been interrupted... ");
						continue;
					}
					Scanner		processScanner = new Scanner(process.getInputStream());
					while (processScanner.hasNext())
						response.append(processScanner.nextLine()).append('\n');
					out.writeUTF(response.toString());
				}
				catch (IOException e) {

					continue;
				}
			}
		}
	}

	public boolean		heartbeat() throws IOException, InterruptedException {

		this.serverOutputStream.write(new byte[]{4, 2});
		Thread.sleep(1000);
		byte[]	response = new byte[2];
		if (this.serverInputStream.read(response) == 2) {
			if (response[0] == 4 && response[1] == 2) {

				System.out.println("client is alive");
				return (true);
			}
		}
		return (false);
	}

	public static void 	main(String[] args) throws IOException {

		Scanner				scanner = new Scanner(System.in);

		new Thread(() -> {

			while (true) {

				if (scanner.hasNext())
					if (scanner.nextLine().equals("exit"))
						System.exit(0);
			}
		}).start();
		Server	server = new Server();
		server.serverRuntime();
		// TODO: 12/11/17 Do heartbeat
	}
}