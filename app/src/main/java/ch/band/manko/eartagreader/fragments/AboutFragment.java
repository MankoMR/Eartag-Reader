/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.band.manko.eartagreader.R;
import ch.band.manko.eartagreader.databinding.FragmentAboutBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends Fragment {
	FragmentAboutBinding binding;

	public AboutFragment() {
		// Required empty public constructor
	}
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment AboutFragment.
	 */
	public static AboutFragment newInstance() {
		return new AboutFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			//mParam1 = getArguments().getString(ARG_PARAM1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		binding = FragmentAboutBinding.inflate(inflater,container,false);
		return binding.getRoot();
	}
}