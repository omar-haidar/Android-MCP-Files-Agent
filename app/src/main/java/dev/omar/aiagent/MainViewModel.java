package dev.omar.aiagent;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.omar.aiagent.mcp.GeminiFileAgent;

public class MainViewModel extends AndroidViewModel {
    private GeminiFileAgent agent;
    private MutableLiveData<Boolean> canChat = new MutableLiveData<>(true);
    private MutableLiveData<String> messageData = new MutableLiveData<>();
    public MainViewModel(@NonNull Application application) {
        super(application);
        agent = new GeminiFileAgent(application.getApplicationContext(),App.getApiKey());
        agent.setMemoryEnabled(App.get().getConfigs().isMemoryEnabled());
    }

    public GeminiFileAgent getAgent(){
        return agent;
    }


    public LiveData<Boolean> canChat(){
        return canChat;
    }

    public LiveData<String> getMesage(){
        return messageData;
    }

    public void executePrompt(String userPrompt) {
        canChat.postValue(false);
        agent.processUserPrompt(userPrompt, new GeminiFileAgent.AgentCallback() {
            @Override
            public void onSuccess(String message) {
               messageData.postValue(message);
               canChat.postValue(true);
            }

            @Override
            public void onError(String error) {
                messageData.postValue(error);
                canChat.postValue(true);
            }
        });
    }

}
