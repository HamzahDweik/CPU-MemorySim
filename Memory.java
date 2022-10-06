import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Memory {

    private int[] memory;

    public static void main(String[] args) {

        // initializes memory
        Memory mem = new Memory(args[0]);
	
	String[] address;
        Scanner sc = new Scanner(System.in);

        // listens for instructions
        while(sc.hasNextLine()) {

            address = sc.nextLine().split(" ", 3);

            // handles reads from the cpu
            if (Integer.parseInt(address[0]) == 0)
                System.out.println(mem.read(Integer.parseInt(address[1])));
		
            // handles writes from the cpu
            else if (Integer.parseInt(address[0]) == 1)
                mem.write(Integer.parseInt(address[1]), Integer.parseInt(address[2]));

            else break;
        }
    }
    Memory(String fileName){

        // initializes memory
        this.memory = new int[2000];

        // loads file into memory
        try {
            File inputFile = new File(fileName);
            Scanner reader = new Scanner(inputFile);
            int index = 0;
            while(reader.hasNextLine() && index < 2000){
                String inputString = reader.nextLine();
                if(inputString.equals("")) continue;
                String[] dataArray = inputString.split(" ", 2);
		String data = dataArray[0];
		if(data.equals("")) continue;

                if(data.charAt(0) == '.')
                    index = Integer.parseInt(data.substring(1));
                else{
                    this.memory[index] = Integer.parseInt(data);
                    index += 1;
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }

    public int read(int address){
        return this.memory[address];
    }

    public void write(int address, int data){
        this.memory[address] = data;
    }

}
