package ch.manko.usingocrinapps.googlemlkitwithcamerax.models;

public class ProposedTvdNumber implements Comparable<ProposedTvdNumber> {
    public String tvdNumber;
    public int occurrence;
    public ProposedTvdNumber(String tvdNumber, int occurrence){
        this.tvdNumber = tvdNumber;
        this.occurrence = occurrence;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProposedTvdNumber that = (ProposedTvdNumber) o;
        return occurrence == that.occurrence &&
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
