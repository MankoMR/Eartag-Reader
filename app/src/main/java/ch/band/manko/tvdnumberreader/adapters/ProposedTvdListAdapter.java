package ch.band.manko.tvdnumberreader.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import ch.band.manko.tvdnumberreader.R;
import ch.band.manko.tvdnumberreader.databinding.ProposedTvdItemBinding;
import ch.band.manko.tvdnumberreader.models.ProposedTvdNumber;

public class ProposedTvdListAdapter extends ListAdapter<ProposedTvdNumber, ProposedTvdListAdapter.ProposedTvdItem> {

    private static final DiffUtil.ItemCallback<ProposedTvdNumber> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ProposedTvdNumber>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull ProposedTvdNumber oldTvd, @NonNull ProposedTvdNumber newTvd) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldTvd == newTvd;
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull ProposedTvdNumber oldTvd, @NonNull ProposedTvdNumber newTvd) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldTvd.equals(newTvd);
                }
            };

    private final ItemInteractions listener;
    public ProposedTvdListAdapter(@NonNull ItemInteractions listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }
    @NonNull
    @Override
    public ProposedTvdItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ProposedTvdItemBinding binding= ProposedTvdItemBinding.inflate(inflater,parent,false);
        return new ProposedTvdItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProposedTvdItem holder, int position) {

        holder.setContent(this.getItem(position), position);
    }

    class ProposedTvdItem extends RecyclerView.ViewHolder {
        private ProposedTvdItemBinding binding;
        private ProposedTvdNumber tvd;
        private int position;

        public ProposedTvdItem(@NonNull ProposedTvdItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
            position = -1;
            binding.confirm.setOnClickListener(view->{
                listener.onConfirm(tvd,position);
            });
            binding.cancel.setOnClickListener(view->{
                listener.onRemove(tvd,position);
            });
        }
        @SuppressLint("DefaultLocale")
        public void setContent(ProposedTvdNumber tvd, int position){
            this.tvd = tvd;
            binding.text.setText(tvd.tvdNumber);
            binding.occurence.setText(String.format("%d",tvd.occurrence));
            if (tvd.isRegistered){
                int color = ResourcesCompat.getColor(binding.getRoot().getResources(),R.color.exist,null);
                binding.resultItemCard.setCardBackgroundColor(color);
                binding.confirm.setVisibility(View.INVISIBLE);
            }
            this.position = position;
        }
    };
    public interface ItemInteractions{
        void onConfirm(ProposedTvdNumber tvd, int position);
        void onRemove(ProposedTvdNumber tvd, int position);
    }
}
