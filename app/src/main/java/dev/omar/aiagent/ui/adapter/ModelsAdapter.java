package dev.omar.aiagent.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import dev.omar.aiagent.App;
import dev.omar.aiagent.databinding.ItemModelBinding;
import dev.omar.aiagent.mcp.model.GeminiModel;

public class ModelsAdapter extends ListAdapter<GeminiModel, ModelsAdapter.ModelsViewHolder> {

    public ModelsAdapter() {
        super(GeminiModel.DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ModelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ModelsViewHolder(ItemModelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModelsViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ModelsViewHolder extends RecyclerView.ViewHolder {
        private ItemModelBinding binding;

        public ModelsViewHolder(@NonNull ItemModelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(GeminiModel model) {
            binding.name.setText(model.getDisplayName());
            final String modelId = model.getName().replace("models/", "");
            binding.summary.setText(modelId);
            binding.card.setCardBackgroundColor(modelId.equals(App.getModelId()) ?
                    MaterialColors.getColor(binding.card, com.google.android.material.R.attr.colorPrimaryContainer) :
                    Color.TRANSPARENT);

        }
    }
}
