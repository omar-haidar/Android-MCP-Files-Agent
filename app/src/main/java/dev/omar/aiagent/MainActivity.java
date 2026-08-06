package dev.omar.aiagent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import dev.omar.aiagent.databinding.ActivityMainBinding;
import dev.omar.aiagent.ui.sheet.ModelsBottomSheet;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setSubtitle(App.getModelId());
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .build();
        mainViewModel.canChat().observe(this, canChat -> {
            binding.progress.setVisibility(canChat ? View.GONE : View.VISIBLE);
            binding.imgSend.setEnabled(canChat);
            binding.imgSend.setImageDrawable(canChat ? ContextCompat.getDrawable(MainActivity.this, R.drawable.send_24px) : ContextCompat.getDrawable(MainActivity.this, R.drawable.stop_24px));
        });

        mainViewModel.getMesage().observe(this, message -> {
            markwon.setMarkdown(binding.txtResult, message);
        });

        binding.imgSend.setOnClickListener(v -> {
            if (!binding.txtInput.getText().toString().isEmpty()) {
                mainViewModel.executePrompt(binding.txtInput.getText().toString());
                binding.txtInput.setText("");
            }

        });
        mainViewModel.executePrompt("preview your tools");
        App.get().getConfigs().setModelChangedListener(model->{
            binding.toolbar.setSubtitle(model);
        });


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.findItem(R.id.menu_item_use_memory) != null) {
            menu.findItem(R.id.menu_item_use_memory).setChecked(App.get().getConfigs().isMemoryEnabled());
        }
        if (menu.findItem(R.id.menu_item_reload) != null) {
            menu.findItem(R.id.menu_item_reload).setEnabled(mainViewModel.canChat().getValue());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_change_apikey) {
            SetApiKeyActivity.changeApiKey(MainActivity.this);
        } else if (item.getItemId() == R.id.menu_item_change_model) {
            new ModelsBottomSheet().show(getSupportFragmentManager(), "Models");
        } else if (item.getItemId() == R.id.menu_item_use_memory) {
            App.get().getConfigs().setMemoryEnabled(!item.isChecked());
            item.setChecked(!item.isChecked());
            mainViewModel.getAgent().setMemoryEnabled(item.isChecked());
        } else if (item.getItemId() == R.id.menu_item_about) {
           App.showAboutDialog(MainActivity.this);
        }else if (item.getItemId() == R.id.menu_item_reload) {
            mainViewModel.executePrompt("preview your tools");
        }
        return super.onOptionsItemSelected(item);
    }
}