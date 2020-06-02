package ch.band.manko.tvdnumberreader.models;

/**
 * A ProposedTvdNumber is a tvd-number which the user might add to the tvd-number list.
 * It implements Comparable<ProposedTvdNumber> sort the numbers in a list after the number of occurrences the
 * tvd-number got recognized from the camera.
 *
 * This is a class just for storing related information, therefore all fields are public for convenience.
 */
public class ProposedTvdNumber implements Comparable<ProposedTvdNumber> {
    public String tvdNumber;
    public int occurrence;
    public boolean isRegistered;

    /**
     * Creates a ProposedTvdNumber.
     *
     * @param tvdNumber: the tvd-number this Object represents
     * @param occurrence: how many times this Object already occurred.
     * @param isRegistered: whether the number is already in the tvd-number list.
     */
    public ProposedTvdNumber(String tvdNumber, int occurrence, boolean isRegistered){
        this.tvdNumber = tvdNumber;
        this.occurrence = occurrence;
        this.isRegistered = isRegistered;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProposedTvdNumber that = (ProposedTvdNumber) o;
        return occurrence == that.occurrence &&
                isRegistered == that.isRegistered &&
                tvdNumber.equals(that.tvdNumber);
    }
    @Override
    public int compareTo(ProposedTvdNumber o) {
        if(this.occurrence > o.occurrence)
            return -1;
        if (this.occurrence < o.occurrence)
            return  1;
        return 0;
    }
}
