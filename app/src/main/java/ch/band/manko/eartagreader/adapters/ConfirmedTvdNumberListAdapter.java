package ch.band.manko.eartagreader.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ch.band.manko.eartagreader.databinding.ConfirmedTvdnumberItemBinding;
import ch.band.manko.eartagreader.models.EarTag;

/**
 * @See <a href="https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter">ListAdapter</a>
 * @See <a href="https://developer.android.com/guide/topics/ui/layout/recyclerview">Recyclerview</a>
 */
public class ConfirmedTvdNumberListAdapter extends ListAdapter<EarTag, ConfirmedTvdNumberListAdapter.ConfirmedTvdNumberItem> {
    private static final DiffUtil.ItemCallback<EarTag> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<EarTag>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull EarTag oldTvd, @NonNull EarTag newTvd) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldTvd == newTvd;
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull EarTag oldTvd, @NonNull EarTag newTvd) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldTvd.equals(newTvd);
                }
            };

    public ConfirmedTvdNumberListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ConfirmedTvdNumberItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ConfirmedTvdnumberItemBinding binding= ConfirmedTvdnumberItemBinding.inflate(inflater,parent,false);
        return new ConfirmedTvdNumberItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmedTvdNumberItem holder, int position) {
        holder.setContent(getItem(position),position);
    }

    public class ConfirmedTvdNumberItem extends RecyclerView.ViewHolder {
        EarTag number;
        ConfirmedTvdnumberItemBinding binding;
        private int position;

        public ConfirmedTvdNumberItem(@NonNull ConfirmedTvdnumberItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            number = new EarTag("");
            position = -1;
        }

        public void setContent(EarTag number, int position){
            this.number = number;
            this.position = position;
            binding.tvdNumber.setText(number.number.toString());
        }
    }
}
