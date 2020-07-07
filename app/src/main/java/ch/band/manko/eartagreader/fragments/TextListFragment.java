/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import ch.band.manko.eartagreader.R;
import ch.band.manko.eartagreader.adapters.ConfirmedEarTagListAdapter;
import ch.band.manko.eartagreader.data.EarTagRepository;
import ch.band.manko.eartagreader.databinding.FragmentListBinding;
import ch.band.manko.eartagreader.models.EarTag;

/**
 * This is the Fragment that is shown after starting the app
 * It just shows a list of the saved tvd-numbers and asks for permission to use the camera.
 *
 * Asking for the Permission here simplified creating camera objects in the AnalysePhotoFragment.
 * It also improves the user experience because he can only open the AnalysePhotoFragment when the
 * permissions allow it.
 */
public class TextListFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = TextListFragment.class.getCanonicalName();
    private FragmentListBinding binding;

    // This is an arbitrary number we are using to keep track of the permission
    // request. Where an app has multiple context for requesting permission,
    // this can help differentiate the different contexts.
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    // This is an array with all the permission required in AnalysePhotoFragment.
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null. This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>A default View can be returned by calling {@link #Fragment(int)} in your
     * constructor. Otherwise, this method returns null.
     *
     * <p>It is recommended to <strong>only</strong> inflate the layout in this method and move
     * logic that operates on the returned View to {@link #onViewCreated(View, Bundle)}.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater,container,false);
        binding.rvTextlist.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTextlist.setAdapter(new ConfirmedEarTagListAdapter());

        binding.fabAdd.setOnClickListener(view->{
            //Ask for Permission to use the camera if permission isn't already granted.
            if (!allPermissionsGranted()) {
                this.requestPermissions(REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS); }
            else {
                navigateToAnalysePhotoFragment();
            }
        });
        return binding.getRoot();
    }

    /**
     * Once the Views are created, the Fragments starts listening to changes to the tvd-number list
     * and updates the ui when there are changes.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EarTagRepository repository = new EarTagRepository(getContext());
        repository.getAll().observe(this.getViewLifecycleOwner(), new Observer<List<EarTag>>() {
            @Override
            public void onChanged(List<EarTag> earTags) {
                updateProposedList(earTags);
            }
        });
    }

    /**
     * Updates the to the user shown list, with the new list.
     * @param list
     */
    private void updateProposedList(List<EarTag> list){
        ConfirmedEarTagListAdapter adapter = (ConfirmedEarTagListAdapter) binding.rvTextlist.getAdapter();
        if(adapter == null)
            return;
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                navigateToAnalysePhotoFragment();
            } else {
                Toast.makeText(this.getContext(),
                        R.string.no_camera_permission,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Checks if all permission required for the AnalysePhotoFragment have been granted.
     */
    private boolean allPermissionsGranted(){
        boolean granted = true;
        for (String permission:REQUIRED_PERMISSIONS) {
            granted &= ContextCompat.checkSelfPermission(getContext(),permission) == PackageManager.PERMISSION_GRANTED;
        }
        return granted;
    }

    /**
     * Navigates to the AnalysePhotoFragment.
     *
     * Should only be called once all required permissions are granted.
     */
    private void navigateToAnalysePhotoFragment(){
        NavDirections action = TextListFragmentDirections.actionAddText();
        Navigation.findNavController(binding.getRoot()).navigate(action);
    }
}
