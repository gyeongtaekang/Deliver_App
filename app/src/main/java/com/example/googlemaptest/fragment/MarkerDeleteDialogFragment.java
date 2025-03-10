package com.example.googlemaptest.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.googlemaptest.MyApplication;
import com.example.googlemaptest.R;
import com.example.googlemaptest.api.Party;

public class MarkerDeleteDialogFragment extends DialogFragment {
    EditText passwordEditText;
    Button confirmButton, cancelButton;
    private int id;

    private MarkerDeleteSuccessListener successListener;
    private MarkerDeleteFailureListener failureListener;

    public MarkerDeleteDialogFragment(int id) {
        this.id = id;
    }

    public interface MarkerDeleteSuccessListener {
        void onDeleteSuccess(int id);
    }

    public interface MarkerDeleteFailureListener {
        void onDeleteFailure(int id);
    }

    public void setListener(MarkerDeleteSuccessListener successListener,
                            MarkerDeleteFailureListener failureListener) {
        this.successListener = successListener;
        this.failureListener = failureListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_marker_delete_dialog, container, false);

        passwordEditText = root.findViewById(R.id.passwordEditText);
        confirmButton = root.findViewById(R.id.confirmButton);
        cancelButton = root.findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(view -> {
            String enteredPassword = passwordEditText.getText().toString().trim();
            Party party = MyApplication.getPartyMap().get(id);
            if (enteredPassword.equals(party.getMarker_password())) {
                if (successListener != null) {
                    successListener.onDeleteSuccess(id);
                }
            } else {
                if (failureListener != null) {
                    failureListener.onDeleteFailure(id);
                }
            }
            dismiss();
        });

        cancelButton.setOnClickListener(view -> dismiss());

        return root;
    }
}
