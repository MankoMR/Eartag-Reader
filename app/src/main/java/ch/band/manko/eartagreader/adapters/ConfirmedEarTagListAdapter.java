/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ch.band.manko.eartagreader.databinding.ConfirmedEartagItemBinding;
import ch.band.manko.eartagreader.models.EarTag;

/**
 * @See <a href="https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter">ListAdapter</a>
 * @See <a href="https://developer.android.com/guide/topics/ui/layout/recyclerview">Recyclerview</a>
 */
public class ConfirmedEarTagListAdapter extends ListAdapter<EarTag, ConfirmedEarTagListAdapter.EarTagItem> {
    private static final DiffUtil.ItemCallback<EarTag> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<EarTag>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull EarTag oldEarTag, @NonNull EarTag newEarTag) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldEarTag == newEarTag;
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull EarTag oldTvd, @NonNull EarTag newTvd) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldTvd.equals(newTvd);
                }
            };

    public ConfirmedEarTagListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public EarTagItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ConfirmedEartagItemBinding binding= ConfirmedEartagItemBinding.inflate(inflater,parent,false);
        return new EarTagItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EarTagItem holder, int position) {
        holder.setContent(getItem(position),position);
    }

    public class EarTagItem extends RecyclerView.ViewHolder {
        EarTag earTag;
        ConfirmedEartagItemBinding binding;
        private int position;

        public EarTagItem(@NonNull ConfirmedEartagItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            earTag = new EarTag("");
            position = -1;
        }

        public void setContent(EarTag number, int position){
            this.earTag = number;
            this.position = position;
            binding.earTagNumber.setText(number.number.toString());
        }
    }
}
