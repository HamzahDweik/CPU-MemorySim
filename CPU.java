import java.io.*;
import java.util.Stack;
import java.util.Scanner;
import java.lang.Runtime;
import java.lang.System;

public class CPU {

    // initializes registers
    public static int pc = 0, sp = 1000, ir = 0, ac = 0, x = 0, y = 0, timer = 0;
    public boolean userMode = true, keepRunning = true;

    // initializes data streams
    PrintWriter memoryWriter = null;
    Scanner memoryReader = null;
    Process memoryChild = null;

    // initializes memory data stream
    private void init(String filename){
        try {
            Runtime rt = Runtime.getRuntime();
            this.memoryChild = rt.exec("java Memory " + filename);
            this.memoryWriter = new PrintWriter(memoryChild.getOutputStream());
            this.memoryReader = new Scanner(memoryChild.getInputStream());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // checks memory access permissions before read or write
    private void memoryCheck(int address){
        if(this.userMode && address >= 1000 && address < 2000){
            System.err.println("Memory violation: accessing system address " + address + " in user mode");
            System.exit(1);
        } else if(this.userMode && (address < 0 || address >= 2000)){
            System.err.println("Memory out-of-bounds!");
            System.exit(1);
        }
    }

    // reads the next instruction
    private int read(){
        return this.read(++this.pc);
    }

    // reads the instruction at the given address
    private int read(int address){
        memoryCheck(address);
        this.memoryWriter.printf("0 " + address + "\n");
        this.memoryWriter.flush();
        return Integer.parseInt(this.memoryReader.nextLine());
    }

    // writes the data to the given address
    private void write(int address, int data){
        memoryCheck(address);
        this.memoryWriter.printf("1 " + address + " " + data + "\n");
        this.memoryWriter.flush();
    }

    // initializes interrupt handler
    private void interruptInit(){

        // update timer and mdoe
        this.timer = 0;
        this.userMode = false;

        // save the current state
        this.write(1999, pc);
        this.write(1998, sp);

        // update current pointers
        this.sp = 1998;
        this.pc = 1000;
    }

    private void instructionProcessing(){

        // reads instruction from memory
        this.ir = read(this.pc);

        // processes instruction
        switch(this.ir){
            // load value into AC: load value
            case 1:
                this.ac = read();
                break;
            // load the value at the address into the AC
            case 2:
                this.ir = read();
                this.ac = read(this.ir);
                break;
            // load the value at the address found at the given address
            case 3:
                this.ir = read();
                this.ir = read(this.ir);
                this.ac = read(this.ir);
                break;
            // load the value at the address + X into the AC
            case 4:
                this.ir = read() + this.x;
                this.ac = read(this.ir);
                break;
            // load the value at the address + Y into the AC
            case 5:
                this.ir = read() + this.y;
                this.ac = read(this.ir);
                break;
            // load from (Sp + X) into the AC
            case 6:
                this.ir = this.sp + this.x;
                this.ac = read(ir);
                break;
            // store the value in the ac into the address
            case 7:
                this.ir = read();
                this.write(this.ir, this.ac);
                break;
            // get random int from 1 to 100 into the AC
            case 8:
                this.ac = (int)Math.floor(Math.random() * 100 + 1);
                break;
            // Port = 1, print ac as int, port = 2, print ac as char
            case 9:
                this.ir = read();
                if(this.ir == 1) System.out.print(this.ac);
                else if(this.ir == 2) System.out.print((char)this.ac);
                else System.out.println("Port Number Invalid.");
                break;
            // add the value of x to the ac
            case 10:
                this.ac += this.x;
                break;
            // add the value of y to the ac
            case 11:
                this.ac += this.y;
                break;
            // subtract the value of x from the ac
            case 12:
                this.ac -= this.x;
                break;
            // subtract the value of x from the ac
            case 13:
                this.ac -= this.y;
                break;
            // copy the value of the ac into x
            case 14:
                this.x = this.ac;
                break;
            // copy the value of x into the ac
            case 15:
                this.ac = this.x;
                break;
            // copy the value of the ac into y
            case 16:
                this.y = this.ac;
                break;
            // copy the value of y into the ac
            case 17:
                this.ac = this.y;
                break;
            // copy the value of the ac into sp
            case 18:
                this.sp = this.ac;
                break;
            // copy the value of sp into ac
            case 19:
                this.ac = this.sp;
                break;
            // jump to the address
            case 20:
                this.ir = read();
                this.pc = this.ir - 1;
                break;
            // jump to the address if ac = 0
            case 21:
                this.ir = read();
                if(this.ac == 0){
                    this.pc = this.ir - 1;
                }
                break;
            // jump to the address if ac != 0
            case 22:
                this.ir = read();
                if(this.ac != 0){
                    this.pc = this.ir - 1;
                }
                break;
            // push return address onto stack, jump to the address
            case 23:
                this.write(--this.sp, this.pc+2);
                this.ir = read();
                this.pc = this.ir - 1;
                break;
            // pop return address from the stack, jump to address
            case 24:
                this.pc = this.read(this.sp++) - 1;
                break;
            // increment the value in x
            case 25:
                this.x += 1;
                break;
            // decrement the value in x
            case 26:
                this.x -= 1;
                break;
            // push ac onto stack
            case 27:
                this.write(--this.sp, this.ac);
                break;
            // pop from stack onto ac
            case 28:
                this.ac = this.read(this.sp++);
                break;
            // perform system call
            case 29:
                // save the current state
                if(this.userMode) {
                    this.userMode = false;
                    this.write(1999, pc + 1);
                    this.write(1998, sp);

                    // update current pointers
                    this.sp = 1998;
                    this.pc = 1499;
                }
                break;
            // return from system call
            case 30:
                // restore save state
                this.pc = this.read(1999) - 1;
                this.sp = this.read(1998);
                this.userMode = true;
                break;
            // end execution
            case 50:
                this.memoryWriter.printf("shutdown");
                this.memoryWriter.flush();
                this.keepRunning = false;
                break;
            default:
                System.err.println("Instruction not found.");
                System.exit(1);
        }

        pc += 1;
    }

    // runs the CPU
    public void run(String filename, int interruptInterval){

        // initializes memory communication channel
        this.init(filename);

        // begins clock cycles
        while(this.keepRunning){
            if(this.timer >= interruptInterval && this.userMode)
                this.interruptInit();
            instructionProcessing();
            this.timer += 1;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        CPU cpu = new CPU();
        cpu.run(args[0], Integer.parseInt(args[1]));
    }

}
