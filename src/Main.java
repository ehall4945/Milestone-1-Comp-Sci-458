//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.*;

public class Main {

    // Map for storing register names to their corresponding numbers
    private static final Map<String, Integer> registerMap = new HashMap<>();

    // Map for storing instruction names along with their opcodes and function codes
    private static final Map<String, InstructionFormat> instructionMap = new HashMap<>();

    static {
        // Initializes the register mappings; includes everything
        String [] registers = {
                "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
                "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"
        };

        // For loop, iterates through array and maps
        // the register names to their corresponding numbers
        for (int i = 0; i < registers.length; i++) {
            // Map register name to its number
            registerMap.put("$" + registers[i], i);

            // Makes sure to allow direct access
            registerMap.put("$" + i, i);
        }

        // Sets instruction mappings for R, I, and J-type instructions
        // Start with R-type: add, sub, and, or, slt
        instructionMap.put("add", new InstructionFormat("000000", "100000", "R"));
        instructionMap.put("sub", new InstructionFormat("000000", "100010", "R"));
        instructionMap.put("and", new InstructionFormat("000000", "100100", "R"));
        instructionMap.put("or", new InstructionFormat("000000", "100101", "R"));
        instructionMap.put("slt", new InstructionFormat("000000", "101010", "R"));

        // Then move onto I-type: addi, addiu, andi, ori, lui, lw, sw, beq, bne
        instructionMap.put("addi", new InstructionFormat("001000", null, "I"));
        instructionMap.put("addiu", new InstructionFormat("001001", null, "I"));
        instructionMap.put("andi", new InstructionFormat("001100", null, "I"));
        instructionMap.put("ori", new InstructionFormat("001101", null, "I"));
        instructionMap.put("lui", new InstructionFormat("001111", null, "I"));
        instructionMap.put("lw", new InstructionFormat("100011", null, "I"));
        instructionMap.put("sw", new InstructionFormat("101011", null, "I"));
        instructionMap.put("beq", new InstructionFormat("000100", null, "I"));
        instructionMap.put("bne", new InstructionFormat("000101", null, "I"));

        // Then move onto J-type: j
        instructionMap.put("j", new InstructionFormat("000010", null, "J"));
        // Finally, do R-type: syscall
        instructionMap.put("syscall", new InstructionFormat("000000", "001100", "R"));

    }

    public static void main(String[] args) {
        // Helps ensure that only one command line argument is provided, throws error if not
        if (args.length != 1) {
            System.err.println("Usage: java -jar PROG1_Milestone1.jar \"<MIPS instruction>\"");
        }

        // Trim any extra spaces that were in the input instruction and store the new String
        String instructionLine = args[0].trim();

        // After, convert the MIPS instruction into machine code using the assemble() method
        String machineCode = assemble(instructionLine);

        // Finally, print the result (machineCode)
        System.out.println(machineCode);
    }

    private static String assemble(String instructionLine) {
        // Splits the instruction string into its name and its operands from the first whitespace
        // Stores the result as an element in parts array
        String[] parts = instructionLine.split("\\s+", 2);
        // Then get the instruction's name
        String instructName = parts[0];
        // Get the operands if they are present
        String operands = parts.length > 1 ? parts[1] : "";

        // Gets the instruction format from instructionMap,
        // stores the result in a new InstructionFormat object
        InstructionFormat format = instructionMap.get(instructName);

        // If the provided instruction in format is not valid i.e. null, throw an error
        if (format == null) {
            throw new IllegalArgumentException("Unsupported instruction : " + instructName);
        }

        // Makes sure to route the assembler function based off of the instruction type in format
        // Does this using switch cases, since it's more efficient than multiple if statements
        switch (format.type) {
            // First case: R-type
            case "R":
                    // Calls assembleRType method and returns its output
                    return assembleRType(instructName, operands, format);
            // Second case: I-type
            case "I":
                // Calls assembleIType method and returns its output
                return assembleIType(operands, format);
            case "J":
                // Calls assembleJType method and returns its output
                return assembleJType(operands, format);
            default:
                // Default catch-all, throw an IllegalStateException
                throw new IllegalStateException("Unknown instruction format type");
        }
    }

