package ch.band.manko.tvdnumberreader.fragments;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Size;
import android.widget.Toast;

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
import androidx.core.app.ActivityCompat;
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

public class AnalysePhotoFragment extends Fragment implements CameraXConfig.Provider, AnalysePhotoViewModel.ICommandExecutor {
    private static final String TAG = AnalysePhotoFragment.class.getSimpleName();

    private Executor executor = Executors.newSingleThreadExecutor();
    private AnalysePhotoViewModel viewModel = new AnalysePhotoViewModel(this,getContext());
    private Camera camera;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FragmentAnalysePhotoBinding binding;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.ui_unlock);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onStop() {
        CameraX.unbindAll();
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
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
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

    public void playSound() {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
    }

    public void updateProposedList(List<ProposedTvdNumber> list){
        ProposedTvdListAdapter adapter = (ProposedTvdListAdapter) binding.proposalList.getAdapter();
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
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
        analyzerUseCase.setAnalyzer(executor, new TextRecognizer(viewModel));

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview,analyzerUseCase);
        preview.setSurfaceProvider(binding.tvCamStream.createSurfaceProvider());
    }

    private void navigateBack(){
        NavDirections action = AnalysePhotoFragmentDirections.actionShowList();
        Navigation.findNavController(binding.getRoot()).navigate(action);
    }



    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
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
        }catch (NullPointerException e){
            Log.e(TAG,"Error unboxing torchstate",e);
        }
    }
}
