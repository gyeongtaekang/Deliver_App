package com.example.googlemaptest.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.googlemaptest.R;

public class FilterDialogFragment extends DialogFragment {

    private Button buttonApplyFilter;
    private SeekBar seekBarRange;
    private TextView textViewRange;
    private Spinner spinnerFoodOptions;
    private FilterDialogListener listener;
    private SeekBar seekBarParticipantRange;
    private TextView textViewParticipantRange;

    public interface FilterDialogListener {
        void onFilterSelected(String foodCategory, int range, int participantCount);
    }


    public void setFilterDialogListner(FilterDialogListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_dialog, container, false);

        // 버튼 바인딩
        buttonApplyFilter = view.findViewById(R.id.button_apply_filter);

        // 슬라이더와 텍스트뷰 바인딩
        seekBarRange = view.findViewById(R.id.seekbar_range);
        textViewRange = view.findViewById(R.id.textview_range);

        // 스피너 바인딩 및 설정
        spinnerFoodOptions = view.findViewById(R.id.spinner_food_options);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.food_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFoodOptions.setAdapter(adapter);
        // 참가 인원 수 선택 UI 바인딩
        seekBarParticipantRange = view.findViewById(R.id.seekbar_participant_range);
        textViewParticipantRange = view.findViewById(R.id.textview_participant_range);

        // 참가 인원 수 SeekBar 리스너
        seekBarParticipantRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewParticipantRange.setText("참가 인원 수: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 필요한 코드 작성
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 필요한 코드 작성
            }
        });

        // SeekBar 변경 리스너 설정
        seekBarRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewRange.setText("범위 선택: " + (progress * 10) + " m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 사용자가 터치를 시작할 때 수행할 동작 (여기에 필요한 코드가 없다면 비워두셔도 됩니다)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 사용자가 터치를 끝낼 때 수행할 동작 (여기에 필요한 코드가 없다면 비워두셔도 됩니다)
            }
        });

        // 버튼 클릭 리스너 설정
        // 필터 적용 버튼 클릭 리스너
        buttonApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedRange = seekBarRange.getProgress();
                String selectedFoodOption = spinnerFoodOptions.getSelectedItem().toString();
                int selectedParticipantCount = seekBarParticipantRange.getProgress();
                listener.onFilterSelected(selectedFoodOption, selectedRange, selectedParticipantCount); // 수정된 매개변수 전달
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(1100, 1100); // 가로, 세로 크기를 300dp로 설정
        }
        // SeekBar thumb 설정
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.de); // 'your_image_name'을 이미지 파일 이름으로 변경하세요.
        seekBarRange.setThumb(drawable);
    }
    private void applyFilter() {
        int selectedRange = seekBarRange.getProgress();
        String selectedFoodOption = spinnerFoodOptions.getSelectedItem().toString();
        int selectedParticipantCount = seekBarParticipantRange.getProgress();
        // 인터페이스를 통해 MainActivity에 필터 옵션 전달
        listener.onFilterSelected(selectedFoodOption, selectedRange, selectedParticipantCount);
    }

}