    /*
     *
     * The following three methods are for assembling & getting the
     * Binary + Hexadecimal values for R-type, I-type, and J-type
     * They all basically do the same exact thing
     *
    */
    private static String assembleRType(String instructName, String operands, InstructionFormat format) {
        // Starts by removing all spaces and splits the operands by their commas
        // Then stores the results as an element in regs String array
        String[] regs = operands.replaceAll("\\s", "").split(",");

        // Gets the destination (rd), source (rs), and target (rt) registers
        // Defaults to zero if they're missing
        int rd = registerMap.getOrDefault(regs[0], 0);
        int rs = regs.length > 1 ? registerMap.getOrDefault(regs[1], 0) : 0;
        int rt = regs.length > 2 ? registerMap.getOrDefault(regs[2], 0) : 0;

        System.out.println("rs: " + rs + ", rt: " + rt + ", rd: " + rd);

        // Constructs the binary value of each R-type instruction using rd, rs, and rt
        String binary = format.opcode +
                        String.format("%05d", Integer.parseInt(Integer.toBinaryString(rs & 0x1F))) +
                        String.format("%05d", Integer.parseInt(Integer.toBinaryString(rt & 0x1F))) +
                        String.format("%05d", Integer.parseInt(Integer.toBinaryString(rd & 0x1F))) +
                        "00000" + format.funct; // Helps shift the amount, usually 0

        // After converting to binary, convert string to hexadecimal
        return String.format("%08x", Integer.parseUnsignedInt(binary, 2));
    }

    private static String assembleIType(String operands, InstructionFormat format) {
        // Starts by removing all spaces and splits the operands by their commas
        // Then stores the results as an element in regs String array
        String[] regs = operands.replaceAll("\\s", "").split(",");

        // Gets the destination (rd), source (rs), and target (rt) registers
        // Defaults to zero if they're missing
        int rd = registerMap.getOrDefault(regs[0], 0);
        int rs = regs.length > 1 ? registerMap.getOrDefault(regs[1], 0) : 0;
        int rt = regs.length > 2 ? registerMap.getOrDefault(regs[2], 0) : 0;

        // Constructs the binary value of each R-type instruction using rd, rs, and rt
        String binary = format.opcode +
                String.format("%5s", Integer.toBinaryString(rs)).replace(' ', '0') +
                String.format("%5s", Integer.toBinaryString(rt)).replace(' ', '0') +
                String.format("%5s", Integer.toBinaryString(rd)).replace(' ', '0') +
                "00000" + format.funct; // Helps shift the amount, usually 0

        // After converting to binary, convert string to hexadecimal
        return String.format("%08x", Integer.parseUnsignedInt(binary, 2));
    }

    private static String assembleJType(String operands, InstructionFormat format) {
        // Starts by removing all spaces and splits the operands by their commas
        // Then stores the results as an element in regs String array
        String[] regs = operands.replaceAll("\\s", "").split(",");

        // Gets the destination (rd), source (rs), and target (rt) registers
        // Defaults to zero if they're missing
        int rd = registerMap.getOrDefault(regs[0], 0);
        int rs = regs.length > 1 ? registerMap.getOrDefault(regs[1], 0) : 0;
        int rt = regs.length > 2 ? registerMap.getOrDefault(regs[2], 0) : 0;

        // Constructs the binary value of each R-type instruction using rd, rs, and rt
        String binary = format.opcode +
                String.format("%5s", Integer.toBinaryString(rs)).replace(' ', '0') +
                String.format("%5s", Integer.toBinaryString(rt)).replace(' ', '0') +
                String.format("%5s", Integer.toBinaryString(rd)).replace(' ', '0') +
                "00000" + format.funct; // Helps shift the amount, usually 0

        // After converting to binary, convert string to hexadecimal
        return String.format("%08x", Integer.parseUnsignedInt(binary, 2));
    }

    // Helper class for storing the instruction format details
    private static class InstructionFormat {
        String opcode; // Opcode for the instruction
        String funct; // Function code (this if for R-type instructions)
        String type; // Instruction type i.e. R, I, J

        // Constructor for InstructionFormat class
        InstructionFormat(String opcode, String funct, String type) {
            this.opcode = opcode;
            this.funct = funct;
            this.type = type;
        }
    }
} // End class