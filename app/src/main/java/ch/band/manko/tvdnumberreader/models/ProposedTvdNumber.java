package ch.band.manko.tvdnumberreader.models;

public class ProposedTvdNumber implements Comparable<ProposedTvdNumber> {
    public String tvdNumber;
    public int occurrence;
    public boolean isRegistered;
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
