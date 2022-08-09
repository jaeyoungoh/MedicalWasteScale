package kr.co.rubeesys.medicalWasteScale.common;

import static kr.co.rubeesys.medicalWasteScale.common.InitString.EMPTY_STRING;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    private String selectedDate = EMPTY_STRING;
    private String selectedTag = EMPTY_STRING;

    private OnDateAdjustedListener mOnDateAdjustedListener;

    public interface OnDateAdjustedListener {
        void onDateAdjusted(String tag, String selectedDate);
    }
    public DatePickerFragment(OnDateAdjustedListener onDateAdjustedListener) {
        mOnDateAdjustedListener = onDateAdjustedListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar mCalendar = Calendar.getInstance();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, dayOfMonth);
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        selectedDate = new SimpleDateFormat("yyyy-MM-dd").format(mCalendar.getTime());
        mOnDateAdjustedListener.onDateAdjusted(getTag(), selectedDate);
    }
}
