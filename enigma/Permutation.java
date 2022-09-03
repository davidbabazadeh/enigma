package enigma;

import java.util.Arrays;
import java.util.List;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author David Babazadeh
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        if (cycles.length() > 2) {
            if (cycles.charAt(0) != ('(')
                    || cycles.charAt(cycles.length() - 1) != (')')) {
                throw error("invalid cycle sequence %s", cycles);
            }
            _cycles = Arrays.asList(cycles.substring(1, cycles.length() - 1)
                    .split("\\)\\s*\\("));
        } else {
            _cycles = List.of("");
        }
    }

    /** returns the permutation's cycles. */
    public String getCycles() {
        return _cycles.toString();
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles.add(cycle);
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return alphabet().size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int pIndex = p;
        char pChar = alphabet().toChar(wrap(p));
        String pStr = Character.toString(pChar);
        for (String cycle : _cycles) {
            if (cycle.contains(pStr)) {
                pChar = cycle.charAt((cycle.indexOf(pChar) + 1)
                        % cycle.length());
                pIndex = alphabet().toInt(pChar);
                break;
            }
        }
        return pIndex;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size.  */
    int invert(int c) {
        int cIndex = c;
        char cChar = alphabet().toChar(wrap(c));
        String cStr = Character.toString(cChar);
        for (String cycle : _cycles) {
            if (cycle.contains(cStr)) {
                cIndex = alphabet().toInt(cycle.charAt(
                        (cycle.indexOf(cChar) - 1 + cycle.length())
                                % cycle.length()));
                break;
            }
        }
        return cIndex;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return alphabet().toChar(permute(alphabet().toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return alphabet().toChar(invert(alphabet().toInt(c)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < alphabet().size(); i++) {
            if (i == permute(i)) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** cycles this permutation maps to. */
    private List<String> _cycles;

}
