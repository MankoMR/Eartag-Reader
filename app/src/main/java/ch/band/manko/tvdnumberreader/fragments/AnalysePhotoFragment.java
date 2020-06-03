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
        binding.imageButton.setOnClickListener(this::toggleFlashLamp);
        return binding.getRoot();
    }
    private void navigateBack(){
        NavDirections action = AnalysePhotoFragmentDirections.actionShowList();
        Navigation.findNavController(binding.getRoot()).navigate(action);
    }

    private void cameraSetup(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                //.setTargetResolution(new Size(720,720)))
                .setTargetResolution(new Size(1080,1080))
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Build the image analysis use case and instantiate our analyzer
        ImageAnalysis analyzerUseCase = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1080,1080))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analyzerUseCase.setAnalyzer(Executors.newSingleThreadExecutor(), new TextRecognizer(viewModel));

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview,analyzerUseCase);
        preview.setSurfaceProvider(binding.tvCamStream.createSurfaceProvider());
    }
    private void toggleFlashLamp(View view) {
        try {
            int torchstate = camera.getCameraInfo().getTorchState().getValue();
            if (torchstate == TorchState.OFF) {
                binding.imageButton.setImageResource(R.drawable.ic_flash_off_black_24dp);
                camera.getCameraControl().enableTorch(true);
            } else if (torchstate == TorchState.ON) {
                binding.imageButton.setImageResource(R.drawable.ic_flash_on_black_24dp);
                camera.getCameraControl().enableTorch(false);
            }
            playSoundTextRecognized();
        }catch (NullPointerException e){
            Log.e(TAG,"Error unboxing torchstate",e);
        }
    }

    public void playSoundTextRecognized() {
        newEntityPlayer.seekTo(0);
        newEntityPlayer.start();
    }

    public void playSoundTouch() {
        onAddPlayer.seekTo(0);
        onAddPlayer.start();
    }

    public void updateProposedList(List<ProposedTvdNumber> list){
        ProposedTvdListAdapter adapter = (ProposedTvdListAdapter) binding.proposalList.getAdapter();
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }
}
