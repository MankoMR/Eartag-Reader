package ch.band.manko.tvdnumberreader.models;

/**
 * A ProposedEarTag is a tvd-number which the user might add to the tvd-number list.
 * It implements Comparable<ProposedEarTag> sort the numbers in a list after the number of occurrences the
 * tvd-number got recognized from the camera.
 *
 * This is a class just for storing related information, therefore all fields are public for convenience.
 */
public class ProposedEarTag implements Comparable<ProposedEarTag> {
    public String number;
    public int occurrence;
    public boolean isRegistered;

    /**
     * Creates a ProposedEarTag.
     *
     * @param number: the tvd-number this Object represents
     * @param occurrence: how many times this Object already occurred.
     * @param isRegistered: whether the number is already in the tvd-number list.
     */
    public ProposedEarTag(String number, int occurrence, boolean isRegistered){
        this.number = number;
        this.occurrence = occurrence;
        this.isRegistered = isRegistered;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProposedEarTag that = (ProposedEarTag) o;
        return occurrence == that.occurrence &&
                isRegistered == that.isRegistered &&
                number.equals(that.number);
    }
    @Override
    public int compareTo(ProposedEarTag o) {
        if(this.occurrence > o.occurrence)
            return -1;
        if (this.occurrence < o.occurrence)
            return  1;
        return 0;
    }
}
