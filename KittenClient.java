import java.net.*;
import java.io.*;
import java.util.*;


public class KittenClient extends Thread {

	private Scanner 	networkIn;
	private PrintStream networkOut;
	private Scanner 	consoleIn;
	private PrintStream consoleOut;
	private Socket 		socket;
	private String 		ip 				= "127.0.0.1";
	private int 		port 			= 1234;
	private boolean 	quit 			= false;
	private String		sendFileCommand = "";

	public KittenClient() {
		try {
			socket = new Socket( ip, port );

			networkIn 	= new Scanner( socket.getInputStream() );
			networkOut 	= new PrintStream( socket.getOutputStream() );

			consoleIn 	= new Scanner( System.in );
			consoleOut 	= System.out;

			this.start();
			this.readNetwork();

			this.socket.close();
		} catch( java.util.NoSuchElementException nsee ) {
			System.out.println("Server broke.");
			System.exit(2);
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.readKeyboard();
	}

	private void readNetwork() {
		String input;
		boolean display = true;
		while(!quit) {
			input = this.networkIn.nextLine();
			if(input.equals("/quitAck")){
				break;
			}

			if(input.startsWith("/")) {
				String[] cmd = input.split(" ");
				switch (cmd[0] ) {
					case "/sendFileAck":
						this.sendFile(this.sendFileCommand);
						display = false;
						break;
					case "/sendFile":
						this.receiveFile(input);
						display = false;
						break;
					case "/fileData":
						this.receiveFileData(input);
						display = false;
						break;
				}
			}

			if(!input.startsWith(">"))
				input+="\n";
			if(display)
				this.consoleOut.print(input);
			display = true;
		}
	}

	private void readKeyboard() {
		String input;
		while(!quit) {
			input = this.consoleIn.nextLine();
			this.networkOut.println(input);
			if(input.startsWith("/")) {
				String[] cmd = input.split(" ");
				switch (cmd[0] ) {
					case "/quit":
						quit = true; break;
					case "/sendFile":
						this.sendFileCommand = input; break;
						//this.sendFile(input); break;
				}
			}
		}
	}

	private void sendFile(String input) {
		String[] cmd = input.split(" ");
		byte[] buffer = new byte[8];
		if(cmd.length >= 3) {
			try {
				FileInputStream fis = new FileInputStream(cmd[2]);
				if(cmd.length >= 3) {
					while(fis.read(buffer)!=-1) {
						// fileData user data
						this.networkOut.println("/fileData "+cmd[1]+" "+cmd[2]+" "+new String(buffer));

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void receiveFile(String input) {
		String[] cmd = input.split(" ");
		// empty dest file
		boolean append = false;
		if(cmd.length >= 3) {
			try {
				FileOutputStream fos = new FileOutputStream(cmd[2],append);
				fos.close();
			} catch (Exception e) {
			   e.printStackTrace();
		   }
		}
	}

	private void receiveFileData(String input) {
		String[] cmd = input.split(" ");
		boolean append = true;
		if(cmd.length >= 4) {
			try {
				FileOutputStream fos = new FileOutputStream(cmd[2],append);
				String data = String.join(" ", Arrays.copyOfRange(cmd, 3, cmd.length));
				fos.write(data.getBytes());
				fos.close();
			} catch (Exception e) {
			   e.printStackTrace();
		   }
		}
	}

	public static void main(String[] args) {
		new KittenClient();
	}
}
