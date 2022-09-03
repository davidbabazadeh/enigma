package enigma;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author David Babazadeh
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {

        if (numRotors - pawls < 1 || numRotors < 2) {
            throw error("invalid machine: number of pawls or rotors");
        } else if (allRotors.size() < numRotors) {
            throw error("invalid machine: too many slots for numRotors");
        }

        _alphabet = alpha;
        _rotors = new Rotor[numRotors];
        _pawls = pawls;
        _allRotors = new HashMap<String, Rotor>(allRotors.size());
        for (Rotor r : allRotors) {
            _allRotors.put(r.name(), r);
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _rotors.length;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        assert rotors.length == _rotors.length;

        for (int i = 0; i < rotors.length; i += 1) {
            Rotor selected = _allRotors.get(rotors[i]);

            if (i == 0) {
                if (!(selected instanceof Reflector)) {
                    throw error("first rotor must be reflector");
                }
            } else if (i < numRotors() - numPawls()) {
                if (selected instanceof Reflector
                        || selected instanceof MovingRotor) {
                    throw error("rotor %d must be type NonMoving", i);
                }
            } else {
                if (!(selected instanceof MovingRotor)) {
                    throw error("rotor %d must be type Moving", i);
                }
            }

            if (selected != null
                    && Arrays.asList(rotors).indexOf(rotors[i]) == i) {
                _rotors[i] = selected;
            } else {
                throw error("invalid or repeated rotor input %s", rotors[i]);
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw error("invalid number of rotor positions");
        }

        for (int i = 1; i < _rotors.length; i += 1) {
            char posn = setting.charAt(i - 1);

            if (!alphabet().contains(posn)) {
                throw error("invalid position: character %c not in alphabet",
                        posn);
            }

            _rotors[i].set(posn);
            _rotors[i].setRing(0);
        }
    }

    /** Set my rotors according to SETTING and RINGSTELLUNG,
     *  which both must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting, String ringstellung) {
        if (ringstellung.length() != numRotors() - 1) {
            throw error("invalid number of rotor positions");
        }

        String rSetting = "";
        for (int i = 0; i < setting.length(); i++) {
            rSetting += alphabet().toChar((alphabet().toInt(setting.charAt(i))
                    - alphabet().toInt(ringstellung.charAt(i))
                    + alphabet().size()) % alphabet().size());
        }
        setRotors(rSetting);

        for (int i = 0; i < setting.length(); i++) {
            _rotors[i + 1].setRing(ringstellung.charAt(i));
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine.  */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        int[] advances = new int[_rotors.length];
        for (int i = _rotors.length - _pawls + 1; i < _rotors.length; i += 1) {
            if (_rotors[i].atNotch()) {
                advances[i] += 1;
                advances[i - 1] += 1;
            }
        }
        advances[_rotors.length - 1] += 1;
        for (int i = _rotors.length - _pawls; i < _rotors.length; i += 1) {
            if (advances[i] > 0) {
                _rotors[i].advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _rotors.length - 1; i > 0; i -= 1) {
            c = _rotors[i].convertForward(c);
        }
        for (Rotor r : _rotors) {
            c = r.convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String code = "";
        for (int i = 0; i < msg.length(); i += 1) {
            code += alphabet().toChar(convert(alphabet().toInt(msg.charAt(i))));
        }
        return code;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** number of pawls. */
    private int _pawls;

    /** mapping of rotor collection by name. */
    private final HashMap<String, Rotor> _allRotors;

    /** selected rotors. */
    private Rotor[] _rotors;

    /** plugboard permutation aka stecker. */
    private Permutation _plugboard;
}
