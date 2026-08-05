package dev.omar.aiagent.ui.sheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import dev.omar.aiagent.App;
import dev.omar.aiagent.databinding.SheetModelsBinding;
import dev.omar.aiagent.mcp.model.GeminiModel;
import dev.omar.aiagent.mcp.service.GeminiModelService;
import dev.omar.aiagent.mcp.service.IGeminiModelService;
import dev.omar.aiagent.ui.adapter.ModelsAdapter;

public class ModelsBottomSheet extends BottomSheetDialogFragment {

    private SheetModelsBinding binding;
    private GeminiModelService modelService;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SheetModelsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ModelsAdapter adapter = new ModelsAdapter();
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        modelService = new GeminiModelService(App.getApiKey());
        modelService.fetchFunctionCallingModels(new IGeminiModelService.ModelCallback() {
            @Override
            public void onSuccess(List<GeminiModel> models, String markdownFormattedResult) {
                getActivity().runOnUiThread(()->{
                    adapter.submitList(models);
                    binding.progress.setVisibility(View.GONE);
                });

            }

            @Override
            public void onError(String errorMessage) {
                getActivity().runOnUiThread(()->{
                    binding.progress.setVisibility(View.GONE);
                });
            }
        });

    }
}
