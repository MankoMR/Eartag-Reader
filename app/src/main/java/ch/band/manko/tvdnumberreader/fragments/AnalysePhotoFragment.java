package ch.band.manko.tvdnumberreader.fragments;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Size;

import androidx.annotation.Nullable;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.util.concurrent.ListenableFuture;

import ch.band.manko.tvdnumberreader.AnalysePhotoViewModel;
import ch.band.manko.tvdnumberreader.R;
import ch.band.manko.tvdnumberreader.TextRecognizer;
import ch.band.manko.tvdnumberreader.adapters.ProposedTvdListAdapter;
import ch.band.manko.tvdnumberreader.databinding.FragmentAnalysePhotoBinding;
import ch.band.manko.tvdnumberreader.models.ProposedTvdNumber;

public class AnalysePhotoFragment extends Fragment implements AnalysePhotoViewModel.ICommandExecutor {
    private static final String TAG = AnalysePhotoFragment.class.getSimpleName();

    private FragmentAnalysePhotoBinding binding;
    private AnalysePhotoViewModel viewModel = new AnalysePhotoViewModel(this,getContext());
    private Camera camera;
    private MediaPlayer newEntityPlayer;
    private MediaPlayer onAddPlayer;

    /**
     * Releases resources associated with these MediaPlayer objects.
     * It is considered good practice to call this method when you're
     * done using the MediaPlayers. In particular, whenever an Activity
     * of an application is paused (its onPause() method is called),
     * or stopped (its onStop() method is called), this method should be
     * invoked to release the MediaPlayer objects, unless the application
     * has a special need to keep the object around. In addition to
     * unnecessary resources (such as memory and instances of codecs)
     * being held, failure to call this method immediately if
     * MediaPlayer objects are no longer needed may also lead to
     * continuous battery consumption for mobile devices, and playback
     * failure for other applications if no multiple instances of the
     * same codec are supported on a device. Even if multiple instances
     * of the same codec are supported, some performance degradation
     * may be expected when unnecessary multiple instances are used
     * at the same time.
     */
    private void releaseMediaplayers(){
        onAddPlayer.release();
        newEntityPlayer.release();
    }
    private void instantiateMediaPlayers(){
        newEntityPlayer = MediaPlayer.create(getContext(), R.raw.ui_unlock);
        onAddPlayer = MediaPlayer.create(getContext(), R.raw.ui_tap_variant_04);
    }

    @Override
    public void onStart() {
        super.onStart();
        instantiateMediaPlayers();
    }

    @Override
    public void onPause() {
        releaseMediaplayers();
        super.onPause();
    }

    @Override
    public void onStop() {
        releaseMediaplayers();
        super.onStop();
    }

    /**
     * This function sets the View up.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalysePhotoBinding.inflate(inflater,container,false);
        binding.fabConfirm.setOnClickListener(view -> navigateBack());
        binding.proposalList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.proposalList.setAdapter(new ProposedTvdListAdapter(viewModel));
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraSetup(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getContext()));
        binding.fabToggleFlash.setOnClickListener(this::toggleFlashLamp);
        return binding.getRoot();
    }
    private void navigateBack(){
        NavDirections action = AnalysePhotoFragmentDirections.actionShowList();
        Navigation.findNavController(binding.getRoot()).navigate(action);
    }

    /**
     * This Methods sets up all the required pieces to show a stream from the camera, to analyse
     * images from stream and to toggle the flashlight.
     *
     * @param cameraProvider A singleton which can be used to bind the lifecycle of cameras
     *                       to any {@link LifecycleOwner} within an application's process.
     */
    private void cameraSetup(@NonNull ProcessCameraProvider cameraProvider) {
        //Automatically selects a camera based on the requirements.
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        //Setup the Preview to be shown on the screen.
        Preview preview = new Preview.Builder()
                //.setTargetResolution(new Size(720,720)))
                .setTargetResolution(new Size(1080,1080))
                .build();
        preview.setSurfaceProvider(binding.tvCamStream.createSurfaceProvider());

        // Setup the image analysis use case and instantiate the analyzer
        ImageAnalysis analyzerUseCase = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1080,1080))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analyzerUseCase.setAnalyzer(Executors.newSingleThreadExecutor(), new TextRecognizer(viewModel));

        //Gets a camera object to control the flashlight and binds the lifecyle of the objects to
        //the lifetime this fragment
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview,analyzerUseCase);
    }

    /**
     * This method gets called when the fabToggleFlash-Button gets pressed.
     * It changes the Icon on the Button and also the Flashlight.
     * @param view
     */
    private void toggleFlashLamp(View view) {
        try {
            int torchstate = camera.getCameraInfo().getTorchState().getValue();
            if (torchstate == TorchState.OFF) {
                binding.fabToggleFlash.setImageResource(R.drawable.ic_flash_off_black_24dp);
                camera.getCameraControl().enableTorch(true);
            } else if (torchstate == TorchState.ON) {
                binding.fabToggleFlash.setImageResource(R.drawable.ic_flash_on_black_24dp);
                camera.getCameraControl().enableTorch(false);
            }
            playSoundTextRecognized();
        }catch (NullPointerException e){
            Log.e(TAG,"Error unboxing torchstate",e);
        }
    }

    /**
     * This methods gets called from AnalysePhotoViewModel
     */
    public void playSoundTextRecognized() {
        newEntityPlayer.seekTo(0);
        newEntityPlayer.start();
    }
    /**
     * This methods gets called from AnalysePhotoViewModel
     */
    public void playSoundTouch() {
        onAddPlayer.seekTo(0);
        onAddPlayer.start();
    }
    /**
     * This methods gets called from AnalysePhotoViewModel.
     * It updates the list shown on the screen to the current one.
     */
    public void updateProposedList(List<ProposedTvdNumber> list){
        ProposedTvdListAdapter adapter = (ProposedTvdListAdapter) binding.proposalList.getAdapter();
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }
}
