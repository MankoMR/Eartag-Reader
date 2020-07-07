/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ch.band.manko.eartagreader.R;
import ch.band.manko.eartagreader.databinding.ProposedEartagItemBinding;
import ch.band.manko.eartagreader.models.ProposedEarTag;

/**
 * @See <a href="https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter">ListAdapter</a>
 * @See <a href="https://developer.android.com/guide/topics/ui/layout/recyclerview">Recyclerview</a>
 */
public class ProposedEarTagListAdapter extends ListAdapter<ProposedEarTag, ProposedEarTagListAdapter.ProposedEarTagItem> {

    private static final DiffUtil.ItemCallback<ProposedEarTag> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ProposedEarTag>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull ProposedEarTag oldEarTag, @NonNull ProposedEarTag newEarTag) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldEarTag == newEarTag;
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull ProposedEarTag oldEarTag, @NonNull ProposedEarTag newEarTag) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldEarTag.equals(newEarTag);
                }
            };

    private final ItemInteractions listener;
    public ProposedEarTagListAdapter(@NonNull ItemInteractions listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }
    @NonNull
    @Override
    public ProposedEarTagItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ProposedEartagItemBinding binding= ProposedEartagItemBinding.inflate(inflater,parent,false);
        return new ProposedEarTagItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProposedEarTagItem holder, int position) {

        holder.setContent(this.getItem(position), position);
    }

    class ProposedEarTagItem extends RecyclerView.ViewHolder {
        private ProposedEartagItemBinding binding;
        private ProposedEarTag proposedEarTag;
        private int position;

        public ProposedEarTagItem(@NonNull ProposedEartagItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
            position = -1;
            binding.confirm.setOnClickListener(view->{
                listener.onConfirm(proposedEarTag,position);
            });
            binding.cancel.setOnClickListener(view->{
                listener.onRemove(proposedEarTag,position);
            });
        }
        @SuppressLint("DefaultLocale")
        public void setContent(ProposedEarTag proposedEarTag, int position){
            this.proposedEarTag = proposedEarTag;
            binding.text.setText(proposedEarTag.number);
            binding.occurence.setText(String.format("%d",proposedEarTag.occurrence));
            if (proposedEarTag.isRegistered){
                int color = ResourcesCompat.getColor(binding.getRoot().getResources(),R.color.exist,null);
                binding.resultItemCard.setCardBackgroundColor(color);
                binding.confirm.setVisibility(View.INVISIBLE);
            }else {
                int color = ResourcesCompat.getColor(binding.getRoot().getResources(),R.color.design_default_color_background,null);
                binding.resultItemCard.setCardBackgroundColor(color);
                binding.confirm.setVisibility(View.VISIBLE);
            }
            this.position = position;
        }
    };
    public interface ItemInteractions{
        void onConfirm(ProposedEarTag tvd, int position);
        void onRemove(ProposedEarTag tvd, int position);
    }
}
