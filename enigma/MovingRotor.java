package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author David Babazadeh
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;

        for (int i = 0; i < notches.length(); i++) {
            if (!alphabet().contains(notches.charAt(i))) {
                throw error("invalid notch %c", notches.charAt(i));
            }
        }
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    @Override
    String notches() {
        String notches = "";

        for (int i = 0; i < _notches.length(); i++) {
            notches += alphabet().toChar(alphabet().toInt(_notches.charAt(i))
                    - ring());
        }

        return notches;
    }

    /** string of notches in rotor.  */
    private String _notches;

}
