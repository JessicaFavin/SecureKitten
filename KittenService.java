import java.net.*;
import java.io.*;
import java.util.*;

public class KittenService extends Thread {

    public static final int 					MAX_USERS   = 3;
    public static       int 					nbUsers     = 0;
    public static       ArrayList<PrintStream> 	outputs     = new ArrayList<PrintStream>();
	public static       ArrayList<String>		loggedUsers	= new ArrayList<String>();
    public static       HashMap<String,String>	usersList	= new HashMap<String,String>();

    private Socket      socket;
    private Scanner     userInput;
    private PrintStream userOutput;
    private String      username;

    public KittenService(Socket socket) {
        this.socket = socket;
		this.start();



    }

    @Override
    public void run() {
		//init
		boolean auth = logIn();

		//if init true
		if(auth) {
			String input;

	        this.userOutput.println("You are connected. "+nbUsers+" user(s) connected.");
			listCommands();
			broadcastInfo(username+" joined the chat");

	        while(!(input = this.userInput.nextLine().trim()).equals("/quit")) {
				if(input.startsWith("/")){
					command(input);
				} else {
					this.broadcastMessage(input);
				}
	        }

	        logOut();
	        broadcastInfo(username+" left the chat");
		}


        disconnect();
    }


	private void disconnect() {
		try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    private boolean logIn() {
		try {
            this.userInput = new Scanner(socket.getInputStream());
            this.userOutput = new PrintStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(this.nbUsers < MAX_USERS){
            boolean correctLogIn = this.authenticate();
			if(correctLogIn) {
				outputs.add(this.userOutput);
		        loggedUsers.add(this.username);
				nbUsers++;
				return true;
			} else {
				return false;
			}
        } else {
            this.userOutput.println("This chat is full. Come back later");
			return false;
        }

    }

    private boolean authenticate() {
		boolean alreadyLogged 	= false;
        int 	correctPassword = 3;
		String 	password;
        do {
            this.userOutput.println("Username : ");
            this.username   = this.userInput.nextLine();
            alreadyLogged  	= loggedUsers.contains(this.username);
			if(alreadyLogged) {
				this.userOutput.println("User already logged.");
			}
        } while(alreadyLogged);

        this.userOutput.println("Password : ");
        password = this.userInput.nextLine();

		if(!usersList.containsKey(this.username)) {
			// if not in usersList add password
			usersList.put(this.username, password);
			return true;
		} else {
			//else give 3 tries
			do {
	             if (usersList.get(this.username).equals(password)) {
					 return true;
				 } else {
					 this.userOutput.println("Wrong password.");
					 this.userOutput.println("Password : ");
					 this.userOutput.flush();
			         password = this.userInput.nextLine();
					 correctPassword--;
				 }
	        } while(correctPassword>0);
			this.userOutput.println("Too many wrong attempts.");
		}
		return false;
    }

    private synchronized void logOut() {
		this.userOutput.println("/quitAck");
		outputs.remove(this.userOutput);
		loggedUsers.remove(this.username);
		nbUsers--;
    }

    private synchronized void broadcastMessage(String message) {
		PrintStream output;
		for(int i=0; i<nbUsers; i++) {
			output =  outputs.get(i);
            if(output != this.userOutput) {
                output.println(this.username+" : "+message);
            }
            // output.println("> ");
			// output.flush();
        }
    }

    private synchronized void broadcastInfo(String info) {
		PrintStream output;
		for(int i=0; i<nbUsers; i++) {
			output =  outputs.get(i);
            if(output != this.userOutput) {
                output.println("["+info+"]");
				// output.println("> ");
				output.flush();
            }
        }
    }

	private  void info(String info) {
			this.userOutput.println("["+info+"]");
			// this.userOutput.println("\n> ");
			// this.userOutput.flush();
	}

	private void listCommands() {
		this.userOutput.println(" /? — display help\n"
		+" /help — display help\n"
		+" /quit — disconnect. \n"
		+" /list — list users. \n"
		+" /sendMsg <username> <message> — send msg to username.\n"
		+" /sendFile <username> <filename> — send file to username.");
		// +"> ");
	}


	private void command(String input) {
		String[] cmd = input.split(" ");
		switch(cmd[0]) {
			case "/?":
			case "/help":
				listCommands(); break;
			case "/list":
				listUsers(); break;
			case "/sendMsg":
				unicastMessage(input); break;
			case "/sendFile":
				unicastFile(input); break;
			case "/fileData":
				unicastFiledata(input); break;
			default:
				this.userOutput.println("Command not supported yet.");
				break;

		}
		// this.userOutput.println("> ");
		// this.userOutput.flush();
	}

	private void listUsers() {
		String result = "";
		for(String username : loggedUsers) {
			result += username+" ";
		}
		this.userOutput.println(result);
	}

	private void unicastMessage(String input) {
		String[] cmd = input.split(" ");
		if(cmd.length >= 3) {
			String message = String.join(" ", Arrays.copyOfRange(cmd, 2, cmd.length));
			int id = loggedUsers.indexOf(cmd[1]);
			if(id>-1){
				outputs.get(id).println(this.username+" : "+message+"\n> ");
				// outputs.get(id).flush();
			} else {
				this.info("No "+cmd[1]+" user logged.");
			}
		} else {
			this.info("Use : /sendMsg <username> <message> — Not enough arguments");
		}
	}

	private void unicastFile(String input) {
		// this.userOutput.println("Command not supported yet.");

		String[] cmd = input.split(" ");
		if(cmd.length >= 3) {
			String message = String.join(" ", Arrays.copyOfRange(cmd, 2, cmd.length));
			int id = loggedUsers.indexOf(cmd[1]);
			if(id>-1){
				this.userOutput.println("/sendFileAck");
				outputs.get(id).println(input);
			} else {
				this.info("No "+cmd[1]+" user logged.");
			}
		} else {
			this.info("Use : /sendMsg <username> <message> — Not enough arguments");
		}
	}

	private void unicastFiledata(String input) {
		String[] cmd = input.split(" ");
		String data = String.join(" ", Arrays.copyOfRange(cmd, 2, cmd.length));
		int id = loggedUsers.indexOf(cmd[1]);
		if(id>-1){
			outputs.get(id).println(input);
			// outputs.get(id).flush();
		} else {
			this.info("No "+cmd[1]+" user logged.");
		}
	}
}
