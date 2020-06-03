package ch.band.manko.tvdnumberreader.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.band.manko.tvdnumberreader.R;
import ch.band.manko.tvdnumberreader.adapters.ConfirmedTvdNumberListAdapter;
import ch.band.manko.tvdnumberreader.data.TvdNumberRepository;
import ch.band.manko.tvdnumberreader.databinding.FragmentListBinding;
import ch.band.manko.tvdnumberreader.models.TvdNumber;

/**
 * This is the Fragment that is shown after starting the app
 * It just shows a list of the saved tvd-numbers and asks for permission to use the camera.
 */
public class TextListFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = TextListFragment.class.getCanonicalName();
    private FragmentListBinding binding;


    // This is an arbitrary number we are using to keep track of the permission
    // request. Where an app has multiple context for requesting permission,
    // this can help differentiate the different contexts.
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    // This is an array of all the permission specified in the manifest.
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TvdNumberRepository repository = new TvdNumberRepository(getContext());
        repository.getAll().observe(this, new Observer<List<TvdNumber>>() {
            @Override
            public void onChanged(List<TvdNumber> tvdNumbers) {
                updateProposedList(tvdNumbers);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater,container,false);
        binding.rvTextlist.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTextlist.setAdapter(new ConfirmedTvdNumberListAdapter());

        binding.fabAdd.setOnClickListener(view->{
            //Ask for Permission to use the camera if permission isn't already granted.
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[] {Manifest.permission.CAMERA},
                        REQUEST_CODE_PERMISSIONS); }
            else {
                navigateToAnalysePhotoFragment();
            }
        });
        return binding.getRoot();
    }
    private void updateProposedList(List<TvdNumber> list){
        ConfirmedTvdNumberListAdapter adapter = (ConfirmedTvdNumberListAdapter) binding.rvTextlist.getAdapter();
        if(adapter == null)
            return;
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private boolean allPermissionsGranted(){
        boolean granted = true;
        for (String permission:REQUIRED_PERMISSIONS) {
            granted &= ContextCompat.checkSelfPermission(getContext(),permission) == PackageManager.PERMISSION_GRANTED;
        }
        return granted;
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
    private void navigateToAnalysePhotoFragment(){
        NavDirections action = TextListFragmentDirections.actionAddText();
        Navigation.findNavController(binding.getRoot()).navigate(action);
    }
}
