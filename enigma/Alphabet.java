package enigma;

import static enigma.EnigmaException.error;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author David Babazadeh
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated.  */
    Alphabet(String chars) {
        _chars = chars;
        for (int i = _chars.length() - 1; i > 0; i -= 1) {
            char ch = _chars.charAt(i);

            if (i != _chars.indexOf(ch) || ch == ')' || ch == '(') {
                throw error("invalid alphabet: "
                        + "repeated or invalid character %c", ch);
            }
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _chars.contains(Character.toString(ch));
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _chars.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        int index = _chars.indexOf(ch);
        return switch (index) {
        case (-1) -> throw error("character %s not in alphabet");
        default -> index;
        };
    }

    /** string of unique, ordered alphabet characters. */
    private String _chars;

}
