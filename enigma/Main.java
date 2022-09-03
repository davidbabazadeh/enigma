package enigma;

import ucb.util.CommandArgs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Arrays;

import static enigma.EnigmaException.error;

/** Enigma simulator.
 *  @author David Babazadeh
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine mach = readConfig();
        if (_input.hasNext("\\*")) {
            setUp(mach, _input.nextLine());
        } else {
            throw error("invalid input: first line must indicate settings");
        }

        while (_input.hasNextLine()) {
            String nextLine = _input.nextLine();
            if (nextLine.contains("*")) {
                setUp(mach, nextLine);
            } else {
                String msg = nextLine.replaceAll(" ", "");
                printMessageLine(mach.convert(msg));
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());

            if (!_config.hasNextInt()) {
                throw error("invalid config: numRotors must be int");
            }
            int numRotors = _config.nextInt();

            if (!_config.hasNextInt()) {
                throw error("invalid config: numPawls must be int");
            }
            int pawls = _config.nextInt();

            List<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }

            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String type = _config.next();

            String cycles = readNextCycle();

            return switch (type.charAt(0)) {
            case 'M' -> new MovingRotor(name, new Permutation(cycles,
                    _alphabet), type.substring(1));
            case 'N' -> new FixedRotor(name, new Permutation(cycles,
                    _alphabet));
            default -> new Reflector(name, new Permutation(cycles, _alphabet));
            };
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** reads next sequence of cycles from config and returns string
     *  for permutation. */
    private String readNextCycle() {
        String cycles = "";

        while (_config.hasNext("\\(.+\\)")) {
            cycles += _config.next() + " ";
        }

        if (cycles.equals("")) {
            return cycles;
        }

        return cycles.substring(0, cycles.length() - 1);
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment.
     *  expects asterisk  */
    private void setUp(Machine M, String settings) {
        String[] settingsArr = settings.split(" ");
        if (!Objects.equals(settingsArr[0], "*")) {
            throw error("invalid settings line: must begin with *");
        } else if (settingsArr.length < M.numRotors() + 2) {
            throw error("invalid settings: requires %d arguments",
                    M.numRotors() + 2);
        }

        M.insertRotors(Arrays.copyOfRange(settingsArr, 1, M.numRotors() + 1));

        if (settingsArr.length > M.numRotors() + 2
                && !settingsArr[M.numRotors() + 2].contains("(")) {
            M.setRotors(settingsArr[M.numRotors() + 1],
                    settingsArr[M.numRotors() + 2]);
        } else {
            M.setRotors(settingsArr[M.numRotors() + 1]);
        }

        if (!settings.contains("(")) {
            M.setPlugboard(new Permutation("", _alphabet));
        } else {
            M.setPlugboard(new Permutation(
                    settings.substring(settings.indexOf('(')), _alphabet));
        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 1) {
            _output.print(msg.charAt(i));
            if ((i + 1) % 5 == 0) {
                _output.print(" ");
            }
        }
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
